#!/bin/bash
# ============================================================================
# Script para agregar GSI2 a la tabla de milking-records
# HU-001: Consulta de Registros de Ordeño por Lactancia
# ============================================================================
# 
# PREREQUISITOS:
# - AWS CLI configurado con credenciales válidas
# - Permisos para modificar tablas DynamoDB
#
# USO:
# chmod +x update-milking-gsi.sh
# ./update-milking-gsi.sh [nombre-tabla]
#
# EJEMPLO:
# ./update-milking-gsi.sh cattle-milking-records-dev
# ============================================================================

set -e

# Configuración
TABLE_NAME="${1:-cattle-milking-records}"
GSI_NAME="GSI2-bovine-lactation-index"

echo "============================================"
echo "Creando GSI2 en tabla: $TABLE_NAME"
echo "Nombre del índice: $GSI_NAME"
echo "============================================"

# Verificar que la tabla existe
echo "[1/3] Verificando que la tabla existe..."
aws dynamodb describe-table --table-name "$TABLE_NAME" > /dev/null 2>&1 || {
    echo "ERROR: La tabla '$TABLE_NAME' no existe"
    exit 1
}
echo "✅ Tabla encontrada"

# Verificar si el GSI ya existe
echo "[2/3] Verificando si el GSI ya existe..."
EXISTING_GSI=$(aws dynamodb describe-table --table-name "$TABLE_NAME" \
    --query "Table.GlobalSecondaryIndexes[?IndexName=='$GSI_NAME'].IndexName" \
    --output text 2>/dev/null || echo "")

if [ "$EXISTING_GSI" == "$GSI_NAME" ]; then
    echo "⚠️  El GSI '$GSI_NAME' ya existe en la tabla"
    echo "No se requiere ninguna acción adicional"
    exit 0
fi

# Crear el GSI
echo "[3/3] Creando GSI2..."
aws dynamodb update-table \
    --table-name "$TABLE_NAME" \
    --attribute-definitions \
        AttributeName=gsi2pk,AttributeType=S \
        AttributeName=gsi2sk,AttributeType=S \
    --global-secondary-index-updates \
        "[{
            \"Create\": {
                \"IndexName\": \"$GSI_NAME\",
                \"KeySchema\": [
                    {\"AttributeName\": \"gsi2pk\", \"KeyType\": \"HASH\"},
                    {\"AttributeName\": \"gsi2sk\", \"KeyType\": \"RANGE\"}
                ],
                \"Projection\": {\"ProjectionType\": \"ALL\"}
            }
        }]"

echo ""
echo "============================================"
echo "✅ GSI2 creado exitosamente"
echo "============================================"
echo ""
echo "NOTA: El índice puede tardar varios minutos en estar disponible."
echo "Monitorea el estado con:"
echo ""
echo "  aws dynamodb describe-table --table-name $TABLE_NAME \\"
echo "      --query 'Table.GlobalSecondaryIndexes[?IndexName==\`$GSI_NAME\`].IndexStatus'"
echo ""
echo "El estado debe cambiar de 'CREATING' a 'ACTIVE'"
