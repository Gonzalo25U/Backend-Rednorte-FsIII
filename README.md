# 🏥 RedNorte — Infraestructura, CI/CD y Observabilidad

Sistema de gestión médica basado en microservicios Spring Boot (backend) y Astro + React (frontend), desplegado en AWS EKS con pipeline CI/CD automatizado, análisis de calidad continuo y monitoreo centralizado.

---

## 📋 Tabla de contenidos

1. [Arquitectura general](#1-arquitectura-general)
2. [Cluster AWS EKS](#2-cluster-aws-eks)
3. [VPC, subredes y tags](#3-vpc-subredes-y-tags)
4. [Docker Hub](#4-docker-hub)
5. [Manifiestos Kubernetes — Backend](#5-manifiestos-kubernetes--backend)
6. [Manifiestos Kubernetes — Frontend](#6-manifiestos-kubernetes--frontend)
7. [Pipeline CI/CD — Backend](#7-pipeline-cicd--backend)
8. [Pipeline CI/CD — Frontend](#8-pipeline-cicd--frontend)
9. [GitHub Secrets](#9-github-secrets)
10. [Branch Protection (Ruleset)](#10-branch-protection-ruleset)
11. [SonarCloud — Backend](#11-sonarcloud--backend)
12. [SonarCloud — Frontend](#12-sonarcloud--frontend)
13. [CloudWatch — Monitoreo y Dashboard](#13-cloudwatch--monitoreo-y-dashboard)
14. [Evidencias del despliegue](#14-evidencias-del-despliegue)

---

## 1. Arquitectura general

RedNorte está compuesto por **8 servicios** desplegados en un único cluster EKS bajo el namespace `rednorte`:

| Capa | Servicio | Tecnología | Puerto |
|---|---|---|---|
| Frontend | `frontend` | Astro + React + nginx | 80 |
| Entrada | `bff-service` | Spring Boot | 8085 |
| Entrada | `gateway-service` | Spring Boot | 8082 |
| Descubrimiento | `eureka-server` | Spring Cloud Eureka | 8761 |
| Negocio | `auth-service` | Spring Boot + JWT | 8084 |
| Negocio | `user-service` | Spring Boot | 8081 |
| Negocio | `appointment-service` | Spring Boot | 8083 |
| Negocio | `notification-service` | Spring Boot | 8086 |
| Infra | `rabbitmq` | RabbitMQ | 5672 / 15672 |
| Infra | `redis` | Redis | 6379 |
| Datos | Supabase PostgreSQL | externo (pooler) | 5432 |

> 📸 **[Diagrama de arquitectura general / VPC completo]**
![Diagrama de arquitectura general ](/docs/vpc-cluster1.png)

> 📸 **[kubectl get pods -n rednorte — todos los pods Running]**
![kubectl get pods](/docs/pods-activos.png)
---

## 2. Cluster AWS EKS

| Parámetro | Valor |
|---|---|
| Nombre del cluster | `ClusterRedNorte` |
| Región | `us-east-1` |
| Versión de Kubernetes | `v1.35` |
| Namespace | `rednorte` |
| Node Group | `GrupoNodosRedNorte` |
| IAM Role (cluster y nodos) | `LabRole` |
| Cantidad de nodos | 3 |
| Pods máximos por nodo | 17 |
| CPU por nodo | 2 vCPU (1930m allocatable) |
| Memoria por nodo | ~3.8 GB (3.2 GB allocatable) |

### Estrategia de despliegue

Todos los Deployments usan `RollingUpdate` con `maxSurge: 0` y `maxUnavailable: 1`, evitando que se creen pods nuevos antes de eliminar los viejos (previene saturación de nodos). Además, `revisionHistoryLimit: 2` limita los ReplicaSets antiguos acumulados.

> 📸 **[EKS → Clusters → ClusterRedNorte, vista general]**
![EKS → Clusters → ClusterRedNorte](/docs/cluster1.png)
![EKS → Clusters → ClusterRedNorte](/docs/cluster2.png)

> 📸 **[EKS → Node Groups → GrupoNodosRedNorte]**
![EKS → Node Groups](/docs/nodos-cluster.png)



---

## 3. VPC, subredes y tags

```
VPC — 10.0.0.0/16 (us-east-1)
├── Subred pública  → Load Balancers (ALB)
│   ├── 10.0.0.0/24 (us-east-1a)
│   └── 10.0.1.0/24 (us-east-1b)
└── Subred privada  → Nodos EKS + Pods
    ├── 10.0.12.0/24 (us-east-1a)
    └── 10.0.25.0/24 (us-east-1b)
```

- **Subred pública** → expone los ALB de `frontend-service`, `bff-service` y `gateway-service` en el puerto 80.
- **Subred privada** → aloja los nodos EC2 y todos los pods. Sin IP pública. Egress hacia Supabase vía NAT Gateway.

### Tags obligatorios en subredes públicas

Sin estos tags, los Services tipo `LoadBalancer` quedan en `<pending>` indefinidamente:

```bash
aws ec2 create-tags \
  --resources <subnet-id-1> <subnet-id-2> \
  --tags Key=kubernetes.io/role/elb,Value=1 \
         Key=kubernetes.io/cluster/ClusterRedNorte,Value=shared \
  --region us-east-1
```

| Tag | Valor | Propósito |
|---|---|---|
| `kubernetes.io/role/elb` | `1` | Permite alojar ALBs externos |
| `kubernetes.io/cluster/ClusterRedNorte` | `shared` | Asocia la subred al cluster |

> 📸 **[VPC → Subnets, lista de subredes públicas y privadas]**
![VPC → Subnets, lista de subredes públicas y privadas](/docs/vpc-cluster2.png)

> 📸 **[Tags de la subred pública en EC2 → Subnets]**
![Tags de la subred pública en EC2 → Subnets](/docs/tag-public1.png)
![Tags de la subred pública en EC2 → Subnets](/docs/tag-public2.png)

> 📸 **[kubectl get services -n rednorte — EXTERNAL-IP asignada]**
![kubectl get services](/docs/Services-running.png)

---

## 4. Docker Hub

Las imágenes se almacenan en **Docker Hub** (usuario `gonzalo25u`) en lugar de ECR, ya que el rol `voclabs` del laboratorio no permite `ecr:CreateRepository`.

| Repositorio | Contenido |
|---|---|
| `gonzalo25u/rednorte-eureka-server` | Servidor de descubrimiento |
| `gonzalo25u/rednorte-auth-service` | Autenticación JWT |
| `gonzalo25u/rednorte-user-service` | Gestión de usuarios |
| `gonzalo25u/rednorte-appointment-service` | Gestión de citas |
| `gonzalo25u/rednorte-gateway-service` | API Gateway |
| `gonzalo25u/rednorte-bff-service` | Backend for Frontend |
| `gonzalo25u/rednorte-notification-service` | Notificaciones |
| `gonzalo25u/rednorte-frontend` | Astro + React + nginx |

Cada imagen se etiqueta con el **SHA del commit de Git**, garantizando trazabilidad exacta entre código desplegado e imagen.

> 📸 **[Docker Hub — lista de repositorios]**
![Docker Hub — lista de repositorios](/docs/repositorios-dockerhub.png)

> 📸 **[Tags de imágenes con SHA del commit]**
![Tags de imágenes con SHA del commit](/docs/sha.png)

---

## 5. Manifiestos Kubernetes — Backend

Ejemplo representativo (`k8s/bff-service.yml`):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bff-service
  namespace: rednorte
  labels:
    app: bff-service
spec:
  replicas: 1
  revisionHistoryLimit: 2
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 0
      maxUnavailable: 1
  selector:
    matchLabels:
      app: bff-service
  template:
    metadata:
      labels:
        app: bff-service
    spec:
      containers:
      - name: bff-service
        image: gonzalo25u/rednorte-bff-service:${IMAGE_TAG}
        ports:
        - containerPort: 8085
        envFrom:
        - configMapRef:
            name: rednorte-config
        - secretRef:
            name: rednorte-secrets
---
apiVersion: v1
kind: Service
metadata:
  name: bff-service
  namespace: rednorte
spec:
  type: LoadBalancer
  selector:
    app: bff-service
  ports:
  - port: 80
    targetPort: 8085
```

### ConfigMap — variables no sensibles (`k8s/configmap.yml`)

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: rednorte-config
  namespace: rednorte
data:
  DB_HOST: "aws-1-sa-east-1.pooler.supabase.com"
  DB_PORT: "5432"
  DB_NAME: "postgres"
  RABBITMQ_HOST: "rabbitmq-service"
  EUREKA_URL: "http://eureka-server-service:8761/eureka/"
  GATEWAY_URL: "http://gateway-service:8082"
  SUPABASE_URL: "https://pfprycndvkdxuzvgjqol.supabase.co"
```

> 📸 **[carpeta k8s/ del backend en el repositorio]**
![carpeta k8s/ del backend](/docs/k8s.png)


---

## 6. Manifiestos Kubernetes — Frontend

`k8s/frontend.yml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
  namespace: rednorte
  labels:
    app: frontend
spec:
  replicas: 1
  revisionHistoryLimit: 2
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 0
      maxUnavailable: 1
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
      - name: frontend
        image: gonzalo25u/rednorte-frontend:${IMAGE_TAG}
        ports:
        - containerPort: 80
---
apiVersion: v1
kind: Service
metadata:
  name: frontend-service
  namespace: rednorte
spec:
  type: LoadBalancer
  selector:
    app: frontend
  ports:
  - port: 80
    targetPort: 80
```

El frontend es un sitio **estático** (Astro `output: static`) servido por **nginx**, que recibe la URL del BFF en tiempo de build mediante `--build-arg PUBLIC_API_URL`.

> 📸 **[carpeta k8s/ del frontend en el repositorio]**
![carpeta k8s/ del frontend](/docs/k8s-front.png)

---

## 7. Pipeline CI/CD — Backend

`.github/workflows/deploy.yml` se activa con cada `push` a `master`.

```
git push origin master
        ↓
1. Checkout código
2. Setup Java 21
3. Tests (JUnit 5 + Mockito) + cobertura Jacoco
        ↓
4. Análisis SonarCloud + Quality Gate
        ↓ (se detiene si el Quality Gate falla)
5. Login a Docker Hub + credenciales AWS
6. mvn package + docker build + docker push (×7 servicios)
7. kubectl apply — namespace, configmap, secrets
8. kubectl apply — redis, rabbitmq
9. kubectl apply — microservicios con IMAGE_TAG=SHA
10. kubectl rollout status (verificación)
11. Métricas a CloudWatch (duración, éxito/fallo)
```

Fragmento clave — Quality Gate bloqueante:

```yaml
- name: Análisis SonarCloud — appointment-service
  uses: SonarSource/sonarcloud-github-action@master
  with:
    projectBaseDir: appointment-service
    args: >
      -Dsonar.organization=gonzalo25u
      -Dsonar.projectKey=Gonzalo25U_Backend-Rednorte-FsIII
      -Dsonar.qualitygate.wait=true
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

Fragmento clave — métricas a CloudWatch:

```yaml
- name: Registrar métricas en CloudWatch
  run: |
    DEPLOY_END=$(date +%s)
    DEPLOY_TIME=$((DEPLOY_END - DEPLOY_START))
    aws cloudwatch put-metric-data \
      --namespace "RedNorte/Deployments" \
      --metric-name "DeploymentDuration" \
      --value $DEPLOY_TIME --unit Seconds \
      --region us-east-1
```

> 📸 **[Pipeline backend exitoso en GitHub Actions]**
![pipeline backend exitoso en GitHub Actions](/docs/Action-back.png)

> 📸 **[Detalle de los pasos — Build, Push, Deploy]**
![detalle de los pasos — Build, Push, Deploy](/docs/Action-back2.png)

---

## 8. Pipeline CI/CD — Frontend

`.github/workflows/deploy.yml` se activa con cada `push` a `main`.

```
git push origin main
        ↓
1. Checkout código
2. Setup Node 22
3. npm ci + Vitest --coverage
        ↓
4. Análisis SonarCloud + Quality Gate
        ↓ (se detiene si el Quality Gate falla)
5. Login a Docker Hub + credenciales AWS
6. docker build --build-arg PUBLIC_API_URL=... + docker push
7. kubectl apply — frontend.yml con IMAGE_TAG=SHA
8. kubectl rollout status
9. Métricas a CloudWatch
```

Dockerfile del frontend (multi-stage):

```dockerfile
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG PUBLIC_API_URL
RUN PUBLIC_API_URL=$PUBLIC_API_URL npm run build

FROM nginx:alpine AS runner
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

> 📸 **[Pipeline frontend exitoso en GitHub Actions]**
![Pipeline frontend](/docs/Action-front.png)

> 📸 **[Detalle de los pasos — Tests, Build, Push, Deploy]**
![Detalle de los pasos](/docs/Action-front2.png)

---

## 9. GitHub Secrets

Ninguna credencial está escrita en el código fuente. Todas se inyectan vía GitHub Secrets:

| Secret | Repositorio | Propósito |
|---|---|---|
| `AWS_ACCESS_KEY_ID` | Backend + Frontend | Autenticación AWS |
| `AWS_SECRET_ACCESS_KEY` | Backend + Frontend | Autenticación AWS |
| `AWS_SESSION_TOKEN` | Backend + Frontend | Token temporal (laboratorio) |
| `DOCKERHUB_USERNAME` | Backend + Frontend | Usuario Docker Hub |
| `DOCKERHUB_TOKEN` | Backend + Frontend | Access Token Docker Hub |
| `DB_USERNAME` | Backend | Usuario pooler Supabase |
| `DB_PASSWORD` | Backend | Contraseña PostgreSQL |
| `JWT_SECRET` | Backend | Firma de tokens JWT |
| `SUPABASE_ANON_KEY` | Backend | Clave pública Supabase |
| `SUPABASE_SERVICE_KEY` | Backend | Clave de servicio Supabase |
| `SONAR_TOKEN` | Backend + Frontend | Autenticación SonarCloud |
| `PUBLIC_API_URL` | Frontend | URL del BFF inyectada en build |

Los Secrets de Kubernetes (`k8s/secrets.yml`) contienen solo placeholders en el repositorio:

```yaml
stringData:
  DB_USERNAME: "${DB_USERNAME}"
  DB_PASSWORD: "${DB_PASSWORD}"
  JWT_SECRET: "${JWT_SECRET}"
```

El pipeline los reemplaza en tiempo de ejecución:

```bash
sed -i "s|\${DB_PASSWORD}|${{ secrets.DB_PASSWORD }}|g" k8s/secrets.yml
kubectl apply -f k8s/secrets.yml
```

Los pods leen las credenciales mediante `envFrom.secretRef`, montadas como variables de entorno al arrancar el contenedor — nunca quedan expuestas en logs ni en el repositorio.

> 📸 **[GitHub Secrets del backend (nombres visibles, valores ocultos)]**
![GitHub Secrets del backend](/docs/Secrets-back.png)

> 📸 **[GitHub Secrets del frontend]**
![GitHub Secrets del frontend](/docs/Secrets-front.png)

> 📸 **[Secrets.yml]**
![Texto alternativo](/docs/Secrets-yml.png)

---

## 10. Branch Protection (Ruleset)

Se configuró un **Ruleset** en GitHub para proteger la rama `master` (backend) y `main` (frontend), bloqueando push directo y exigiendo Pull Request con checks aprobados.

### Configuración

- **Target branches:** `master` (backend) / `main` (frontend)
- **Require a pull request before merging:** ✅
- **Require status checks to pass:** ✅ (`Tests y Análisis de Calidad`, `Build, Push y Deploy`)
- **Block force pushes:** ✅

### Evidencia de funcionamiento

Al intentar `git push origin master` directamente, GitHub rechaza el push:

```
! [remote rejected] master -> master (push declined due to repository rule violations)
error: failed to push some refs
```

Esto demuestra que **ningún código llega a producción sin pasar por revisión y por los checks automatizados del pipeline** (tests + SonarCloud Quality Gate).

# Configuración del ruleset

> 📸 **[Ruleset Backend]**
![Backend](/docs/RuleSet-back-create.png)
![Backend](/docs/RuleSet-back-create2.png)

> 📸 **[Ruleset Frontend]**
![Frontend](/docs/RuleSet-front-create.png)
![Frontend](/docs/RuleSet-front-create2.png)

> 📸 **[Push directo rechazado por el ruleset]**
![Push directo rechazado](/docs/no-push.png)

> 📸 **[Pull Request con checks pasando antes del merge]**
![Pull Request con checks pasando](/docs/Ruleset.png)

> 📸 **[Pull Request con checks Listos]**
![Pull Request con checks pasando](/docs/Ruleset-ok.png)

---

## 11. SonarCloud — Backend

| Parámetro | Valor |
|---|---|
| Organización | `gonzalo25u` |
| Project Key | `Gonzalo25U_Backend-Rednorte-FsIII` |
| Servicios analizados | `appointment-service`, `user-service` |
| Herramientas de testing | JUnit 5, Mockito, Jacoco |
| Cobertura mínima (Jacoco) | 70% |
| Quality Gate | Sonar way |

Configuración de Jacoco en `pom.xml`:

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.11</version>
  <executions>
    <execution>
      <id>check</id>
      <goals><goal>check</goal></goals>
      <configuration>
        <rules>
          <rule>
            <element>BUNDLE</element>
            <limits>
              <limit>
                <counter>LINE</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.70</minimum>
              </limit>
            </limits>
          </rule>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

Si el Quality Gate falla, el scanner retorna `exit code 3` y el job `test-and-quality` falla, bloqueando el job `build-and-deploy` (que depende de él vía `needs:`).

> 📸 **[Dashboard SonarCloud backend — Quality Gate Passed]**
![Dashboard SonarCloud backend](/docs/Sonar-back.png)
![Dashboard SonarCloud backend](/docs/Sonar-back2.png)



---

## 12. SonarCloud — Frontend

| Parámetro | Valor |
|---|---|
| Organización | `gonzalo25u` |
| Project Key | `Gonzalo25U_Frontend-Rednorte-FsIII` |
| Herramienta de testing | Vitest + @vitest/coverage-v8 |
| Cobertura alcanzada | 80.5% |
| Quality Gate | Sonar way |

Configuración de coverage en `vitest.config.js`:

```javascript
export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: ["./src/test/setup.js"],
    include: ["src/**/*.test.{js,jsx}"],
    coverage: {
      provider: "v8",
      reporter: ["lcov", "text"],
      include: ["src/**/*.{js,jsx}"],
      exclude: ["src/test/**", "src/**/*.test.{js,jsx}"]
    }
  },
});
```

Se escribieron tests unitarios para containers (`LoginContainer`, `UserListContainer`, `DoctorContainer`, `PacienteContainer`), componentes modales, servicios (`authService`) y utilidades (`api.js`, `auth.js`), elevando la cobertura desde 19% hasta superar el umbral del 80% requerido por el Quality Gate.

> 📸 **[Dashboard SonarCloud frontend — Quality Gate Passed]**
![Dashboard SonarCloud Frontend](/docs/Sonar-front.png)
![Dashboard SonarCloud Frontend](/docs/Sonar-front2.png)


---

## 13. CloudWatch — Monitoreo y Dashboard

### Métricas personalizadas

El pipeline envía métricas propias al namespace `RedNorte/Deployments` en cada ejecución:

```bash
aws cloudwatch put-metric-data \
  --namespace "RedNorte/Deployments" \
  --metric-name "DeploymentDuration" \
  --value $DEPLOY_TIME --unit Seconds \
  --region us-east-1

aws cloudwatch put-metric-data \
  --namespace "RedNorte/Deployments" \
  --metric-name "DeploymentSuccess" \
  --value 1 --unit Count \
  --region us-east-1
```

Si el despliegue falla (`if: failure()`), se registra `DeploymentSuccess = 0`, permitiendo distinguir despliegues exitosos de fallidos directamente en el dashboard.

### Dashboard — RedNorte-Dashboard

| Widget | Métrica | Fuente |
|---|---|---|
| Tiempo de despliegue | `DeploymentDuration` | Pipeline (custom) |
| Despliegues exitosos/fallidos | `DeploymentSuccess` | Pipeline (custom) |
| CPU de nodos EKS | `CPUUtilization` | AWS/EC2 (Auto Scaling Group) |
| Network In/Out | `NetworkIn`, `NetworkOut` | AWS/EC2 (Auto Scaling Group) |

```bash
aws cloudwatch put-dashboard \
  --dashboard-name "RedNorte-Dashboard" \
  --dashboard-body '{ "widgets": [ ... ] }' \
  --region us-east-1
```

> 📸 **[CloudWatch , vista completa]**
![CloudWatch](/docs/dash1.png)

---

## 14. Evidencias del despliegue

### Estado final del cluster

```bash
kubectl get pods -n rednorte
```

| Pod | Estado |
|---|---|
| frontend | Running |
| bff-service | Running |
| gateway-service | Running |
| eureka-server | Running |
| auth-service | Running |
| user-service | Running |
| appointment-service | Running |
| notification-service | Running |
| rabbitmq | Running |
| redis | Running |

### URLs públicas

| Servicio | URL |
|---|---|
| Frontend | `http://a918aa7d71946440f9cad2d989c4d0d4-864776264.us-east-1.elb.amazonaws.com` |
| BFF | `http://ae8cced15e30a434ab64a24e0f35ca7c-562956923.us-east-1.elb.amazonaws.com` |
| Gateway | `http://a24fa6f8d8b734c259412bde4df58a7f-781657377.us-east-1.elb.amazonaws.com` |

### Verificación funcional end-to-end

```bash
curl -X POST http://<bff-url>/bff/auth/login \
  -H "Content-Type: application/json" \
  -d '{"rut":"11111111-1","password":"********"}'

# {"token":"eyJhbGciOiJIUzI1NiJ9..."}
```

Se verificó login exitoso, listado/eliminación de usuarios desde el panel de administración, y persistencia de datos en Supabase PostgreSQL.

> 📸 **[Frontend cargado en el navegador — pantalla de login]**
![Frontend cargado en el navegador](/docs/front1.png)
![Frontend cargado en el navegador](/docs/front2.png)
![Frontend cargado en el navegador](/docs/front3.png)
![Frontend cargado en el navegador](/docs/front4.png)

> 📸 **[Pods Rednorte — todos en Running sin reinicios]**
![Pods Rednorte](/docs/pods-activos.png)

> 📸 **[Respuesta exitosa del login vía curl (datos censurados)]**
![Respuesta exitosa del login vía curl](/docs/token-longin-terminal.png)