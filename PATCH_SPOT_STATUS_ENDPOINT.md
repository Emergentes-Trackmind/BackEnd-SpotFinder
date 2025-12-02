# Endpoint PATCH para Cambio de Estado de Spots

## 📋 Descripción
Se ha implementado un endpoint PATCH para permitir el cambio manual del estado de los spots (plazas de parking) desde el dashboard web.

## 🔗 Endpoint

```
PATCH /api/parkings/{parkingId}/spots/{spotId}
```

### Parámetros de Ruta
- `parkingId` (Long): ID del parking
- `spotId` (String/UUID): ID único del spot

### Headers Requeridos
```
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

### Body Request
```json
{
  "status": "AVAILABLE" | "OCCUPIED" | "RESERVED"
}
```

## ✅ Respuestas

### 200 OK - Éxito
Retorna el spot actualizado:
```json
{
  "id": "2c41cfd9-46a8-4512-9c6c-b98635c0ea20",
  "parkingId": "9",
  "label": "A1",
  "status": "AVAILABLE",
  "rowIndex": 1,
  "columnIndex": 1
}
```

### 400 Bad Request - Error de validación
```json
{
  "error": "Estado inválido. Valores permitidos: AVAILABLE, OCCUPIED, RESERVED"
}
```
O:
```json
{
  "error": "ID de spot inválido"
}
```
O:
```json
{
  "error": "Se requiere el campo 'status'"
}
```

### 401 Unauthorized - Sin autenticación
```json
{
  "error": "Token de autenticación requerido"
}
```

### 403 Forbidden - No es el propietario
```json
{
  "error": "No tienes permisos para modificar este parking"
}
```

### 404 Not Found - Recurso no encontrado
```json
{
  "error": "Parking no encontrado"
}
```
O:
```json
{
  "error": "Spot no encontrado"
}
```

### 500 Internal Server Error
```json
{
  "error": "Error al actualizar el spot: <mensaje>"
}
```

## 🔐 Seguridad

El endpoint incluye:
1. **Autenticación JWT**: Requiere token Bearer válido
2. **Validación de propiedad**: Solo el propietario del parking puede modificar sus spots
3. **Validación de estados**: Solo acepta estados válidos del enum `ParkingSpotStatus`

## 📝 Validaciones

1. **Token JWT**: Se verifica que exista y sea válido
2. **Parking existe**: Se valida que el parking exista en la base de datos
3. **Propiedad**: El usuario autenticado debe ser el dueño del parking
4. **Spot existe**: El spot debe existir y pertenecer al parking especificado
5. **Estado válido**: El nuevo estado debe ser uno de: AVAILABLE, OCCUPIED, RESERVED
6. **UUID válido**: El spotId debe ser un UUID válido

## 🧪 Ejemplo de Uso

### Con cURL
```bash
curl -X PATCH \
  https://spotfinderback-eaehduf4ehh7hjah.eastus2-01.azurewebsites.net/api/parkings/9/spots/2c41cfd9-46a8-4512-9c6c-b98635c0ea20 \
  -H 'Authorization: Bearer eyJhbGc...' \
  -H 'Content-Type: application/json' \
  -d '{
    "status": "OCCUPIED"
  }'
```

### Con JavaScript (Fetch)
```javascript
const response = await fetch(
  `https://spotfinderback-eaehduf4ehh7hjah.eastus2-01.azurewebsites.net/api/parkings/9/spots/2c41cfd9-46a8-4512-9c6c-b98635c0ea20`,
  {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      status: 'OCCUPIED'
    })
  }
);

const data = await response.json();
```

### Con Angular HttpClient
```typescript
updateSpotStatus(parkingId: string, spotId: string, status: string): Observable<SpotResponse> {
  return this.http.patch<SpotResponse>(
    `${this.baseUrl}/parkings/${parkingId}/spots/${spotId}`,
    { status }
  );
}
```

## 🎯 Estados Disponibles

| Estado | Descripción |
|--------|-------------|
| `AVAILABLE` | Plaza disponible para uso |
| `OCCUPIED` | Plaza ocupada |
| `RESERVED` | Plaza reservada |

## ⚠️ Notas Importantes

1. **Case-insensitive**: El estado se convierte a mayúsculas automáticamente
2. **Persistencia**: El cambio se guarda inmediatamente en la base de datos
3. **No afecta otros endpoints**: Este endpoint es independiente y no modifica el comportamiento de:
   - `GET /api/parkings/{parkingId}/spots` (listar spots)
   - `POST /api/parkings/{parkingId}/spots` (crear spot)
   - Otros endpoints de parking

## 🔧 Implementación Técnica

### Archivo Modificado
- `ParkingsController.java`: Líneas 376-457

### Imports Agregados
```java
import com.spotfinder.backend.v1.parkingManagement.domain.model.queries.GetParkingSpotByIdQuery;
import java.util.UUID;
```

### Método Clave
```java
@PatchMapping("/parkings/{parkingId}/spots/{spotId}")
public ResponseEntity<?> updateSpotStatus(
    @PathVariable Long parkingId,
    @PathVariable String spotId,
    @RequestBody Map<String, Object> body,
    HttpServletRequest request
)
```

## ✅ Integración con Frontend

El frontend ya tiene el método `updateSpotStatus` en el servicio `SpotsService`:

```typescript
// frontend/FrontEnd-Web-SpotFinder/src/app/profileparking/services/spots.service.ts
updateSpotStatus(parkingId: string, spotId: string, status: SpotStatus): Observable<SpotResponse> {
  return this.http.patch<SpotResponse>(
    `${this.baseUrl}/${parkingId}/spots/${spotId}`, 
    { status }
  );
}
```

Ahora este método funcionará correctamente con el nuevo endpoint del backend.

## 🚀 Estado del Despliegue

El endpoint está listo para desplegarse en Azure:
- URL: `https://spotfinderback-eaehduf4ehh7hjah.eastus2-01.azurewebsites.net`
- No requiere cambios en la base de datos
- Compatible con la aplicación móvil existente
- No afecta funcionalidades existentes
