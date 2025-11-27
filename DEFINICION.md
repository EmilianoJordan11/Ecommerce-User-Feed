# Microservicio de User Feed (Reseñas)

## Descripción

Microservicio que permite a los usuarios crear reseñas sobre artículos que hayan comprado en el sistema de ecommerce. Solo se permite una reseña por artículo por usuario, y el sistema valida automáticamente que el usuario tenga una orden con el artículo antes de permitir la creación.

## Casos de uso

### CU: Alta de Reseña

- Precondición: El usuario debe estar autenticado y debe haber comprado el artículo (tener al menos una orden que contenga el artículo).

- Camino normal:

  - El usuario autenticado proporciona un `articleId`, `rating` (1-5) y opcionalmente un `comment`.
  - El sistema valida que el usuario esté autenticado mediante JWT consultando al servicio Auth.
  - El sistema valida que no exista una reseña previa del mismo usuario para ese artículo.
  - El sistema consulta al servicio Orders para verificar que el usuario tenga al menos una orden con ese artículo.
  - El sistema valida que el rating esté entre 1 y 5.
  - Se crea la nueva reseña en la base de datos.
  - Se devuelve la reseña creada con código 201.

- Caminos alternativos:

  - Si el usuario no está autenticado, devuelve error 401.
  - Si ya existe una reseña del usuario para ese artículo, devuelve error 400.
  - Si el usuario no tiene ninguna orden con ese artículo, devuelve error 400.
  - Si el rating no está entre 1 y 5, devuelve error 400.

### CU: Modificación de Reseña

- Precondición: El usuario debe estar autenticado y ser el propietario de la reseña.

- Camino normal:

  - El usuario autenticado proporciona el ID de la reseña y los campos a actualizar (`rating`, `comment`).
  - El sistema valida que la reseña exista.
  - El sistema valida que el usuario sea el propietario de la reseña.
  - El sistema valida que los nuevos datos cumplan con las reglas de negocio.
  - Se actualiza la reseña en la base de datos.
  - Se devuelve la reseña actualizada.

- Caminos alternativos:

  - Si el usuario no está autenticado, devuelve error 401.
  - Si el usuario intenta modificar una reseña que no es suya, devuelve error 403.
  - Si la reseña no existe, devuelve error 404.
  - Si el rating no está entre 1 y 5, devuelve error 400.

### CU: Eliminación de Reseña

- Precondición: El usuario debe estar autenticado y ser el propietario de la reseña o administrador.

- Camino normal:

  - El usuario autenticado solicita eliminar una reseña específica por su ID.
  - El sistema valida que la reseña exista.
  - El sistema valida que el usuario sea el propietario de la reseña o tenga permisos de administrador.
  - Se elimina la reseña de la base de datos.
  - Se devuelve código 204 (No Content).

- Caminos alternativos:

  - Si el usuario no está autenticado, devuelve error 401.
  - Si el usuario no es el propietario ni administrador, devuelve error 403.
  - Si la reseña no existe, devuelve error 404.

### CU: Consulta de Reseñas

- Precondición: Ninguna para consultas públicas. Autenticación requerida para consultar reseñas propias.

- Camino normal:

  - **Por ID (público):** Se puede consultar cualquier reseña por su ID sin autenticación.
  - **Por Artículo (público):** Se pueden obtener todas las reseñas de un artículo específico sin autenticación.
  - **Mis Reseñas (privado):** El usuario autenticado puede consultar todas sus reseñas.

- Caminos alternativos:

  - Si la reseña no existe (consulta por ID), devuelve error 404.
  - Si el usuario no está autenticado (mis reseñas), devuelve error 401.

### CU: Consulta de Promedio de Calificaciones

- Precondición: Ninguna (endpoint público).

- Camino normal:

  - Se solicita el promedio de calificaciones de un artículo específico.
  - El sistema devuelve el promedio precalculado, la cantidad total de reseñas y la fecha de última actualización.

- Caminos alternativos:

  - Si el artículo no tiene reseñas, devuelve error 404.

- Nota: El promedio se precalcula automáticamente cada vez que se crea, modifica o elimina una reseña del artículo.

## Modelo de datos

**Feed**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | Identificador único (autoincremental) |
| userId | String | ID del usuario propietario de la reseña |
| articleId | String | ID del artículo reseñado |
| comment | String (TEXT) | Comentario de la reseña (opcional) |
| rating | Integer | Puntuación del 1 al 5 |
| createdAt | DateTime | Fecha de creación |
| updatedAt | DateTime | Fecha de última actualización |

**Índices:**

- Índice único compuesto: `{ userId, articleId }` - Previene reseñas duplicadas del mismo usuario al mismo artículo.

**ArticleRating**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| articleId | String (PK) | ID del artículo (clave primaria) |
| averageRating | Double | Promedio de calificaciones (1.0 - 5.0) |
| totalReviews | Integer | Cantidad total de reseñas del artículo |
| updatedAt | DateTime | Fecha de última actualización del promedio |

## Interfaz REST

### Crear Reseña

`POST /v1/feed`

**Headers**

- Authorization: Bearer {token}

**Body**

```json
{
  "articleId": "6927da17c3255b97d5a1acbb",
  "comment": "Excelente producto, muy recomendado",
  "rating": 5
}
```

**Response**

`201 CREATED`

```json
{
  "id": 1,
  "userId": "69279ef20cd0f9962d81ada1",
  "articleId": "6927da17c3255b97d5a1acbb",
  "comment": "Excelente producto, muy recomendado",
  "rating": 5,
  "createdAt": "2025-11-27T02:30:00",
  "updatedAt": null
}
```

`400 BAD REQUEST`

```json
{
  "error": "Ya tienes una reseña para este artículo"
}
```

```json
{
  "error": "No puedes reseñar este artículo. Solo puedes reseñar artículos que hayas comprado"
}
```

`401 UNAUTHORIZED`

```json
{
  "error": "Unauthorized"
}
```

---

### Obtener Reseña por ID

`GET /v1/feed/{id}`

**Params path**

- id: ID de la reseña

**Response**

`200 OK`

```json
{
  "id": 1,
  "userId": "69279ef20cd0f9962d81ada1",
  "articleId": "6927da17c3255b97d5a1acbb",
  "comment": "Excelente producto, muy recomendado",
  "rating": 5,
  "createdAt": "2025-11-27T02:30:00",
  "updatedAt": null
}
```

`404 NOT FOUND`

```json
{
  "error": "Feed no encontrado"
}
```

---

### Actualizar Reseña

`PUT /v1/feed/{id}`

**Headers**

- Authorization: Bearer {token}

**Params path**

- id: ID de la reseña

**Body**

```json
{
  "comment": "Actualizo mi comentario, sigue siendo excelente",
  "rating": 4
}
```

**Response**

`200 OK`

```json
{
  "id": 1,
  "userId": "69279ef20cd0f9962d81ada1",
  "articleId": "6927da17c3255b97d5a1acbb",
  "comment": "Actualizo mi comentario, sigue siendo excelente",
  "rating": 4,
  "createdAt": "2025-11-27T02:30:00",
  "updatedAt": "2025-11-27T02:45:00"
}
```

`401 UNAUTHORIZED`

```json
{
  "error": "Unauthorized"
}
```

`403 FORBIDDEN`

```json
{
  "error": "No tienes permiso para modificar este feed"
}
```

`404 NOT FOUND`

```json
{
  "error": "Feed no encontrado"
}
```

---

### Eliminar Reseña

`DELETE /v1/feed/{id}`

**Headers**

- Authorization: Bearer {token}

**Params path**

- id: ID de la reseña

**Response**

`204 NO CONTENT`

*Sin cuerpo de respuesta*

`401 UNAUTHORIZED`

```json
{
  "error": "Unauthorized"
}
```

`403 FORBIDDEN`

```json
{
  "error": "No tienes permiso para eliminar este feed"
}
```

`404 NOT FOUND`

```json
{
  "error": "Feed no encontrado"
}
```

---

### Obtener Reseñas de un Artículo

`GET /v1/feed/article/{articleId}`

**Params path**

- articleId: ID del artículo

**Response**

`200 OK`

```json
[
  {
    "id": 1,
    "userId": "69279ef20cd0f9962d81ada1",
    "articleId": "6927da17c3255b97d5a1acbb",
    "comment": "Excelente producto",
    "rating": 5,
    "createdAt": "2025-11-27T02:30:00",
    "updatedAt": null
  },
  {
    "id": 2,
    "userId": "69279ef20cd0f9962d81ada2",
    "articleId": "6927da17c3255b97d5a1acbb",
    "comment": "Muy bueno",
    "rating": 4,
    "createdAt": "2025-11-27T03:00:00",
    "updatedAt": null
  }
]
```

---

### Obtener Promedio de Calificaciones de un Artículo

`GET /v1/feed/article/{articleId}/rating`

**Params path**

- articleId: ID del artículo

**Response**

`200 OK`

```json
{
  "articleId": "6927da17c3255b97d5a1acbb",
  "averageRating": 4.5,
  "totalReviews": 10,
  "updatedAt": "2025-11-27T15:30:00"
}
```

`404 NOT FOUND`

```json
{
  "error": "No hay reseñas para este artículo"
}
```

---

### Obtener Mis Reseñas

`GET /v1/feed/my-feeds`

**Headers**

- Authorization: Bearer {token}

**Response**

`200 OK`

```json
[
  {
    "id": 1,
    "userId": "69279ef20cd0f9962d81ada1",
    "articleId": "6927da17c3255b97d5a1acbb",
    "comment": "Excelente producto",
    "rating": 5,
    "createdAt": "2025-11-27T02:30:00",
    "updatedAt": null
  }
]
```

`401 UNAUTHORIZED`

```json
{
  "error": "Unauthorized"
}
```

## Dependencias con otros microservicios

| Microservicio | Tipo | Descripción |
|---------------|------|-------------|
| Auth | REST (síncrono) | Validación de tokens JWT en `GET /users/current` |
| Orders | REST (síncrono) | Verificación de compra del artículo en `GET /orders` y `GET /orders/{orderId}` |

## Configuración

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `server.port` | Puerto del servidor | 3005 |
| `security.auth-server-url` | URL del servicio Auth | http://localhost:3000 |
| `orders-server-url` | URL del servicio Orders | http://localhost:3004 |
| `spring.datasource.url` | URL de la base de datos PostgreSQL | jdbc:postgresql://localhost:5432/user-feed |
