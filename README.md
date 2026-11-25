# SENDO---datos

Datos de la app de clientes **SENDO** (solo lectura). No editar a mano.

- `c/` — un archivo por cliente con su plan semanal, **cifrado** (AES-256-GCM) con una clave que sale de su nombre y su contraseña (PBKDF2-SHA256, 150 000 vueltas). Sin el nombre y la contraseña no se puede saber de quién es un archivo ni leerlo. Las contraseñas no se guardan aquí.
- `estado.json` — cuándo se publicó por última vez y cuántas cuentas hay.
- Lo publica automáticamente el repositorio privado `SENDO-proiektua/SENDO---Dev` (GitHub Actions) cada vez que cambia algo en la app SENDO Dev.
- `app/` — código de la app Android (sin la firma, que no se sube).

La app lee `https://raw.githubusercontent.com/SENDO-proiektua/SENDO---datos/main/c/<id>.json`, así que el repositorio tiene que ser **público**: el contenido va cifrado.
