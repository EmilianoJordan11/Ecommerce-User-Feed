# 📝 User Feed Microservice

Microservicio de reseñas para el sistema de ecommerce. Permite a los usuarios crear, consultar, modificar y eliminar reseñas sobre artículos que hayan comprado.

## 🚀 Tecnologías

- Java 17
- Spring Boot 3.5.6
- PostgreSQL
- RabbitMQ
- Maven

## 📋 Requisitos previos

- Java 17+
- PostgreSQL
- RabbitMQ
- Microservicio Auth (authgo)
- Microservicio Orders (ordersgo)

## 🛠️ Preparación del ambiente

Configura las URLs y credenciales en `src/main/resources/application.yml` según tu entorno:

```yaml
server:
  port: 3005                    # Puerto de este microservicio

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/user-feed
    username: postgres
    password: tu_password
  
  rabbitmq:
    host: localhost
    port: 5672
    username: tu_usuario
    password: tu_password

security:
  auth-server-url: http://localhost:3000    # URL del microservicio Auth

orders-server-url: http://localhost:3004    # URL del microservicio Orders
```

## 🏃 Ejecución

```bash
# Compilar
./mvnw clean package

# Ejecutar
./mvnw spring-boot:run
```

## 🔗 Endpoints

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/v1/feed/{id}` | Obtener reseña por ID | ❌ |
| GET | `/v1/feed/article/{articleId}` | Reseñas de un artículo | ❌ |
| GET | `/v1/feed/my-feeds` | Mis reseñas | ✅ |
| POST | `/v1/feed` | Crear reseña | ✅ |
| PUT | `/v1/feed/{id}` | Actualizar reseña | ✅ |
| DELETE | `/v1/feed/{id}` | Eliminar reseña | ✅ |

## 📦 Crear reseña

```bash
curl -X POST http://localhost:3005/v1/feed \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "articleId": "6927da17c3255b97d5a1acbb",
    "comment": "Excelente producto",
    "rating": 5
  }'
```

## 🔐 Seguridad

- Autenticación via JWT validado contra el servicio Auth
- Cache local de tokens con expiración automática
- Invalidación de tokens via RabbitMQ (evento logout)

## 📊 Reglas de negocio

- Solo puedes reseñar artículos que hayas comprado
- Una sola reseña por artículo por usuario
- Rating entre 1 y 5
- Solo el dueño puede modificar su reseña
- Solo el dueño o admin puede eliminar una reseña

## 🐰 RabbitMQ

Escucha el exchange `auth` (fanout) para invalidar tokens cuando un usuario hace logout.

## 📄 Documentación

Ver [DEFINICION.md](DEFINICION.md) para casos de uso detallados y especificación completa de la API.
