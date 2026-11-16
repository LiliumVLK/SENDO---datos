# Tarea: Publicar SENDO para la app de clientes

Copiar los planes de la web SENDO (cuentas de clientes y sus semanas) a este repositorio (LiliumVLK/SENDO---datos),
cifrados por cliente, para que los lea la app Android de solo lectura «SENDO».
No cambies nada de la web. Nunca muestres, copies a un mensaje ni subas sin cifrar nombres de cuentas con contraseñas.

Web: https://claude.ai/artifact/SiTLNShyxfarDWuV8PvFtg — slug `d03e30a0-8825-4fb0-bf09-33b7c465f6b5`.
Su base de datos se lee con la herramienta `db_list` del servidor claude-artifacts (cárgala con ToolSearch "select:mcp__claude-artifacts__db_list").

1. Este repositorio ya está clonado en la sesión. Desde su raíz: `git checkout main && git pull origin main`.
2. Crea una carpeta vacía nueva fuera del repositorio (por ejemplo `/tmp/sendo-export-<hora>`), con una subcarpeta `semanas/`.
3. `db_list` slug de la web, collection `cuentas`, limit 1000 (pagina con nextCursor si lo hay). Escribe en
   `<carpeta>/cuentas.json` la lista `[{"id": <id>, "data": {"nombre": ..., "clave": ...}}, ...]` con cada documento
   (solo hacen falta nombre y clave). Si la lectura falla, termina con «error: no se pudo leer la web» sin tocar el repositorio.
4. Para cada cuenta, `db_list` collection `planes/<id>/semanas`, limit 1000, y escribe `<carpeta>/semanas/<id>.json` como
   `[{"id": "AAAA-MM-DD", "data": {...}}, ...]` copiando cada documento tal cual (sus días y comidas). Si no hay semanas, `[]`.
5. `python3 -I herramientas/publicar.py <carpeta>` (necesita `cryptography`; si falta: `pip install --break-system-packages cryptography`).
   - "SIN CAMBIOS": no hagas commit.
   - "ACTUALIZADO": `git add c estado.json && git commit -m "Planes <fecha>"` (autor Claude <noreply@anthropic.com>)
     `&& git pull --rebase origin main && git push origin HEAD:main`.
   - Error: no subas nada.
6. Borra la carpeta exportada (lleva contraseñas sin cifrar).
7. Termina con una línea: actualizado, sin cambios o el error, y el número de cuentas.
