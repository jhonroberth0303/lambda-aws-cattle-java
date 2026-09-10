#!/usr/bin/env python3
"""
============================================================================
Migración: corregir el typo `birthType: DISTOXICO` -> `DISTOCICO` en los
eventos PARTO ya persistidos.
EP-20260909 (Configuración, catálogos e i18n), Fase 2.
============================================================================

Contexto:
- El <option> del formulario de PARTO y el valor persistido usaban "DISTOXICO"
  (typo de "DISTOCICO"). Al hacer PARTO schema-driven se corrigió el catálogo;
  este script reescribe los `payloadJson` históricos.
- La tabla de eventos ("Events") guarda cada evento como item pk/sk con el
  payload serializado en el atributo `payloadJson`. El `sk` NO contiene el
  birthType, así que solo hay que reescribir `payloadJson`.
- La lectura del front tolera ambos valores mientras la migración no termine.

PREREQUISITOS:
- Python 3.8+, boto3 (pip install boto3)
- AWS CLI configurado con credenciales del entorno objetivo

USO:
    python migrate-birthtype-distocico.py --dry-run                 # tabla "Events"
    python migrate-birthtype-distocico.py --table events-prod --dry-run
    python migrate-birthtype-distocico.py --table events-prod       # aplica
============================================================================
"""

import argparse
import json
from datetime import datetime, timezone

import boto3

DEFAULT_TABLE_EVENTS = "Events"
OLD_VALUE = "DISTOXICO"
NEW_VALUE = "DISTOCICO"


def scan_parto_events_with_typo(table):
    """Escanea PARTO cuyo payloadJson contiene el valor antiguo."""
    items = []
    kwargs = {
        "FilterExpression": "eventType = :t AND contains(payloadJson, :old)",
        "ExpressionAttributeValues": {":t": "PARTO", ":old": OLD_VALUE},
    }
    response = table.scan(**kwargs)
    items.extend(response.get("Items", []))
    while "LastEvaluatedKey" in response:
        response = table.scan(ExclusiveStartKey=response["LastEvaluatedKey"], **kwargs)
        items.extend(response.get("Items", []))
    return items


def rewrite_payload(payload_json):
    """Devuelve el payloadJson con birthType corregido, o None si no aplica."""
    try:
        payload = json.loads(payload_json)
    except (TypeError, ValueError):
        return None
    if not isinstance(payload, dict) or payload.get("birthType") != OLD_VALUE:
        return None
    payload["birthType"] = NEW_VALUE
    # Formato compacto, igual que Jackson writeValueAsString.
    return json.dumps(payload, ensure_ascii=False, separators=(",", ":"))


def migrate(table, dry_run):
    records = scan_parto_events_with_typo(table)
    print(f"[1/2] PARTO con '{OLD_VALUE}' en payloadJson: {len(records)}")

    updated = skipped = errors = 0
    for item in records:
        pk, sk = item.get("pk"), item.get("sk")
        new_json = rewrite_payload(item.get("payloadJson", ""))
        if new_json is None:
            # El match del filtro venía de otro campo (p. ej. notes); no tocar.
            skipped += 1
            continue
        if dry_run:
            print(f"  [DRY-RUN] {pk} / {sk}")
            updated += 1
            continue
        try:
            table.update_item(
                Key={"pk": pk, "sk": sk},
                UpdateExpression="SET payloadJson = :p, updatedAt = :u",
                ExpressionAttributeValues={
                    ":p": new_json,
                    ":u": datetime.now(timezone.utc).isoformat(),
                },
            )
            print(f"  OK {pk} / {sk}")
            updated += 1
        except Exception as exc:  # noqa: BLE001
            print(f"  ERROR {pk} / {sk}: {exc}")
            errors += 1

    print("[2/2] Resumen")
    print(f"  Reescritos:            {updated}")
    print(f"  Ignorados (otro campo): {skipped}")
    print(f"  Errores:               {errors}")
    if dry_run:
        print("\nMODO DRY-RUN: sin cambios reales. Ejecuta sin --dry-run para aplicar.")


def main():
    parser = argparse.ArgumentParser(description="Migrar birthType DISTOXICO -> DISTOCICO")
    parser.add_argument("--dry-run", action="store_true", help="Simular sin escribir")
    parser.add_argument("--table", default=DEFAULT_TABLE_EVENTS,
                        help=f"Tabla de eventos (default: {DEFAULT_TABLE_EVENTS})")
    args = parser.parse_args()

    print("=" * 60)
    print("MIGRACIÓN birthType DISTOXICO -> DISTOCICO (eventos PARTO)")
    print(f"Tabla: {args.table} | Modo: {'DRY-RUN' if args.dry_run else 'PRODUCCIÓN'}")
    print("=" * 60)

    table = boto3.resource("dynamodb").Table(args.table)
    migrate(table, args.dry_run)


if __name__ == "__main__":
    main()
