## Docker

Este proyecto incluye Docker para:

- `db`: MySQL 8.4 con el esquema inicial cargado desde `src/main/resources/db/storemanager.sql`
- `app`: imagen de la app JavaFX

### Base de datos

Levantar solo MySQL:

```powershell
docker compose up -d db
```

Credenciales del contenedor:

- Base: `storemanager`
- Usuario: `storemanager`
- Clave: `storemanager`
- Root: `root`

### App

Construir la imagen:

```powershell
docker compose build app
```

La app de escritorio necesita un servidor gráfico. El servicio `app` queda bajo el perfil `gui`:

```powershell
docker compose --profile gui up app
```

En Windows normalmente necesitarás un servidor X externo si quieres ver la UI del contenedor. Si solo quieres dockerizar la base y ejecutar la app localmente, usa `docker compose up -d db`.

En Linux, si quieres compartir el socket X11, lo más limpio es hacerlo con un `compose.override.yml` local.
