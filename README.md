# StoreManager

Aplicacion de escritorio para gestion de tienda, inventario, ventas, clientes, proveedores y reportes.

El proyecto esta hecho en:

- Java 17
- JavaFX
- Maven
- MySQL
- FlatLaf

## Modulos principales

- Dashboard
- Productos
- Ventas
- Gestion de stock
- Clientes
- Proveedores
- Reportes

## Estructura del proyecto

```text
src/main/java/com/manager/storemanager
|- config
|- dao
|- fx
|- model
|- service
|- ui
|- util

src/main/resources
|- css
|- db
|- db.properties
```

Notas:

- `fx/` contiene la interfaz principal actual en JavaFX.
- `dao/` contiene el acceso a base de datos.
- `service/` concentra la logica de negocio.
- `src/main/resources/db/storemanager.sql` crea la base y carga datos iniciales.

## Requisitos

- JDK 17
- Maven 3.9+
- MySQL 8+
- Apache NetBeans 29 o compatible con Maven + JavaFX

## Ejecucion local

### 1. Crear la base de datos

Importa el script:

```sql
src/main/resources/db/storemanager.sql
```

Configuracion local por defecto:

```properties
db.url=jdbc:mysql://localhost:3306/storemanager?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=root
```

Ese archivo esta en:
[db.properties](/C:/Hijosdelsol/StoreManager/src/main/resources/db.properties)

### 2. Ejecutar con Maven

```powershell
mvn javafx:run
```

### 3. Ejecutar desde NetBeans

El proyecto ya incluye una accion `Run Project` preparada para JavaFX.

Si NetBeans no responde bien al boton verde:

1. Click derecho sobre el proyecto.
2. Elige `Reload Project`.
3. Marca `Set as Main Project`.
4. Vuelve a usar `Run Project`.

## Credenciales de prueba

La app incluye usuarios semilla en la base de datos:

- Usuario: `admin`
- Usuario: `cajero`

La vista de login muestra accesos de prueba listos para autocompletar.

## Docker

Este repositorio ya incluye soporte Docker:

- `db`: MySQL 8.4 con carga automatica del esquema
- `app`: imagen de la app JavaFX

### Levantar solo la base

```powershell
docker compose up -d db
```

Credenciales del contenedor:

- Base: `storemanager`
- Usuario: `storemanager`
- Clave: `storemanager`
- Root: `root`

### Construir la imagen de la app

```powershell
docker compose build app
```

### Ejecutar la app en contenedor

```powershell
docker compose --profile gui up app
```

Importante:

- La app es de escritorio, asi que el contenedor necesita salida grafica.
- En Windows normalmente necesitaras un servidor X externo para ver la UI.
- Si solo quieres usar Docker para la base y ejecutar la app localmente, usa solo `db`.

## Variables de entorno soportadas

La conexion a base de datos tambien puede leerse desde variables de entorno:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`

Esto permite ejecutar el proyecto localmente o dentro de Docker sin modificar el codigo.

## Estado actual

- La interfaz principal funciona con componentes nativos JavaFX.
- Se eliminaron los HTML usados anteriormente en la UI activa.
- El proyecto ya esta publicado en GitHub y preparado para seguir creciendo desde `main`.

## Recomendaciones

- No uses `root/root` en produccion.
- Crea una base de datos y un usuario dedicados para despliegue real.
- Si trabajas en NetBeans, mantente en JDK 17 para evitar roces con JavaFX 17.
