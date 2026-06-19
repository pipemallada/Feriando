# Feriando 🧺

**Feriando** es una plataforma para comprar productos de la feria (frutas, verduras, frutos secos, etc.) de forma simple, ya sea con **despacho a domicilio** o **retiro en el puesto**. La idea es conectar de manera digital a los **clientes** que quieren comprar con los **feriantes** que venden.

Todo el backend está construido con una **arquitectura de microservicios** en Spring Boot: cada parte del negocio (usuarios, productos, pedidos, pagos, etc.) vive en su propio servicio independiente, con su propia base de datos, y se comunican entre ellos por REST. Adelante hay un **API Gateway** que funciona como puerta de entrada única.

- **Asignatura:** Desarrollo Fullstack (DSY1103-007D)
- **Profesora:** Nancy Bernal

### Integrantes

- Luis Felipe Mallada
- Cristóbal Stenger

---

## Tabla de contenidos

1. [De qué se trata el proyecto](#1-de-qué-se-trata-el-proyecto)
2. [Los dos roles](#2-los-dos-roles)
3. [Cómo funciona de punta a punta (el flujo de una compra)](#3-cómo-funciona-de-punta-a-punta-el-flujo-de-una-compra)
4. [Arquitectura general](#4-arquitectura-general)
5. [El patrón CSR (Controller – Service – Repository)](#5-el-patrón-csr-controller--service--repository)
6. [Los 10 microservicios en detalle](#6-los-10-microservicios-en-detalle)
7. [Comunicación entre microservicios](#7-comunicación-entre-microservicios)
8. [API Gateway](#8-api-gateway)
9. [Stack tecnológico](#9-stack-tecnológico)
10. [Base de datos](#10-base-de-datos)
11. [Manejo de errores y validaciones](#11-manejo-de-errores-y-validaciones)
12. [Documentación con Swagger / OpenAPI](#12-documentación-con-swagger--openapi)
13. [Pruebas unitarias](#13-pruebas-unitarias)
14. [Cómo correrlo en local](#14-cómo-correrlo-en-local)
15. [Despliegue en Google Cloud](#15-despliegue-en-google-cloud)
16. [¿Por qué Google Cloud y no AWS?](#16-por-qué-google-cloud-y-no-aws)
17. [Estructura de carpetas](#17-estructura-de-carpetas)
18. [Variables de entorno](#18-variables-de-entorno)
19. [Cosas pendientes / a futuro](#19-cosas-pendientes--a-futuro)

---

## 1. De qué se trata el proyecto

La feria es un lugar donde se compra muy bien, pero es presencial: hay que ir, cargar las bolsas, andar con efectivo, etc. Feriando lleva eso a lo digital. El cliente entra, mira el catálogo de los feriantes, arma su carrito, hace el pedido, paga y elige si lo va a retirar o se lo despachan. Y por el otro lado, el feriante administra su puesto, sus productos, su stock y revisa qué le están pidiendo, además de tener reportes de cómo le está yendo.

Lo armamos con microservicios porque cada parte del negocio es bastante distinta entre sí (no es lo mismo manejar usuarios que manejar pagos), y separarlas nos deja trabajar en cada una sin pisarnos, escalar solo lo que se necesita y que si una falla no se caiga todo el sistema.

## 2. Los dos roles

| Rol | Qué puede hacer |
|---|---|
| **Cliente** | Navega el catálogo, arma su carrito, genera el pedido, paga y después califica al feriante. |
| **Feriante** | Administra su puesto/perfil, crea y edita sus productos, controla su inventario, ve los pedidos que le llegan y genera reportes de ventas. |

Ambos son, en el fondo, un **usuario** (registrado en `ms-usuario`) con un campo `rol` que dice si es `CLIENTE` o `FERIANTE`. Cuando un usuario es feriante, además se le crea un perfil de feriante en `ms-feriante`.

## 3. Cómo funciona de punta a punta (el flujo de una compra)

Para entender cómo se conectan los microservicios, lo más fácil es seguir una compra completa:

1. **Registro.** El usuario se crea en `ms-usuario`. Si es feriante, se le arma su perfil en `ms-feriante` (que antes verifica con `ms-usuario` que el usuario realmente exista).
2. **Catálogo.** El feriante carga sus productos en `ms-producto` y les asigna stock en `ms-inventario`.
3. **Carrito.** El cliente crea un carrito en `ms-carrito` y le va agregando productos. Ojo: el precio de cada producto **no lo manda el cliente**, lo consulta el carrito directo a `ms-producto` para que nadie pueda "inventarse" un precio.
4. **Pedido.** Cuando el carrito está listo, se cierra y se genera un pedido en `ms-pedido` (estado inicial `PENDIENTE`).
5. **Despacho.** En `ms-despacho` se define si es retiro en feria o despacho a domicilio (en cuyo caso la dirección es obligatoria).
6. **Pago.** El cliente paga en `ms-pago`. Cuando el pago se **confirma**, pasan dos cosas automáticamente:
   - `ms-pago` le avisa a `ms-pedido` que marque el pedido como `PAGADO`.
   - `ms-pago` le avisa a `ms-inventario` que descuente el stock de cada producto del pedido.
7. **Preparación y entrega.** El pedido va cambiando de estado (`PREPARANDO` → `LISTO` → `ENTREGADO`) y el despacho también (`PENDIENTE` → `EN_RUTA` → `ENTREGADO`).
8. **Calificación.** Una vez recibido, el cliente califica al feriante en `ms-calificacion`, que recalcula el promedio y se lo manda a `ms-feriante`.
9. **Reportes.** El feriante puede pedir un reporte de ventas en `ms-reporte`, que junta la info de sus pedidos desde `ms-pedido`.

## 4. Arquitectura general

```
                          ┌──────────────────┐
        Cliente / App ───▶ │   API Gateway    │  (puerto 8080)
                          │ Spring Cloud GW  │
                          └────────┬─────────┘
                                   │  enruta /api/** al MS que corresponde
        ┌──────────────┬──────────┼───────────┬──────────────┬─────────────┐
        ▼              ▼          ▼            ▼              ▼             ▼
   ms-usuario     ms-feriante  ms-producto  ms-inventario  ms-carrito   ms-pedido
   ms-despacho    ms-pago      ms-calificacion   ms-reporte
        │              │          │            │              │             │
        ▼              ▼          ▼            ▼              ▼             ▼
   ┌────────────────────────────────────────────────────────────────────────┐
   │             PostgreSQL  (un schema independiente por microservicio)       │
   └────────────────────────────────────────────────────────────────────────┘
```

Cada microservicio:

- Es una aplicación Spring Boot **independiente**, con su propio `pom.xml`, su propio puerto y su propio `Dockerfile`.
- Tiene su **propio schema** dentro de PostgreSQL (`usuario_schema`, `producto_schema`, etc.). No comparten tablas; si un servicio necesita un dato de otro, se lo pide por REST.
- Sigue el patrón **CSR** (Controller – Service – Repository), que se explica más abajo.
- Expone su propia documentación **Swagger**.

## 5. El patrón CSR (Controller – Service – Repository)

Todos los microservicios están organizados igual, en capas, para que cada clase tenga una sola responsabilidad. Las carpetas dentro de cada MS son básicamente estas:

| Capa / carpeta | Responsabilidad |
|---|---|
| `controller` | Recibe las peticiones HTTP, las traduce a llamadas al service y devuelve el código de respuesta correcto (200, 201, 204, 400, 404...). No tiene lógica de negocio. |
| `service` | Acá vive **toda la lógica de negocio**: validaciones, reglas, llamadas a otros microservicios, cálculos. Es la capa más importante y la que se prueba con tests. |
| `repository` | Acceso a la base de datos con Spring Data JPA. Son interfaces, casi sin código. |
| `model` | Las entidades JPA (las tablas mapeadas a clases Java). |
| `dto` | Objetos de entrada (`...RequestDTO`) y salida (`...ResponseDTO`). Sirven para no exponer las entidades directamente y para validar lo que llega. |
| `mapper` | Convierte entre entidades y DTOs, para no mezclar eso dentro del service. |
| `client` | (Solo en los MS que hablan con otros) Las clases WebClient que llaman a otros microservicios. |
| `exception` | Las excepciones propias (`BusinessException`, `ResourceNotFoundException`) y el `GlobalExceptionHandler`. |
| `config` | Configuración, por ejemplo los beans de WebClient. |

La idea es que el **controller sea "delgado"**: solo traduce HTTP. Por ejemplo, el de usuarios literalmente solo hace `service.crear(dto)` y elige el status. Toda la "inteligencia" está en el service, que es justo lo que cubren las pruebas unitarias.

## 6. Los 10 microservicios en detalle

> Los puertos son los que usa cada servicio cuando se corre en local. En la nube (Cloud Run) el puerto lo asigna la plataforma con la variable `PORT`.

### ms-usuario (puerto 8081)

Maneja el registro y los datos de los usuarios (tanto clientes como feriantes). Cada usuario tiene nombre, apellido, email, contraseña (guardada como `password_hash`) y un rol (`CLIENTE` o `FERIANTE`).

**Endpoints (`/usuarios`):**

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/usuarios` | Lista todos los usuarios. |
| GET | `/usuarios/{id}` | Trae un usuario por id (404 si no existe). |
| POST | `/usuarios` | Crea un usuario (201). |
| PUT | `/usuarios/{id}` | Actualiza un usuario. |
| DELETE | `/usuarios/{id}` | Elimina un usuario (204). |

**Reglas de negocio:** el email es único. Al crear, si ya existe un usuario con ese email tira `400`. Al actualizar, deja repetir el mismo email del propio usuario, pero no usar uno que ya tenga otro.

### ms-feriante (puerto 8082)

Administra el perfil del feriante: su puesto (`nombre_puesto`), descripción, teléfono y su calificación promedio.

**Endpoints (`/feriantes`):**

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/feriantes` | Lista todos. |
| GET | `/feriantes/{id}` | Trae uno por id. |
| POST | `/feriantes` | Crea un feriante. |
| PUT | `/feriantes/{id}` | Actualiza el perfil. |
| DELETE | `/feriantes/{id}` | Elimina. |
| PATCH | `/feriantes/{id}/calificacion-promedio` | **Endpoint interno**: lo usa `ms-calificacion` para actualizar el promedio. |

**Reglas de negocio:**
- Antes de crear un feriante, le pregunta a `ms-usuario` si ese `id_usuario` existe (si no, error). Esto evita feriantes "huérfanos".
- Un usuario solo puede tener **un** feriante asociado (1 a 1).
- El promedio se guarda redondeado a 2 decimales, porque la columna es `NUMERIC(3,2)`.

### ms-producto (puerto 8083)

Gestiona el catálogo: productos y categorías. Maneja **dos** recursos.

**Endpoints de productos (`/productos`):**

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/productos` | Lista todos. Con `?idFeriante=N` filtra por feriante. |
| GET | `/productos/{id}` | Trae uno por id. |
| POST | `/productos` | Crea un producto. |
| PUT | `/productos/{id}` | Actualiza. |
| DELETE | `/productos/{id}` | Elimina. |

**Endpoints de categorías (`/categorias`):** GET (lista), GET `/{id}`, POST, PUT `/{id}`, DELETE `/{id}`.

**Reglas de negocio:** al crear o editar un producto, la categoría debe existir (si no, 404). El nombre de una categoría no se puede repetir.

### ms-inventario (puerto 8084)

Controla el stock disponible de cada producto, su stock mínimo y una alerta que se enciende cuando queda poco.

**Endpoints (`/inventario`):**

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/inventario` | Lista todo el inventario. |
| GET | `/inventario/{id}` | Trae un registro por id. |
| GET | `/inventario/producto/{idProducto}` | Trae el inventario de un producto puntual. |
| POST | `/inventario` | Crea el inventario de un producto. |
| PUT | `/inventario/{id}` | Actualiza. |
| PATCH | `/inventario/producto/{idProducto}/descontar` | Descuenta stock (lo usa `ms-pago` al confirmar). |
| PATCH | `/inventario/producto/{idProducto}/reponer` | Repone stock. |
| DELETE | `/inventario/{id}` | Elimina. |

**Reglas de negocio:**
- Un producto tiene un solo registro de inventario.
- No se puede descontar más stock del disponible (si no, error de stock insuficiente).
- Cada vez que cambia el stock, se recalcula la **alerta**: si el stock queda por debajo (o igual) del mínimo, se prende (`alerta_activa = 1`).

### ms-carrito (puerto 8085)

El carrito del cliente antes de confirmar el pedido. Un carrito tiene varios ítems (detalles), y los ítems son sub-recursos del carrito (no existen sin él).

**Endpoints (`/carritos`):**

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/carritos` | Lista todos. |
| GET | `/carritos/{id}` | Trae un carrito con sus ítems. |
| POST | `/carritos` | Crea un carrito vacío. |
| POST | `/carritos/{id}/items` | Agrega un ítem al carrito. |
| DELETE | `/carritos/{id}/items/{idDetalle}` | Saca un ítem (devuelve el carrito actualizado). |
| PATCH | `/carritos/{id}/cerrar` | Cierra el carrito (ya no se puede tocar). |
| DELETE | `/carritos/{id}` | Borra el carrito completo. |

**Reglas de negocio:**
- Solo se puede modificar un carrito en estado `ACTIVO`. Si está `CERRADO`, no acepta más cambios.
- El **precio** de cada ítem no lo manda el cliente: el carrito lo consulta a `ms-producto`. Así nadie puede manipular el precio desde afuera.
- Cerrar el carrito es el paso previo a generar el pedido.

### ms-pedido (puerto 8086)

Genera la orden de compra y maneja sus estados a lo largo del tiempo.

**Endpoints (`/pedidos`):**

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/pedidos` | Lista todos. Con `?idCliente=N` o `?idFeriante=N` filtra. |
| GET | `/pedidos/{id}` | Trae un pedido con sus detalles. |
| POST | `/pedidos` | Crea un pedido. |
| PATCH | `/pedidos/{id}/estado` | Cambia el estado del pedido. |
| DELETE | `/pedidos/{id}` | Elimina. |

**Estados que maneja:** `PENDIENTE` → `PAGADO` → `PREPARANDO` → `LISTO` → `ENTREGADO`. Este microservicio también es consultado por `ms-pago` (para marcar como pagado) y por `ms-reporte` (para juntar las ventas).

### ms-despacho (puerto 8087)

Administra cómo se entrega el pedido: retiro en feria o despacho a domicilio.

**Endpoints (`/despachos`):**

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/despachos` | Lista todos. |
| GET | `/despachos/{id}` | Trae uno por id. |
| GET | `/despachos/pedido/{idPedido}` | Trae el despacho de un pedido puntual. |
| POST | `/despachos` | Crea el despacho. |
| PUT | `/despachos/{id}` | Actualiza. |
| PATCH | `/despachos/{id}/estado` | Cambia el estado del despacho. |
| DELETE | `/despachos/{id}` | Elimina. |

**Reglas de negocio:**
- Un pedido solo puede tener un despacho (1 a 1).
- Si el tipo es `DOMICILIO`, la **dirección es obligatoria** (esto se valida en el service, porque depende de otro campo).
- Cuando el estado pasa a `ENTREGADO`, se registra automáticamente la fecha de entrega.

### ms-pago (puerto 8088)

Procesa el pago de un pedido. Es uno de los microservicios más interesantes porque coordina a otros dos.

**Endpoints (`/pagos`):**

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/pagos` | Lista todos. |
| GET | `/pagos/{id}` | Trae uno por id. |
| POST | `/pagos` | Registra un pago (estado inicial `PENDIENTE`). |
| PATCH | `/pagos/{id}/confirmar` | Confirma el pago y dispara los efectos. |
| PATCH | `/pagos/{id}/rechazar` | Rechaza el pago. |
| DELETE | `/pagos/{id}` | Elimina. |

**Reglas de negocio:**
- Un pedido solo puede tener un pago.
- No se puede confirmar dos veces el mismo pago.
- Al **confirmar** un pago: marca el pago como `CONFIRMADO`, le pide a `ms-pedido` que ponga el pedido en `PAGADO`, y por cada producto del pedido le pide a `ms-inventario` que descuente el stock.

### ms-calificacion (puerto 8089)

Permite al cliente calificar al feriante después de recibir el pedido (puntaje + comentario).

**Endpoints (`/calificaciones`):**

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/calificaciones` | Lista todas. Con `?idFeriante=N` filtra por feriante. |
| GET | `/calificaciones/{id}` | Trae una por id. |
| POST | `/calificaciones` | Crea la calificación. |
| DELETE | `/calificaciones/{id}` | Elimina. |

**Reglas de negocio:**
- Un pedido solo se puede calificar una vez.
- **No tiene PUT a propósito:** una calificación no se edita. Si el cliente quiere cambiar su nota, borra y crea de nuevo.
- Cada vez que se crea o se borra una calificación, recalcula el promedio del feriante y se lo manda a `ms-feriante`. Si se borró la última, el promedio vuelve a `0.00`.

### ms-reporte (puerto 8090)

Genera estadísticas de ventas para el feriante.

**Endpoints (`/reportes`):**

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/reportes` | Lista todos. Con `?idFeriante=N` filtra. |
| GET | `/reportes/{id}` | Trae uno por id. |
| POST | `/reportes` | Crea un reporte manualmente. |
| POST | `/reportes/generar/feriante/{idFeriante}` | **Genera el reporte automáticamente** consultando `ms-pedido`. |
| DELETE | `/reportes/{id}` | Elimina. |

**Reglas de negocio:** al generar, le pide a `ms-pedido` todos los pedidos del feriante, suma el total de los que están en `PAGADO`, `PREPARANDO`, `LISTO` o `ENTREGADO`, cuenta cuántos pedidos hay y calcula el producto más pedido (el "top").

## 7. Comunicación entre microservicios

Cuando un microservicio necesita un dato que vive en otro, **no entra a su base de datos**: se lo pide por REST usando **WebClient** (de Spring). Esto mantiene a cada servicio dueño de su propia info.

El mapa de quién llama a quién:

| Microservicio | Llama a... | Para qué |
|---|---|---|
| `ms-feriante` | `ms-usuario` | Verificar que el usuario exista antes de crear el feriante. |
| `ms-carrito` | `ms-producto` | Consultar el precio real del producto al agregarlo al carrito. |
| `ms-pago` | `ms-pedido` | Traer el pedido y marcarlo como `PAGADO` al confirmar. |
| `ms-pago` | `ms-inventario` | Descontar el stock de cada producto al confirmar el pago. |
| `ms-calificacion` | `ms-feriante` | Mandar el nuevo promedio de calificación. |
| `ms-reporte` | `ms-pedido` | Traer los pedidos del feriante para armar el reporte. |

**Cómo se maneja que el otro servicio falle:** los clients tienen un **timeout de 3 segundos** y atrapan los errores. En vez de reventar, devuelven un `Optional.empty()` o un `false` y dejan un log de advertencia. Así, si por ejemplo `ms-producto` no responde cuando el carrito le pide un precio, el carrito devuelve un error claro (400) en vez de quedarse colgado.

Las URLs de los servicios que se consumen están configuradas por properties / variables de entorno (por ejemplo `feriando.ms-pedido.url`), así que cambiar de local a la nube es solo cambiar esa variable.

## 8. API Gateway

El **API Gateway** (puerto 8080), hecho con **Spring Cloud Gateway**, es el único punto de entrada. En vez de que el cliente conozca los 10 servicios y sus puertos, le pega siempre al gateway con rutas que empiezan en `/api/...`, y el gateway redirige al servicio correcto sacando el prefijo `/api` (filtro `StripPrefix=1`).

| Ruta en el Gateway | Va a... |
|---|---|
| `/api/usuarios/**` | ms-usuario |
| `/api/feriantes/**` | ms-feriante |
| `/api/productos/**` | ms-producto |
| `/api/categorias/**` | ms-producto |
| `/api/inventario/**` | ms-inventario |
| `/api/carritos/**` | ms-carrito |
| `/api/pedidos/**` | ms-pedido |
| `/api/despachos/**` | ms-despacho |
| `/api/pagos/**` | ms-pago |
| `/api/calificaciones/**` | ms-calificacion |
| `/api/reportes/**` | ms-reporte |

Por ejemplo, una llamada a `GET /api/usuarios/1` el gateway la transforma en `GET /usuarios/1` contra `ms-usuario`. Las rutas de cada servicio están definidas en `api-gateway/src/main/resources/application.yml`.

## 9. Stack tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot (Spring Web, Spring Data JPA, Validation) |
| Gateway | Spring Cloud Gateway (WebFlux) |
| Comunicación entre servicios | WebClient (Spring Reactive Web) |
| Base de datos | PostgreSQL (un schema por microservicio) |
| Driver / conexión nube | PostgreSQL JDBC + Google Cloud SQL Socket Factory |
| Documentación | springdoc-openapi (Swagger UI) |
| Pruebas | JUnit 5 + Mockito + AssertJ |
| Cobertura | JaCoCo |
| Contenedores | Docker (un `Dockerfile` por servicio) |
| Despliegue | Google Cloud Run + Google Cloud SQL |
| CI/CD | GitHub Actions (un workflow por servicio) |
| Build | Maven |
| Control de versiones | GitHub |

## 10. Base de datos

Usamos **PostgreSQL**, y la idea central es que **cada microservicio tiene su propio schema**, para mantener separadas las responsabilidades aunque todo viva en la misma instancia de base de datos:

`usuario_schema`, `feriante_schema`, `producto_schema`, `inventario_schema`, `carrito_schema`, `pedido_schema`, `despacho_schema`, `pago_schema`, `calificacion_schema`, `reporte_schema`.

Toda la estructura está en el archivo **`schema.sql`** (en la raíz del repo). Ese script crea los schemas, las tablas y carga unos datos de ejemplo para poder probar de inmediato.

**Tablas principales:**

- `usuario_schema.usuarios` — usuarios (clientes y feriantes), con email único.
- `feriante_schema.feriantes` — perfil del feriante, ligado a un usuario, con su calificación promedio.
- `producto_schema.categorias` y `producto_schema.productos` — catálogo. Un producto pertenece a una categoría (FK) y a un feriante.
- `inventario_schema.inventario` — stock por producto (con `stock_disponible`, `stock_minimo` y `alerta_activa`). Un registro por producto.
- `carrito_schema.carritos` y `carrito_schema.detalle_carrito` — el carrito y sus ítems (borrado en cascada).
- `pedido_schema.pedidos` y `pedido_schema.detalle_pedido` — la orden y sus líneas, con subtotal.
- `despacho_schema.despachos` — entrega (tipo, dirección, comuna, estado, fechas). Uno por pedido.
- `pago_schema.pagos` — pago (monto, método, estado, código de transacción). Uno por pedido.
- `calificacion_schema.calificaciones` — puntaje y comentario. Una por pedido.
- `reporte_schema.reporte_ventas` — total vendido, total de pedidos y producto top por período.

**Sobre las relaciones entre servicios:** dentro de un mismo schema sí hay llaves foráneas (por ejemplo producto → categoría, o detalle_carrito → carrito). Pero **entre microservicios distintos no hay FK**: por ejemplo, `pedidos.id_cliente` apunta a un usuario que vive en otro schema/servicio, y esa coherencia se cuida por código (vía WebClient), no con una FK física. Eso es a propósito en microservicios: cada base es independiente.

**Datos de ejemplo que trae el script:** 4 usuarios (2 clientes y 2 feriantes), 2 feriantes ("Verduras Don Pedro" y "Frutos del Sur"), categorías (Frutas, Verduras, Frutos secos, etc.), 6 productos y su inventario (uno de ellos, las nueces, ya viene con la alerta de stock prendida para poder probar ese caso).

## 11. Manejo de errores y validaciones

**Validaciones de entrada (DTOs):** los datos que llegan se validan con Bean Validation (`@NotBlank`, `@Email`, `@Size`, etc.) en los `RequestDTO`. Por ejemplo, al crear un usuario el email tiene que tener formato válido y el nombre no puede ir vacío. Si algo falla, se devuelve `400` con el detalle de qué campo está mal.

**Manejo centralizado de errores:** cada microservicio tiene un `GlobalExceptionHandler` (`@RestControllerAdvice`) que captura las excepciones y devuelve siempre un JSON con el mismo formato. Así el que consume la API siempre recibe errores parejos. Las traducciones son:

| Situación | Excepción | Código HTTP |
|---|---|---|
| No se encontró el recurso | `ResourceNotFoundException` | `404 Not Found` |
| Se rompió una regla de negocio | `BusinessException` | `400 Bad Request` |
| Datos de entrada inválidos | `MethodArgumentNotValidException` | `400 Bad Request` (con detalle por campo) |
| Cualquier otra cosa | `Exception` | `500 Internal Server Error` |

Ejemplo del JSON de error:

```json
{
  "timestamp": "2026-06-19T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Usuario 99 no encontrado"
}
```

La diferencia entre 400 y 404 es a propósito: un `404` dice "lo que pediste no existe", y un `400` dice "lo que mandaste está bien formado pero rompe una regla" (por ejemplo, email repetido o stock insuficiente).

## 11.b Ejemplos de peticiones y respuestas

Acá van algunos ejemplos concretos de cómo se ven las llamadas. Todos los ejemplos usan el **API Gateway** (`http://localhost:8080`), pero si le pegas directo a cada servicio es lo mismo sin el prefijo `/api`.

### Crear un usuario

`POST /api/usuarios`

```json
{
  "nombre": "Camila",
  "apellido": "Núñez",
  "email": "camila.nunez@feriando.cl",
  "passwordHash": "clave123",
  "rol": "CLIENTE"
}
```

Respuesta `201 Created`:

```json
{
  "idUsuario": 5,
  "nombre": "Camila",
  "apellido": "Núñez",
  "email": "camila.nunez@feriando.cl",
  "rol": "CLIENTE",
  "activo": 1,
  "createdAt": "2026-06-19T12:00:00"
}
```

Si el email ya existe, responde `400`:

```json
{
  "timestamp": "2026-06-19T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Ya existe un usuario con el email camila.nunez@feriando.cl"
}
```

Y si mandas datos inválidos (por ejemplo email mal escrito y nombre vacío), `400` con el detalle por campo:

```json
{
  "timestamp": "2026-06-19T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Errores de validación",
  "errores": {
    "email": "El email no tiene un formato válido",
    "nombre": "El nombre es obligatorio"
  }
}
```

### Crear un feriante

`POST /api/feriantes`

```json
{
  "idUsuario": 3,
  "nombrePuesto": "Verduras Don Pedro",
  "descripcion": "Verduras y frutas frescas cosechadas en el día",
  "telefono": "+56912345678"
}
```

Respuesta `201 Created`:

```json
{
  "idFeriante": 1,
  "idUsuario": 3,
  "nombrePuesto": "Verduras Don Pedro",
  "descripcion": "Verduras y frutas frescas cosechadas en el día",
  "telefono": "+56912345678",
  "calificacionProm": 0.00,
  "activo": 1
}
```

### Crear un producto

`POST /api/productos`

```json
{
  "idFeriante": 1,
  "idCategoria": 2,
  "nombre": "Tomate larga vida",
  "descripcion": "Tomate firme ideal para ensaladas",
  "precio": 1490.00,
  "unidad": "kg"
}
```

Respuesta `201 Created` (fíjate que la respuesta incluye el `nombreCategoria`, que se resuelve en el servicio):

```json
{
  "idProducto": 2,
  "idFeriante": 1,
  "idCategoria": 2,
  "nombreCategoria": "Verduras",
  "nombre": "Tomate larga vida",
  "descripcion": "Tomate firme ideal para ensaladas",
  "precio": 1490.00,
  "unidad": "kg",
  "activo": 1
}
```

### Armar un carrito

Primero se crea el carrito vacío — `POST /api/carritos`:

```json
{ "idCliente": 1 }
```

Después se le agrega un ítem — `POST /api/carritos/1/items`:

```json
{
  "idProducto": 2,
  "cantidad": 3
}
```

Respuesta `201 Created`. El `precioUnitario` y el `subtotal` los calcula el carrito (el precio lo trae de `ms-producto`, **no** se manda en la petición):

```json
{
  "idCarrito": 1,
  "idCliente": 1,
  "estado": "ACTIVO",
  "createdAt": "2026-06-19T12:00:00",
  "detalles": [
    {
      "idDetalle": 1,
      "idProducto": 2,
      "cantidad": 3,
      "precioUnitario": 1490.00,
      "subtotal": 4470.00
    }
  ],
  "total": 4470.00
}
```

### Crear un pedido

`POST /api/pedidos`

```json
{
  "idCliente": 1,
  "idFeriante": 1,
  "detalles": [
    { "idProducto": 2, "cantidad": 3, "precioUnitario": 1490.00 }
  ]
}
```

Respuesta `201 Created`:

```json
{
  "idPedido": 1,
  "idCliente": 1,
  "idFeriante": 1,
  "estado": "PENDIENTE",
  "total": 4470.00,
  "fechaPedido": "2026-06-19T12:05:00",
  "detalles": [
    {
      "idProducto": 2,
      "cantidad": 3,
      "precioUnitario": 1490.00,
      "subtotal": 4470.00
    }
  ]
}
```

### Pagar y confirmar

Se registra el pago — `POST /api/pagos`:

```json
{
  "idPedido": 1,
  "idCliente": 1,
  "monto": 4470.00,
  "metodoPago": "TARJETA",
  "codigoTransaccion": "TX-998877"
}
```

Y se confirma — `PATCH /api/pagos/1/confirmar`. Esto, además de devolver el pago confirmado, dispara que el pedido pase a `PAGADO` y que se descuente el stock:

```json
{
  "idPago": 1,
  "idPedido": 1,
  "idCliente": 1,
  "monto": 4470.00,
  "metodoPago": "TARJETA",
  "estadoPago": "CONFIRMADO",
  "fechaPago": "2026-06-19T12:10:00",
  "codigoTransaccion": "TX-998877"
}
```

### Descontar stock

`PATCH /api/inventario/producto/2/descontar`

```json
{ "cantidad": 3 }
```

Respuesta `200 OK`. Si el nuevo stock queda bajo el mínimo, `alertaActiva` se prende (pasa a `1`):

```json
{
  "idInventario": 2,
  "idProducto": 2,
  "idFeriante": 1,
  "stockDisponible": 77.00,
  "stockMinimo": 15.00,
  "alertaActiva": 0,
  "ultimaActualizacion": "2026-06-19T12:10:01"
}
```

Si pides descontar más de lo que hay, `400`:

```json
{
  "timestamp": "2026-06-19T12:10:01",
  "status": 400,
  "error": "Bad Request",
  "message": "Stock insuficiente. Disponible: 77.00, solicitado: 200"
}
```

### Cambiar el estado de un pedido

`PATCH /api/pedidos/1/estado`

```json
{ "estado": "PREPARANDO" }
```

### Calificar al feriante

`POST /api/calificaciones`

```json
{
  "idPedido": 1,
  "idCliente": 1,
  "idFeriante": 1,
  "puntaje": 5,
  "comentario": "Todo fresco y llegó rápido"
}
```

Respuesta `201 Created`:

```json
{
  "idCalificacion": 1,
  "idPedido": 1,
  "idCliente": 1,
  "idFeriante": 1,
  "puntaje": 5,
  "comentario": "Todo fresco y llegó rápido",
  "fecha": "2026-06-19T13:00:00"
}
```

Detrás de esto, `ms-calificacion` recalcula el promedio del feriante y se lo manda a `ms-feriante`, así que el `calificacionProm` del feriante queda actualizado.

### Generar un reporte de ventas

`POST /api/reportes/generar/feriante/1`

Respuesta `201 Created`:

```json
{
  "idReporte": 1,
  "idFeriante": 1,
  "periodo": "2026-06",
  "totalVentas": 4470.00,
  "totalPedidos": 1,
  "productoTop": "Producto 2",
  "generadoEn": "2026-06-19T14:00:00"
}
```

## 12. Documentación con Swagger / OpenAPI

Cada microservicio expone su propia documentación interactiva con **springdoc-openapi**. Una vez que el servicio está corriendo, se entra a:

```
http://localhost:8081/swagger-ui.html   (ms-usuario)
http://localhost:8082/swagger-ui.html   (ms-feriante)
http://localhost:8083/swagger-ui.html   (ms-producto)
http://localhost:8084/swagger-ui.html   (ms-inventario)
http://localhost:8085/swagger-ui.html   (ms-carrito)
http://localhost:8086/swagger-ui.html   (ms-pedido)
http://localhost:8087/swagger-ui.html   (ms-despacho)
http://localhost:8088/swagger-ui.html   (ms-pago)
http://localhost:8089/swagger-ui.html   (ms-calificacion)
http://localhost:8090/swagger-ui.html   (ms-reporte)
```

Ahí se pueden ver todos los endpoints, los parámetros que reciben, los códigos de respuesta y probar las llamadas directamente desde el navegador. También está disponible el JSON de OpenAPI en `/v3/api-docs` de cada servicio.

En la versión desplegada en Cloud Run es lo mismo pero con la URL del servicio:

```
https://ms-usuario-xxxxxxxx.southamerica-west1.run.app/swagger-ui.html
```

## 13. Pruebas unitarias

Las pruebas están hechas con **JUnit 5** y **Mockito**, y se enfocan en la capa de **service**, que es donde está la lógica de negocio. Usamos **mocks** para simular los repositorios y los clients (así las pruebas no tocan la base de datos real ni dependen de otros microservicios). Para las verificaciones usamos **AssertJ** (`assertThat`, `assertThatThrownBy`).

Cada test sigue la convención **Given–When–Then** (preparar – ejecutar – verificar) y tiene un `@DisplayName` que explica en español qué está validando, por ejemplo *"crear() lanza excepción si el email ya existe"*.

Hay un archivo de pruebas por microservicio, cubriendo las reglas clave de cada dominio:

| Microservicio | Archivo de test | Nº de tests |
|---|---|---|
| ms-usuario | `UsuarioServiceTest` | 10 |
| ms-feriante | `FerianteServiceTest` | 12 |
| ms-producto | `ProductoServiceTest` + `CategoriaServiceTest` | 10 + 8 |
| ms-inventario | `ProductoServiceTest` | 17 |
| ms-carrito | `CarritoServiceTest` | 12 |
| ms-pedido | `PedidoServiceTest` | 10 |
| ms-despacho | `DespachoServiceTest` | 16 |
| ms-pago | `PagoServiceTest` | 13 |
| ms-calificacion | `CalificacionServiceTest` | 10 |
| ms-reporte | `ReporteServiceTest` | 11 |

**Cobertura:** usamos **JaCoCo**, configurado para medir la capa `service` (que es donde está la lógica que importa). Para correr las pruebas de un microservicio:

```bash
cd ms-usuario
mvn test
```

El reporte de cobertura queda en `target/site/jacoco/index.html`.

## 14. Cómo correrlo en local

### Lo que necesitas

- **Java 21**
- **Maven**
- **PostgreSQL** corriendo en `localhost:5432`
- (Opcional) **Docker**, si lo quieres levantar en contenedores

### Paso 1 — Base de datos

Crea la base y corre el `schema.sql`, que deja todos los schemas, tablas y datos de ejemplo listos:

```bash
createdb feriando_db
psql -d feriando_db -f schema.sql
```

Por defecto cada microservicio se conecta con estos datos (se pueden cambiar con las variables de entorno `DB_URL`, `DB_USER`, `DB_PASS`):

```
url:  jdbc:postgresql://localhost:5432/feriando_db
user: postgres
pass: (la que tengas configurada en tu Postgres)
```

Cada servicio apunta a su schema con el parámetro `?currentSchema=...` en la URL.

### Paso 2 — Levantar los microservicios

Entra a la carpeta de cada microservicio y córrelo con Maven:

```bash
cd ms-usuario
mvn spring-boot:run
```

Repite con los que necesites. Si quieres probar todo el flujo, levanta también los que se consumen entre sí (por ejemplo, para probar `ms-carrito` conviene tener arriba `ms-producto`). Al final, levanta el `api-gateway` y ya puedes pegarle a todo desde `http://localhost:8080/api/...`.

### Paso 3 — Con Docker (alternativa)

Cada microservicio trae su propio `Dockerfile`. Para construir y correr uno:

```bash
cd ms-usuario
mvn clean package -DskipTests
docker build -t ms-usuario .
docker run -p 8081:8081 -e PORT=8081 ms-usuario
```

(El `Dockerfile` usa la variable `PORT` para saber en qué puerto levantar, igual que en Cloud Run.)

## 15. Despliegue en Google Cloud

El proyecto está pensado para desplegarse en **Google Cloud**:

- Cada microservicio y el gateway se despliegan como un servicio separado en **Google Cloud Run**.
- La base de datos PostgreSQL corre en **Google Cloud SQL**.
- Las imágenes Docker se guardan en **Artifact Registry**.

**Región:** `southamerica-west1` (Santiago, Chile).

### CI/CD con GitHub Actions

El despliegue está **automatizado**. En `.github/workflows/` hay un workflow por servicio. Cuando se hace push a `main`, cada workflow:

1. Descarga el código y configura Java 21.
2. Se autentica con Google Cloud (usando el secret `GCP_CREDENTIALS`).
3. Compila el microservicio con Maven (`mvn clean package -DskipTests`).
4. Construye la imagen Docker y la sube a Artifact Registry.
5. Despliega esa imagen en Cloud Run.

Cada workflow tiene un filtro de `paths`, así que **solo se ejecuta si cambió la carpeta de su microservicio**. De esa forma no se redespliega todo el sistema cada vez que tocas un solo servicio.

### Conexión a Cloud SQL

En Cloud Run los servicios se conectan a Cloud SQL usando el **Cloud SQL Socket Factory** de Google (la dependencia `postgres-socket-factory` que está en los `pom.xml`). La conexión se arma con una URL del estilo:

```
jdbc:postgresql:///feriando_db?cloudSqlInstance=<instancia>&socketFactory=com.google.cloud.sql.postgres.SocketFactory&currentSchema=<schema_del_ms>
```

La gracia es que no hay que abrir puertos ni armar túneles a mano: el socket factory de Google maneja la conexión segura contra la instancia de Cloud SQL. Los datos de conexión (`DB_URL`, `DB_USER`, `DB_PASS`) se pasan como variables de entorno en el despliegue.

## 16. ¿Por qué Google Cloud y no AWS?

Lo pensamos y nos quedamos con **Google Cloud** por razones bien concretas para este proyecto:

- **Tiene región en Santiago (`southamerica-west1`).** Como nuestros usuarios serían chilenos, tener los servicios y la base de datos dentro del país baja la latencia y deja los datos en Chile.
- **Cloud Run es serverless y más simple.** Le pasas tu imagen Docker y queda andando, con HTTPS y escalado automático, sin tener que configurar servidores, balanceadores ni redes a mano. Hacer lo mismo en AWS (con ECS/Fargate) tiene bastantes más piezas que armar para llegar al mismo resultado.
- **Escala a cero.** Si nadie está usando un microservicio, Cloud Run lo apaga y deja de cobrar. Para un proyecto de universidad con poco tráfico eso significa que prácticamente no gastamos.
- **Capa gratuita generosa** y créditos iniciales que para nuestro caso alcanzan de sobra.
- **Cloud SQL se conecta fácil.** Con el `postgres-socket-factory` de Google, Spring Boot se conecta a la base sin abrir puertos ni montar túneles, que en RDS de AWS es más enredado.
- **El despliegue calza redondo con el resto.** Cloud Run + Artifact Registry + GitHub Actions + Cloud SQL es un combo que se integra sin fricción, y es justo el que armamos.

En resumen: AWS también podría hacer todo esto, pero para un proyecto de este tamaño Google Cloud nos dejó desplegar **más rápido, más barato y con todo alojado en Chile**.

## 17. Estructura de carpetas

```
feriando.finalll/
├── api-gateway/            # Spring Cloud Gateway (puerta de entrada)
├── ms-usuario/             # microservicio de usuarios
├── ms-feriante/            # microservicio de feriantes
├── ms-producto/            # catálogo (productos + categorías)
├── ms-inventario/          # stock
├── ms-carrito/             # carrito de compras
├── ms-pedido/              # pedidos
├── ms-despacho/            # despacho / entrega
├── ms-pago/                # pagos
├── ms-calificacion/        # calificaciones
├── ms-reporte/             # reportes de ventas
├── .github/workflows/      # un workflow de deploy por servicio
├── schema.sql              # estructura de la base + datos de ejemplo
└── README.md
```

Y dentro de cada microservicio, el patrón CSR:

```
ms-usuario/
├── Dockerfile
├── pom.xml
└── src/
    ├── main/
    │   ├── java/cl/feriando/usuario/
    │   │   ├── controller/     # endpoints REST
    │   │   ├── service/        # lógica de negocio
    │   │   ├── repository/     # acceso a datos (JPA)
    │   │   ├── model/          # entidades
    │   │   ├── dto/            # objetos de entrada/salida
    │   │   ├── mapper/         # entidad <-> DTO
    │   │   ├── client/         # WebClient (si habla con otros MS)
    │   │   ├── config/         # configuración (beans, etc.)
    │   │   └── exception/      # excepciones + handler global
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/.../service/   # pruebas unitarias
```

## 18. Variables de entorno

Las que usan los servicios (con su valor por defecto para local):

| Variable | Para qué | Default |
|---|---|---|
| `PORT` | Puerto en el que levanta el servicio (lo asigna Cloud Run en la nube). | el puerto fijo de cada MS |
| `DB_URL` | URL JDBC de la base de datos. | `jdbc:postgresql://localhost:5432/feriando_db?currentSchema=<schema>` |
| `DB_USER` | Usuario de la base. | `postgres` |
| `DB_PASS` | Contraseña de la base. | (configurada localmente) |
| `feriando.ms-*.url` | URL de otros microservicios que se consumen (por ejemplo en `ms-pago`). | `http://localhost:<puerto>` |

> Nota: las credenciales reales (como la contraseña de la base) deberían ir siempre por variable de entorno o secretos, nunca quemadas en el código. En el despliegue se pasan como variables de entorno del servicio.

## 19. Cosas pendientes / a futuro

- **Seguridad (JWT / login):** la idea es que `ms-usuario` maneje autenticación con JWT y que las contraseñas se guarden encriptadas con BCrypt. La base ya guarda el campo `password_hash`, pero el login con tokens todavía no está implementado; es el siguiente paso natural del proyecto.
- **Filtros de autenticación en el Gateway:** una vez que esté el JWT, el gateway sería el lugar ideal para validar el token antes de dejar pasar las peticiones a los microservicios.
- **Más validaciones cruzadas:** por ahora algunas coherencias entre servicios (por ejemplo que el cliente de un pedido exista) se cuidan en los flujos principales; se podrían reforzar en más puntos.
