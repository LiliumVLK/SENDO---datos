"""Publica los planes de SENDO para la app de clientes, cifrados con la contraseña de cada cuenta.

Uso: python3 -I herramientas/publicar.py <carpeta_exportada> [<raiz_del_repo>]

<carpeta_exportada> contiene lo que se ha leído de la base de datos de la web SENDO:
  cuentas.json            lista [{"id": ..., "data": {...}}] de la colección «cuentas»
  semanas/<id>.json       lista [{"id": "AAAA-MM-DD", "data": {...}}] de «planes/<id>/semanas»

Cada cuenta se guarda en c/<id>.json, donde <id> y la clave salen de PBKDF2(contraseña, "sendo|"+nombre).
Sin el nombre y la contraseña no se puede saber de quién es un archivo ni leerlo. Las contraseñas no se guardan.
Imprime ACTUALIZADO o SIN CAMBIOS.
"""
import base64
import datetime
import hashlib
import json
import os
import sys

from cryptography.hazmat.primitives.ciphers.aead import AESGCM

ITER = 150000
QUITAR = ("note", "updatedAt", "fuente")


def norm(s):
    return " ".join(str(s or "").split()).lower()


def derive(name, pw):
    b = hashlib.pbkdf2_hmac("sha256", pw.encode("utf-8"), ("sendo|" + norm(name)).encode("utf-8"), ITER, 48)
    return b[:32], b[32:48].hex()


def leer(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def main(exp, repo):
    cuentas = leer(os.path.join(exp, "cuentas.json"))
    out = os.path.join(repo, "c")
    os.makedirs(out, exist_ok=True)
    keep, cambios, n = set(), 0, 0
    for c in cuentas:
        d = c.get("data") or {}
        name, pw = d.get("nombre"), d.get("clave")
        if not name or not pw:
            continue
        key, fid = derive(name, pw)
        sem = {}
        p = os.path.join(exp, "semanas", c["id"] + ".json")
        if os.path.exists(p):
            for s in leer(p):
                doc = {k: v for k, v in (s.get("data") or {}).items() if k not in QUITAR}
                if doc.get("days"):
                    sem[s["id"]] = doc
        plain = json.dumps({"v": 1, "id": c["id"], "nombre": name, "semanas": sem},
                           ensure_ascii=False, sort_keys=True, separators=(",", ":")).encode("utf-8")
        fpath = os.path.join(out, fid + ".json")
        keep.add(fid + ".json")
        n += 1
        if os.path.exists(fpath):
            try:
                old = leer(fpath)
                if AESGCM(key).decrypt(base64.b64decode(old["iv"]), base64.b64decode(old["ct"]), None) == plain:
                    continue
            except Exception:
                pass
        iv = os.urandom(12)
        ct = AESGCM(key).encrypt(iv, plain, None)
        with open(fpath, "w", encoding="utf-8") as f:
            json.dump({"v": 1, "iv": base64.b64encode(iv).decode(), "ct": base64.b64encode(ct).decode()}, f)
        cambios += 1
    for fn in os.listdir(out):
        if fn.endswith(".json") and fn not in keep:
            os.remove(os.path.join(out, fn))
            cambios += 1
    if cambios:
        with open(os.path.join(repo, "estado.json"), "w", encoding="utf-8") as f:
            json.dump({"at": datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"), "cuentas": n}, f)
        print(f"ACTUALIZADO ({n} cuentas, {cambios} archivos cambiados)")
    else:
        print(f"SIN CAMBIOS ({n} cuentas)")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
