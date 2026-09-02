# Pizzeria Lite

Proyecto de gestion de pedidos para una pizzeria.
La aplicacion tiene dos modulos funcionales:

- **Producto**: crud de pizzas, bebidas, postres e ingredientes.
- **Pedido**: creacion de pedidos, calculo de totales y descuentos, y cambios
  de estado (`CREATED`, `IN_PREPARATION`, `ON_THE_WAY`, `DELIVERED`,
  `CANCELLED`).

## Stack tecnologico

- Spring Boot 3.2 / Java 17
- Gradle 8
- PostgreSQL 14
- Docker / Docker Compose
- Spring Security (Basic Auth para Actuator)
- Jakarta Bean Validation
- JaCoCo (cobertura de pruebas)

## Arquitectura

Cada modulo sigue una arquitectura en capas: el controlador solo recibe/
devuelve DTOs y delega toda la logica de negocio al servicio; el servicio es
el unico que conoce las entidades JPA y los repositorios.

```
Controller (valida la forma del request con @Valid)
   -> Service (reglas de negocio, entidades, excepciones de dominio)
      -> Repository (acceso a datos)
```

Las excepciones de negocio (producto/pedido no encontrado, regla de negocio
incumplida, transicion de estado invalida) se resuelven en un
`GlobalExceptionHandler` central, que responde siempre con el mismo formato
de error:

```json
{
  "timestamp": "2026-08-27T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found: 999",
  "path": "/api/products/999",
  "details": []
}
```

## Como levantar el entorno

Requisitos: Docker y Docker Compose instalados.

Desde la raiz del proyecto:

```bash
docker compose up --build
```

Esto va a:

1. Construir la imagen de la aplicacion (Gradle 8 / JDK 17) usando el `Dockerfile`.
2. Levantar un contenedor de PostgreSQL 14 con la base de datos `pizzeria_db`.
3. Levantar la aplicacion en `http://localhost:8080`, que se conecta automaticamente
   a la base de datos y carga datos de ejemplo (`data.sql`) en el arranque.

Para bajar el entorno:

```bash
docker compose down
```

Para bajar el entorno y borrar tambien el volumen de datos de Postgres:

```bash
docker compose down -v
```

> Alternativa sin Docker: si tenes Gradle 8 y un PostgreSQL 14 corriendo
> localmente, podes ajustar las variables del `.env` con tu propia conexion y
> ejecutar `./gradlew bootRun`.

### Variables de entorno (`.env`)

La configuracion (credenciales de base de datos, API key del proveedor de
notificaciones, perfil activo y usuario de Actuator) se resuelve via
variables de entorno, nunca hardcodeada en `application.yml`:

```
APPLICATION_NAME=pizzeria-lite
DB=pizzeria_db
USER=pizzeria_user
PASS=pizzeria_pass123
API_URL=https://api.fake-sms-provider.com/v1/send
API_KEY=sk_test_...
SPRING_PROFILES_ACTIVE=dev
ACTUATOR_USER=admin
ACTUATOR_PASSWORD=changeme
```

### Perfiles

- **`dev`** (`application-dev.yml`, perfil por defecto): `ddl-auto: update` y
  carga `data.sql` en cada arranque — pensado para desarrollo local.
- **`prod`** (`application-prod.yml`): `ddl-auto: validate` y no reinicializa
  datos — pensado para un entorno donde el esquema ya existe.

Se activa con la variable `SPRING_PROFILES_ACTIVE`.

## Proceso y logica de negocio

### Crear un producto

1. Se recibe el body con `name`, `description`, `category`, `price` y `available`.
2. Se valida, en este orden, que:
   - `name` no sea nulo ni este vacio.
   - `price` no sea nulo y sea mayor a cero.
   - `category` no sea nula (debe ser una de `PIZZA`, `BEVERAGE`, `DESSERT`, `INGREDIENT`).
   - `price` no supere los 500 (limite de sanidad para evitar precios poco realistas).
3. Si alguna validacion falla, se responde `400 Bad Request` con un mensaje indicando el motivo.
4. Si todo es valido, el producto se guarda y se responde `201 Created` con el producto creado.

La actualizacion (`PUT /api/products/{id}`) y el cambio de disponibilidad
(`PATCH /api/products/{id}/availability`) siguen la misma idea: se busca el
producto por id (`404` si no existe) y se aplican los cambios recibidos.

### Crear un pedido

1. Se recibe el body con los datos del cliente (`customerName`, `customerPhone`,
   `customerAddress`), un `couponCode` opcional y una lista `items` con
   `productId` y `quantity` (el cliente **no** envia precios ni nombres de producto).
2. Se valida que `customerName` no este vacio y que `items` tenga al menos un elemento.
3. Por cada item del pedido:
   - Se valida que tenga un `productId` y una `quantity` mayor a cero.
   - Se busca el producto correspondiente; si no existe, se responde `404`.
   - Si el producto existe pero no esta disponible (`available: false`), se responde `400`.
   - Se "congela" en el item el nombre y el precio del producto en ese momento
     (`productName`, `unitPrice`) y se calcula su `lineTotal` (`unitPrice * quantity`).
4. Se suma el `subtotal` del pedido (suma de todos los `lineTotal`) y la cantidad
   total de unidades pedidas.
5. Se calcula el descuento aplicable, combinando las siguientes reglas (son
   acumulables entre si):
   - Si se envia el cupon `PIZZA10`, se aplica un 10% de descuento sobre el subtotal.
   - Si el subtotal supera 100, se aplica un 10% adicional de descuento; si el
     subtotal supera 50 (pero no 100), se aplica un 5% adicional en su lugar.
   - Si la cantidad total de unidades es 5 o mas, se aplica un 5% adicional de descuento.
6. Se calcula el `total` como `subtotal - discountAmount`, y el pedido se crea
   con estado `CREATED`.
7. Se guarda el pedido junto con sus items y se dispara una notificacion de
   confirmacion al cliente.

### Cambiar el estado de un pedido

El estado de un pedido solo puede avanzar siguiendo estas transiciones
permitidas; cualquier otro cambio se rechaza con `400 Bad Request`:

- `CREATED` → `IN_PREPARATION` o `CANCELLED`
- `IN_PREPARATION` → `ON_THE_WAY` o `CANCELLED`
- `ON_THE_WAY` → `DELIVERED`
- `DELIVERED` y `CANCELLED` son estados finales: no admiten ninguna transicion.

Al pasar un pedido a `DELIVERED` o `CANCELLED` tambien se dispara una
notificacion de confirmacion al cliente.

## Probar los endpoints

Base URL: `http://localhost:8080`

Se incluye una coleccion de Postman lista para importar en
[`postman/pizzeria-lite.postman_collection.json`](postman/pizzeria-lite.postman_collection.json),
con una variable `base_url` ya configurada a `http://localhost:8080`.

### Modulo Producto (`/api/products`)

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/api/products` | Lista todos los productos |
| GET | `/api/products/{id}` | Obtiene un producto por id |
| GET | `/api/products/search?name=pizza` | Busca productos por nombre |
| POST | `/api/products` | Crea un producto |
| PUT | `/api/products/{id}` | Actualiza un producto |
| PATCH | `/api/products/{id}/availability?available=false` | Cambia disponibilidad |
| DELETE | `/api/products/{id}` | Elimina un producto |

Ejemplo de body para `POST /api/products`:

```json
{
  "name": "Pepperoni Lovers Pizza",
  "description": "Double pepperoni, extra cheese",
  "category": "PIZZA",
  "price": 12.99,
  "available": true
}
```

Categorias validas: `PIZZA`, `BEVERAGE`, `DESSERT`, `INGREDIENT`.

### Modulo Pedido (`/api/orders`)

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/api/orders` | Lista todos los pedidos |
| GET | `/api/orders/{id}` | Obtiene un pedido por id |
| GET | `/api/orders/search?customerName=Juan` | Busca pedidos por cliente |
| POST | `/api/orders` | Crea un pedido |
| PUT | `/api/orders/{id}/status` | Cambia el estado del pedido |
| DELETE | `/api/orders/{id}` | Elimina un pedido |

Ejemplo de body para `POST /api/orders`:

```json
{
  "customerName": "Ana Torres",
  "customerPhone": "+51999888777",
  "customerAddress": "Av. Central 100, Lima",
  "couponCode": "PIZZA10",
  "items": [
    { "productId": 2, "quantity": 2 },
    { "productId": 5, "quantity": 3 }
  ]
}
```

Ejemplo de body para `PUT /api/orders/{id}/status`:

```json
{
  "status": "IN_PREPARATION"
}
```

Estados posibles: `CREATED`, `IN_PREPARATION`, `ON_THE_WAY`, `DELIVERED`, `CANCELLED`.

### Datos de ejemplo

Al levantar el entorno se cargan automaticamente 9 productos (pizzas, bebidas,
postres e ingredientes) y 3 pedidos de prueba con sus items, listos para
consultarse desde el arranque.

### Actuator

Solo estan expuestos `/actuator/health`, `/actuator/info` y
`/actuator/metrics`. `health` e `info` son publicos; `metrics` requiere
autenticacion HTTP Basic con `ACTUATOR_USER` / `ACTUATOR_PASSWORD`
(`admin` / `changeme` por defecto). Cualquier otro endpoint de Actuator
(`env`, `beans`, etc.) no esta expuesto y responde `404`.

> Para una guia mas detallada de pruebas manuales (incluyendo ejemplos de
> calculo de descuentos, la maquina de estados del pedido y como probar
> Actuator con autenticacion), ver
> [`POSTMAN_GUIDE.md`](POSTMAN_GUIDE.md).

## Estructura del proyecto

```
src/main/java/com/pizzeria/app/
├── PizzeriaApplication.java
├── common/
│   └── exception/       # excepciones base y GlobalExceptionHandler
├── config/
│   └── SecurityConfig.java
├── product/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   ├── dto/
│   ├── exception/
│   └── controller/
└── order/
    ├── entity/
    ├── repository/
    ├── service/          # OrderService, PricingService, NotificationService...
    ├── dto/
    ├── exception/
    ├── config/           # PricingProperties
    └── controller/
```

## Tests y cobertura

```bash
./gradlew test
```

Corre las pruebas unitarias de ambos modulos (validaciones y reglas de
negocio de `ProductService`, calculo de descuentos de `PricingService` y la
maquina de estados de `OrderStatusTransitionValidator`) y genera un reporte
de cobertura con JaCoCo en `build/reports/jacoco/test/html/index.html`.

