#!/usr/bin/env python3
"""
============================================================================
Script para migrar registros de milking existentes agregando GSI2PK y GSI2SK
HU-001: Consulta de Registros de Ordeño por Lactancia
============================================================================

PREREQUISITOS:
- Python 3.8+
- boto3 instalado (pip install boto3)
- AWS CLI configurado con credenciales válidas

USO:
python migrate-milking-records.py [--dry-run] [--table-milking TABLE] [--table-bovineIdentityItems TABLE]

EJEMPLO:
python migrate-milking-records.py --dry-run
python migrate-milking-records.py --table-milking cattle-milking-records-dev

============================================================================
"""

import boto3
import argparse
from datetime import datetime
from typing import Optional, List, Dict, Any

# Configuración por defecto
DEFAULT_TABLE_MILKING = "cattle-milking-records"
DEFAULT_TABLE_BOVINES = "cattle-bovineIdentityItems"


def get_dynamodb_resource():
    """Obtiene el recurso DynamoDB"""
    return boto3.resource('dynamodb')


def get_lactation_for_bovine(bovines_table, bovine_id: int) -> Optional[Dict[str, Any]]:
    """
    Obtiene la lactancia OPEN para un bovino.
    Busca en el modelo bovineIdentityItem-lact la lactancia activa.
    """
    pk = f"BOVINE#{bovine_id}"
    
    response = bovines_table.query(
        KeyConditionExpression='PK = :pk AND begins_with(SK, :sk_prefix)',
        ExpressionAttributeValues={
            ':pk': pk,
            ':sk_prefix': 'LACT#'
        }
    )
    
    items = response.get('Items', [])
    
    # Buscar lactancia OPEN (activa)
    for lact in items:
        if lact.get('status', '').upper() == 'OPEN':
            return lact
    
    # Si no hay OPEN, retornar la más reciente (para datos históricos)
    if items:
        # Ordenar por lactationNumber descendente
        sorted_lacts = sorted(
            items, 
            key=lambda x: int(x.get('lactationNumber', '0')), 
            reverse=True
        )
        return sorted_lacts[0]
    
    return None


def generate_gsi2_keys(bovine_id: int, lactation_number: int, date: str, shift: str) -> tuple:
    """
    Genera las claves GSI2PK y GSI2SK para un registro de milking.
    
    GSI2PK: BOVINE#<id>#LACT#<nn>
    GSI2SK: <date>#<shift>
    """
    lact_num_str = str(lactation_number).zfill(2)
    gsi2pk = f"BOVINE#{bovine_id}#LACT#{lact_num_str}"
    gsi2sk = f"{date}#{shift}"
    return gsi2pk, gsi2sk


def migrate_record(milking_table, bovines_table, item: Dict[str, Any], dry_run: bool = False) -> bool:
    """
    Migra un registro de milking agregando GSI2PK, GSI2SK y lactationNumber.
    Retorna True si se migró exitosamente.
    """
    pk = item.get('PK')
    sk = item.get('SK')
    bovine_id = item.get('bovineId')
    date = item.get('date')
    shift = item.get('shift')
    
    # Verificar si ya tiene GSI2
    if item.get('gsi2pk') and item.get('gsi2sk'):
        return False  # Ya migrado
    
    if not all([pk, sk, bovine_id, date, shift]):
        print(f"  ⚠️  Registro incompleto: {pk}/{sk}")
        return False
    
    # Obtener lactancia del bovino
    lactation = get_lactation_for_bovine(bovines_table, bovine_id)
    
    if not lactation:
        print(f"  ⚠️  Sin lactancia para bovino {bovine_id}: {pk}/{sk}")
        return False
    
    lactation_number = int(lactation.get('lactationNumber', '1'))
    gsi2pk, gsi2sk = generate_gsi2_keys(bovine_id, lactation_number, date, shift)
    
    if dry_run:
        print(f"  [DRY-RUN] {pk}/{sk} → GSI2PK={gsi2pk}, GSI2SK={gsi2sk}, lactationNumber={lactation_number}")
        return True
    
    # Actualizar el registro
    try:
        milking_table.update_item(
            Key={'PK': pk, 'SK': sk},
            UpdateExpression='SET lactationNumber = :ln, gsi2pk = :gsi2pk, gsi2sk = :gsi2sk',
            ExpressionAttributeValues={
                ':ln': lactation_number,
                ':gsi2pk': gsi2pk,
                ':gsi2sk': gsi2sk
            }
        )
        print(f"  ✅ Migrado: {pk}/{sk} → Lactancia #{lactation_number}")
        return True
    except Exception as e:
        print(f"  ❌ Error migrando {pk}/{sk}: {e}")
        return False


def scan_all_records(table) -> List[Dict[str, Any]]:
    """Escanea todos los registros de la tabla"""
    items = []
    response = table.scan()
    items.extend(response.get('Items', []))
    
    while 'LastEvaluatedKey' in response:
        response = table.scan(ExclusiveStartKey=response['LastEvaluatedKey'])
        items.extend(response.get('Items', []))
    
    return items


def main():
    parser = argparse.ArgumentParser(description='Migrar registros de milking con GSI2')
    parser.add_argument('--dry-run', action='store_true', 
                        help='Simular migración sin hacer cambios')
    parser.add_argument('--table-milking', default=DEFAULT_TABLE_MILKING,
                        help=f'Nombre de la tabla de milking (default: {DEFAULT_TABLE_MILKING})')
    parser.add_argument('--table-bovineIdentityItems', default=DEFAULT_TABLE_BOVINES,
                        help=f'Nombre de la tabla de bovinos (default: {DEFAULT_TABLE_BOVINES})')
    
    args = parser.parse_args()
    
    print("=" * 60)
    print("MIGRACIÓN DE REGISTROS DE MILKING - GSI2")
    print("=" * 60)
    print(f"Tabla Milking: {args.table_milking}")
    print(f"Tabla Bovines: {args.table_bovines}")
    print(f"Modo: {'DRY-RUN (simulación)' if args.dry_run else 'PRODUCCIÓN'}")
    print("=" * 60)
    print()
    
    dynamodb = get_dynamodb_resource()
    milking_table = dynamodb.Table(args.table_milking)
    bovines_table = dynamodb.Table(args.table_bovines)
    
    print("[1/3] Escaneando registros de milking...")
    records = scan_all_records(milking_table)
    print(f"  Encontrados: {len(records)} registros")
    print()
    
    print("[2/3] Migrando registros...")
    migrated = 0
    skipped = 0
    errors = 0
    
    for record in records:
        result = migrate_record(milking_table, bovines_table, record, args.dry_run)
        if result:
            migrated += 1
        elif record.get('gsi2pk'):
            skipped += 1
        else:
            errors += 1
    
    print()
    print("[3/3] Resumen de migración")
    print("=" * 60)
    print(f"  Total registros:     {len(records)}")
    print(f"  Migrados:            {migrated}")
    print(f"  Ya migrados (skip):  {skipped}")
    print(f"  Errores/Incompletos: {errors}")
    print("=" * 60)
    
    if args.dry_run:
        print()
        print("⚠️  MODO DRY-RUN: No se realizaron cambios reales")
        print("   Ejecuta sin --dry-run para aplicar la migración")
    else:
        print()
        print("✅ Migración completada")


if __name__ == '__main__':
    main()
