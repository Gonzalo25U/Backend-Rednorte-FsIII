# 🏥 RedNorte FSIII - Backend

**Plataforma de Gestión de Listas de Espera Hospitalarias - Microservicios Backend**

Backend de la solución tecnológica para RedNorte basado en arquitectura de microservicios con Spring Boot, que gestiona autenticación, usuarios y citas médicas con alta disponibilidad y escalabilidad.

---

## 📋 Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Requisitos Previos](#requisitos-previos)
- [Instalación y Configuración](#instalación-y-configuración)
- [Ejecución](#ejecución)
- [Servicios y Puertos](#servicios-y-puertos)
- [Variables de Entorno](#variables-de-entorno)
- [Endpoints](#endpoints)
- [Testing](#testing)
- [Docker](#docker)
- [Troubleshooting](#troubleshooting)
- [Contribuir](#contribuir)
- [Licencia](#licencia)

---

## 📱 Descripción General

RedNorte FSIII Backend es una **solución de microservicios escalable** construida con **Spring Boot 3.2.5** y **Java 21 LTS**, que implementa patrones empresariales modernos como API Gateway, Service Discovery y Circuit Breaker.

**Características principales:**
- ✅ 6 microservicios especializados (Eureka, Auth, User, Appointment, Gateway, BFF)
- ✅ Autenticación con JWT y validación de roles
- ✅ Persistencia con PostgreSQL y JPA
- ✅ Service Discovery automático con Eureka
- ✅ Comunicación inter-servicios con WebClient
- ✅ Validación de datos robusta (RUT, contraseñas)
- ✅ Containerización con Docker
- ✅ Testing unitario con JUnit 5 y Mockito

---

## 🏗️ Arquitectura

### Diagrama de Microservicios

```
┌────────────────────────────────────────────────┐
│          Frontend (http://localhost:3000)      │
└─────────────────────┬──────────────────────────┘
                      │
        HTTP + JWT Token (Bearer)
                      │
┌─────────────────────▼──────────────────────────┐
│   BFF Service (Port 8085)                      │
│   Orquestación de llamadas, adaptación de datos│
└─────────────────────┬──────────────────────────┘
                      │
         ┌────────────▼──────────────┐
         │  API Gateway (Port 8082)  │
         │  - Enrutamiento central   │
         │  - Load balancing         │
         │  - Throttling             │
         └────────────┬──────────────┘
                      │
    ┌─────────────────┼─────────────────┬──────────────┐
    │                 │                 │              │
┌───▼────┐     ┌─────▼────┐      ┌────▼────┐    ┌───▼──────┐
│ Auth   │     │ User     │      │Appoint. │    │ Eureka   │
│Service │     │ Service  │      │ Service │    │ Server   │
│8084    │     │ 8081     │      │ 8083    │    │ 8761     │
│        │     │          │      │         │    │          │
│- Login │     │- CRUD    │      │- Crear  │    │-Registry │
│- JWT   │     │- Roles   │      │  citas  │    │-Health   │
└────────┘     │- RUT     │      │- Cambiar│    │  check   │
               │- Password│      │  estado │    └──────────┘
               └──────────┘      │- Notas  │
                                 │  médicas│
                                 └─────────┘
                                      │
                                 PostgreSQL
                                 Database
```

---

## 🛠️ Tecnologías

| Categoría | Tecnología | Versión | Propósito |
|-----------|-----------|---------|----------|
| **Framework** | Spring Boot | 3.2.5 | Framework web empresarial |
| **Cloud** | Spring Cloud | 2023.0.1 | Microservicios y discovery |
| **Java** | JDK | 21 LTS | Runtime con 8 años soporte |
| **Base de Datos** | PostgreSQL | 12+ | BD relacional |
| **ORM** | Spring Data JPA | 3.2.5 | Mapeo objeto-relacional |
| **Seguridad** | Spring Security | 3.2.5 | Autenticación/Autorización |
| **JWT** | JJWT | 0.12.3 | Tokens seguros |
| **Build** | Maven | 3.9.x | Gestión de dependencias |
| **Containerización** | Docker | 24.x | Empaquetamiento |
| **Testing** | JUnit 5 + Mockito | Latest | Tests unitarios |

---

## 📦 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- **Java Development Kit (JDK) 21** ([Descargar](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html))
- **Maven 3.9+** ([Descargar](https://maven.apache.org/download.cgi))
- **PostgreSQL 12+** ([Descargar](https://www.postgresql.org/download/))
- **Docker y Docker Compose** ([Descargar](https://www.docker.com/products/docker-desktop)) - *Opcional*
- **Git** para clonar el repositorio

### Verificar Instalación

```bash
java -version              # Debe mostrar Java 21
mvn -version              # Debe mostrar Maven 3.9+
psql --version            # Debe mostrar PostgreSQL 12+
docker --version          # Debe mostrar Docker 24+
```

---

## 💾 Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone https://github.com/rednorte/Backend-Rednorte-FsIII.git
cd Backend-Rednorte-FsIII
```

### 2. Configurar PostgreSQL

#### Opción A: PostgreSQL Local

```bash
# 1. Iniciar PostgreSQL (asume instalación en el sistema)
# Windows:
net start PostgreSQL-x64-15

# macOS:
brew services start postgresql

# Linux (Ubuntu/Debian):
sudo systemctl start postgresql
```

#### Opción B: PostgreSQL en Docker (Recomendado)

```bash
# Crear contenedor PostgreSQL
docker run --name rednorte-postgres \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_DB=rednorte \
  -p 5432:5432 \
  -d postgres:15-alpine

# Verificar que está corriendo
docker ps | grep rednorte-postgres
```

### 3. Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto:

```env
# ===== BASE DE DATOS =====
DB_HOST=localhost
DB_PORT=5432
DB_NAME=rednorte
DB_USERNAME=postgres
DB_PASSWORD=password

# ===== JWT =====
JWT_SECRET=tu-secreto-super-seguro-minimo-32-caracteres-aleatorios

# ===== JAVA OPTIONS =====
JAVA_TOOL_OPTIONS=-Djava.net.preferIPv4Stack=true

# ===== LOGGING (opcional) =====
LOGGING_LEVEL_COM_REDNORTE=DEBUG
```

### 4. Crear Base de Datos (Si no usas Docker)

```bash
# Conectar a PostgreSQL
psql -U postgres -h localhost

# En la consola de psql:
CREATE DATABASE rednorte;
\q  # Salir
```

---

## 🚀 Ejecución

### Opción 1: Docker Compose (Recomendado para Desarrollo)

```bash
# Iniciar todos los servicios (Eureka, Auth, User, Appointment, Gateway, BFF + PostgreSQL)
docker-compose up -d

# Esperar ~30 segundos para que todos inicien

# Verificar que están corriendo
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f

# Detener servicios
docker-compose down
```

### Opción 2: Ejecución Individual (Desarrollo Local)

#### 1. Iniciar Eureka Server (Service Discovery)

```bash
cd eureka-server
mvn spring-boot:run
# Acceder a http://localhost:8761 para ver el dashboard
```

#### 2. Iniciar Auth Service (en otra terminal)

```bash
cd auth-service
mvn spring-boot:run
# Escucha en http://localhost:8084
```

#### 3. Iniciar User Service (en otra terminal)

```bash
cd user-service
mvn spring-boot:run
# Escucha en http://localhost:8081
```

#### 4. Iniciar Appointment Service (en otra terminal)

```bash
cd appointment-service
mvn spring-boot:run
# Escucha en http://localhost:8083
```

#### 5. Iniciar API Gateway (en otra terminal)

```bash
cd gateway-service
mvn spring-boot:run
# Escucha en http://localhost:8082
```

#### 6. Iniciar BFF Service (en otra terminal)

```bash
cd bff-service
mvn spring-boot:run
# Escucha en http://localhost:8085
```

---

## 📡 Servicios y Puertos

| Servicio | Puerto | Descripción | URL de Acceso |
|----------|--------|-------------|---------------|
| Eureka Server | 8761 | Service Discovery | http://localhost:8761 |
| Auth Service | 8084 | Autenticación JWT | http://localhost:8084 |
| User Service | 8081 | Gestión de usuarios | http://localhost:8081 |
| Appointment Service | 8083 | Gestión de citas | http://localhost:8083 |
| API Gateway | 8082 | Enrutamiento central | http://localhost:8082 |
| BFF Service | 8085 | Backend For Frontend | http://localhost:8085 |
| PostgreSQL | 5432 | Base de datos | localhost:5432 |

---

## 🔑 Variables de Entorno

### Archivo `.env` (Crear en la raíz)

```env
# ===== DATABASE CONFIG =====
DB_HOST=localhost                    # Host de PostgreSQL
DB_PORT=5432                         # Puerto de PostgreSQL
DB_NAME=rednorte                     # Nombre de la BD
DB_USERNAME=postgres                 # Usuario PostgreSQL
DB_PASSWORD=password                 # Contraseña PostgreSQL

# ===== JWT CONFIG =====
JWT_SECRET=secreto-super-seguro-minimo-32-caracteres
# Genera uno seguro con: openssl rand -base64 32

# ===== LOGGING =====
LOGGING_LEVEL_COM_REDNORTE=DEBUG     # INFO, DEBUG, WARN, ERROR
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=INFO

# ===== JVM OPTIONS =====
JAVA_TOOL_OPTIONS=-Djava.net.preferIPv4Stack=true
```

### Generar JWT Secret Seguro

```bash
# macOS/Linux:
openssl rand -base64 32

# Windows (PowerShell):
$secret = -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | % {[char]$_}); $secret

# Online: https://www.random.org/cgi-bin/randbytes?nbytes=32&format=h
```

---

## 📚 Endpoints

### Documentación Completa

Consulta `DOCUMENTACION.md` en la raíz del proyecto para una lista exhaustiva de endpoints.

### Quick Reference

#### Auth Service

```bash
# Login
curl -X POST http://localhost:8085/bff/auth/login \
  -H "Content-Type: application/json" \
  -d '{"rut":"12345678-1","password":"password123"}'

# Respuesta:
# { "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
```

#### User Service (Requiere Token)

```bash
# Listar usuarios
curl -X GET http://localhost:8082/api/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Crear usuario
curl -X POST http://localhost:8082/api/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "rut":"98765432-K",
    "name":"Dr. Juan Pérez",
    "role":"DOCTOR",
    "password":"password123"
  }'

# Obtener usuario por RUT
curl -X GET http://localhost:8082/api/users/rut/12345678-1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Appointment Service (Requiere Token)

```bash
# Crear cita
curl -X POST http://localhost:8082/api/appointments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientRut":"12345678-1",
    "doctorRut":"98765432-K"
  }'

# Listar citas de paciente
curl -X GET http://localhost:8082/api/appointments/patient/12345678-1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Cambiar prioridad de cita
curl -X PUT http://localhost:8082/api/appointments/1/priority?priority=A \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🧪 Testing

### Ejecutar Tests

```bash
# Tests de un módulo específico
cd user-service
mvn test

# Tests de todo el backend
mvn test --projects eureka-server,auth-service,user-service,appointment-service,gateway-service,bff-service

# Tests con cobertura (JaCoCo)
mvn clean test jacoco:report
# Ver reporte: target/site/jacoco/index.html
```

### Estructura de Tests

```
src/test/java/com/rednorte/*/
├── controller/    # Tests de controladores
├── service/       # Tests de servicios
├── utils/         # Tests de utilidades
└── integration/   # Tests de integración
```

### Ejemplo de Test Unitario

```java
@SpringBootTest
class UserServiceTest {
  
  @MockBean
  private UserRepository userRepository;
  
  @InjectMocks
  private UserService userService;
  
  @Test
  void shouldCreateUserWithValidRut() {
    // Arrange
    User user = new User();
    user.setRut("12345678-1");
    user.setName("Juan Pérez");
    user.setRole(UserRole.PACIENTE);
    
    when(userRepository.save(any())).thenReturn(user);
    
    // Act
    String[] result = userService.create(user);
    
    // Assert
    assertNotNull(result);
    verify(userRepository, times(1)).save(any());
  }
  
  @Test
  void shouldRejectInvalidRut() {
    User user = new User();
    user.setRut("invalid-rut");
    
    assertThrows(BadRequestException.class, () -> userService.create(user));
  }
}
```

---

## 🐳 Docker

### Build Individual de Imagen

```bash
# Construir imagen de un servicio
cd user-service
docker build -t rednorte/user-service:1.0 .

# Ejecutar contenedor
docker run -d \
  --name user-service \
  -p 8081:8081 \
  -e DB_HOST=postgres \
  -e DB_PASSWORD=password \
  -e JWT_SECRET=tu-secreto \
  rednorte/user-service:1.0
```

### Compose Completo

```bash
# Iniciar todo
docker-compose up -d

# Ver logs
docker-compose logs -f auth-service

# Ejecutar comando en contenedor
docker-compose exec user-service mvn test

# Parar y limpiar
docker-compose down
docker volume prune
```

### Dockerfile Estándar

```dockerfile
FROM openjdk:21-slim

WORKDIR /app

COPY target/auth-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java","-jar","app.jar"]
```

---

## 🐛 Troubleshooting

### Problema: "Connection refused" en PostgreSQL

**Síntoma:** `java.sql.SQLException: Cannot connect to database`

**Soluciones:**
```bash
# 1. Verificar que PostgreSQL está corriendo
ps aux | grep postgres

# 2. Verificar credenciales en .env
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=postgres
DB_PASSWORD=password

# 3. Conectar directamente a BD
psql -U postgres -h localhost -d rednorte

# 4. Si usas Docker, verifica que el contenedor está corriendo
docker ps | grep postgres
```

---

### Problema: Puerto ya está en uso

**Síntoma:** `Address already in use :8081`

**Soluciones:**
```bash
# Encontrar proceso en puerto
lsof -i :8081  (macOS/Linux)
netstat -ano | findstr :8081  (Windows)

# Matar proceso
kill -9 <PID>  (macOS/Linux)
taskkill /PID <PID> /F  (Windows)

# O cambiar puerto en application.yml:
server:
  port: 8081  # Cambia a otro puerto
```

---

### Problema: Servicios no se registran en Eureka

**Síntoma:** Eureka dashboard muestra "No instances available"

**Causas:**
- Eureka Server no está corriendo
- Servicios tardan en registrarse (~10-30s)
- Configuración incorrecta de Eureka URL

**Soluciones:**
```bash
# 1. Verificar que Eureka está corriendo
curl http://localhost:8761/eureka/apps

# 2. Esperar unos segundos
sleep 30

# 3. Verificar logs del servicio
docker-compose logs user-service | grep Eureka

# 4. Verificar application.yml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
```

---

### Problema: Error de Validación de RUT

**Síntoma:** "RUT inválido. Formato requerido: xxxxxxxx-x"

**Causas:**
- Formato incorrecto (debe ser: 12345678-1)
- Dígito verificador incorrecto

**Soluciones:**
```java
// RUT válido: 12.345.678-1
// Formato esperado en API: 12345678-1

// Generador de RUT válido (para testing):
String generatedRut = "12345678-" + generateCheckDigit("12345678");
```

---

### Problema: JWT Token expirado

**Síntoma:** `401 Unauthorized: Invalid or expired token`

**Causas:**
- Token con más de 24 horas de antigüedad
- JWT_SECRET diferente entre servicios

**Soluciones:**
```bash
# 1. Obtener nuevo token
curl -X POST http://localhost:8085/bff/auth/login \
  -d '{"rut":"12345678-1","password":"password123"}'

# 2. Verificar que JWT_SECRET es igual en todos los servicios
grep JWT_SECRET .env

# 3. Cambiar tiempo de expiración en JwtUtil.java (en segundos)
.setExpiration(new Date(System.currentTimeMillis() + 86400000))  // 24 horas
```

---

### Problema: Base de datos no se crea automáticamente

**Síntoma:** `database "rednorte" does not exist`

**Soluciones:**
```bash
# 1. Crear manualmente
createdb -U postgres rednorte

# 2. Verificar que hibernate.ddl-auto = update
# En application.yml:
spring:
  jpa:
    hibernate:
      ddl-auto: update  # create | create-drop | update | validate

# 3. Forzar creación en Eureka (primero)
# Iniciar eureka-server primero, luego otros servicios
```

---

## 🤝 Contribuir

### Workflow de Desarrollo

1. **Crear rama de feature:**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/nueva-funcionalidad
   ```

2. **Hacer cambios y testar:**
   ```bash
   mvn clean test        # Tests unitarios
   mvn spring-boot:run  # Servidor local
   ```

3. **Commits descriptivos:**
   ```bash
   git commit -m "feat(user-service): agregar validación de email"
   git commit -m "test(appointment): aumentar cobertura a 75%"
   git commit -m "docs: actualizar endpoints"
   ```

4. **Push y Pull Request:**
   ```bash
   git push origin feature/nueva-funcionalidad
   # Crear PR en GitHub hacia develop
   ```

### Convención de Commits

```
<tipo>(<scope>): <mensaje>

Tipos: feat, fix, docs, test, refactor, ci, style, perf
Scope: user-service, auth-service, appointment-service, etc.

Ejemplos:
- feat(user-service): agregar endpoint para cambiar contraseña
- fix(auth-service): resolver bug de validación JWT
- test(appointment-service): aumentar cobertura a 80%
```

### Guía de Código

- Usar PascalCase para clases: `UserService`
- Usar camelCase para métodos y variables: `getUserByRut`
- Usar UPPER_SNAKE_CASE para constantes: `MAX_ATTEMPTS`
- Documentar métodos públicos con JavaDoc
- Máximo 100 caracteres por línea
- Indentación de 4 espacios

---

## 📁 Estructura de Carpetas

```
Backend-Rednorte-FsIII/
├── eureka-server/               # Service Discovery
│   ├── src/main/java/
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── pom.xml
│   └── Dockerfile
│
├── auth-service/                # Autenticación
│   ├── src/main/java/com/rednorte/auth_service/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── security/
│   │   ├── client/
│   │   ├── dto/
│   │   └── config/
│   ├── src/test/java/
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── pom.xml
│   └── Dockerfile
│
├── user-service/                # Gestión de usuarios
│   ├── src/main/java/com/rednorte/user_service/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── dto/
│   │   ├── security/
│   │   ├── utils/
│   │   ├── exception/
│   │   └── config/
│   ├── src/test/java/
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── pom.xml
│   └── Dockerfile
│
├── appointment-service/         # Gestión de citas
│   ├── src/main/java/com/rednorte/appointment_service/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── dto/
│   │   ├── enums/
│   │   ├── security/
│   │   ├── mapper/
│   │   ├── config/
│   │   └── client/
│   ├── src/test/java/
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── pom.xml
│   └── Dockerfile
│
├── gateway-service/             # API Gateway
│   ├── src/main/java/com/rednorte/gateway_service/
│   │   ├── config/
│   │   └── GatewayServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── pom.xml
│   └── Dockerfile
│
├── bff-service/                 # Backend For Frontend
│   ├── src/main/java/com/rednorte/bff_service/
│   │   ├── controller/
│   │   ├── security/
│   │   └── config/
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── pom.xml
│   └── Dockerfile
│
├── docker-compose.yml           # Orquestación Docker
├── pom.xml                      # Parent POM
├── .env                         # Variables de entorno
├── DOCUMENTACION.md             # Documentación completa
└── README.md                    # Este archivo
```

---

## 📊 Monitoreo y Logs

### Ver Logs en Tiempo Real

#### Docker Compose:
```bash
# Todos los servicios
docker-compose logs -f

# Un servicio específico
docker-compose logs -f user-service

# Últimas 100 líneas
docker-compose logs --tail=100
```

#### Localmente:
```bash
# Está en la salida de la terminal donde corriste mvn spring-boot:run
```

### Nivel de Logging

Configura en `application.yml`:

```yaml
logging:
  level:
    com.rednorte: DEBUG
    org.springframework.security: DEBUG
    org.springframework.data: DEBUG
    org.hibernate: INFO
```

---

## 📄 Licencia

Este proyecto es propiedad de RedNorte - Servicio Público de Salud. Consulta LICENSE.md para detalles.

---

## 📞 Soporte

- **Documentación completa:** Ver `DOCUMENTACION.md`
- **Issues:** [GitHub Issues](https://github.com/rednorte/Backend-Rednorte-FsIII/issues)
- **Email:** dev-team@rednorte.cl
- **Slack:** #proyecto-fsiii

---

## 📝 Notas de Versión

### v1.0.0 (11 de Mayo 2026)

- ✨ 6 microservicios funcionales
- ✨ Autenticación JWT
- ✨ Gestión de citas con prioridades A-F
- ✨ Validación de RUT con módulo 11
- ✨ Service Discovery con Eureka
- ✨ Tests unitarios (60% cobertura)
- ✨ Docker y Docker Compose

**Próximas fases:**
- 🔄 Aumentar cobertura a 80% (Junio)
- 🔄 Integración de notificaciones (Junio-Julio)
- 🔄 Despliegue en Render (Agosto)
- 🔄 Caché Redis (Septiembre+)

---

**Última actualización:** 11 de Mayo 2026  
**Estado:** En desarrollo (Fase 1)  
**Siguiente phase:** Mejora de tests y notificaciones (Junio 2026)
