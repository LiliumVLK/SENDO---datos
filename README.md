# SENDO---datos

Datos de la app de clientes **SENDO** (solo lectura). No editar a mano.

- `c/` — un archivo por cliente con su plan semanal, **cifrado** (AES-256-GCM) con una clave que sale de su nombre y su contraseña (PBKDF2-SHA256, 150 000 vueltas). Sin el nombre y la contraseña no se puede saber de quién es un archivo ni leerlo. Las contraseñas no se guardan aquí.
- `estado.json` — cuándo se publicó por última vez y cuántas cuentas hay.
- `herramientas/publicar.py` — genera `c/` a partir de lo exportado de la web SENDO.
- `tareas/publicar.md` — instrucciones de la tarea programada que copia la web a este repositorio.
- `app/` — código de la app Android (sin la firma, que no se sube).

La app lee `https://raw.githubusercontent.com/LiliumVLK/SENDO---datos/main/c/<id>.json`, así que el repositorio tiene que ser **público**: el contenido va cifrado.
