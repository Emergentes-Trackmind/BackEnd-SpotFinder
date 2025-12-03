# IoT Integration for Parking Spots - Implementation Summary

## Overview
This implementation adds IoT sensor integration capabilities to the ParkingSpot entity, allowing spots to be linked with physical IoT sensors and updated via telemetry data.

## Components Created/Modified

### 1. Domain Layer

#### New Enum: `ParkingSpotIotStatus`
- **Location**: `domain/model/valueobjects/ParkingSpotIotStatus.java`
- **Values**: 
  - `CONNECTED` - Sensor is actively sending data
  - `OFFLINE` - Sensor is not connected or not assigned

#### Modified Entity: `ParkingSpot`
- **Location**: `domain/model/entities/ParkingSpot.java`
- **New Fields**:
  - `iotStatus` (Enum): Tracks IoT connection status (default: OFFLINE)
  - `sensorSerialNumber` (String): Unique identifier for the linked IoT device
- **New Methods**:
  - `assignIotSensor(String serialNumber)`: Assigns a sensor to the spot
  - `updateByTelemetry(boolean occupied)`: Updates spot status based on sensor data

#### New Commands:
1. **`AssignIotSensorCommand`**
   - Parameters: `spotId`, `sensorSerialNumber`
   - Purpose: Link an IoT sensor to a parking spot

2. **`UpdateSpotByTelemetryCommand`**
   - Parameters: `sensorSerialNumber`, `occupied`
   - Purpose: Update spot status from telemetry data

### 2. Application Layer

#### New Repository: `ParkingSpotRepository`
- **Location**: `infrastructure/persistence/jpa/repositories/ParkingSpotRepository.java`
- **Methods**:
  - `findBySensorSerialNumber(String)`: Lookup spot by sensor serial
  - `existsBySensorSerialNumber(String)`: Check if serial number is already assigned

#### Updated Service: `ParkingCommandServiceImpl`
- Added dependency injection for `ParkingSpotRepository`
- **New Handler Methods**:
  1. `handle(AssignIotSensorCommand)`: Validates and assigns sensor to spot
  2. `handle(UpdateSpotByTelemetryCommand)`: Updates spot via sensor data

### 3. Interface Layer (REST API)

#### New Controller: `ParkingSpotsController`
- **Base Path**: `/api/spots` or `/api/v1/spots`

#### Endpoints:

##### 1. Assign IoT Sensor to Spot
```http
PUT /api/v1/spots/{spotId}/assign-iot
Content-Type: application/json

{
  "sensorSerialNumber": "SENSOR-12345"
}
```

**Response**: ParkingSpotResource with updated IoT fields

**Validations**:
- Spot must exist
- Sensor serial number must be unique (not already assigned)
- Sets `iotStatus` to `OFFLINE` initially

##### 2. Update Spot by Telemetry
```http
POST /api/v1/spots/telemetry
Content-Type: application/json

{
  "sensorSerialNumber": "SENSOR-12345",
  "occupied": true
}
```

**Response**: ParkingSpotResource with updated status

**Logic**:
- Finds spot by sensor serial number
- Updates `status` to `OCCUPIED` if `occupied=true`, `AVAILABLE` if `occupied=false`
- Sets `iotStatus` to `CONNECTED`

#### Updated Resource: `ParkingSpotResource`
- Added fields: `iotStatus`, `sensorSerialNumber`

## Usage Flow

### 1. Initial Setup (Assign Sensor)
```bash
# Assign IoT sensor to a parking spot
curl -X PUT http://localhost:8080/api/v1/spots/{spotId}/assign-iot \
  -H "Content-Type: application/json" \
  -d '{"sensorSerialNumber": "SENSOR-001"}'
```

### 2. Telemetry Updates (From IoT/Edge Server)
```bash
# When sensor detects a car (occupied)
curl -X POST http://localhost:8080/api/v1/spots/telemetry \
  -H "Content-Type: application/json" \
  -d '{"sensorSerialNumber": "SENSOR-001", "occupied": true}'

# When sensor detects spot is free
curl -X POST http://localhost:8080/api/v1/spots/telemetry \
  -H "Content-Type: application/json" \
  -d '{"sensorSerialNumber": "SENSOR-001", "occupied": false}'
```

## Database Changes Required

Before running the application, ensure your database schema includes these new columns:

```sql
ALTER TABLE parking_spot 
ADD COLUMN iot_status VARCHAR(50),
ADD COLUMN sensor_serial_number VARCHAR(255) UNIQUE;
```

Or let Spring Boot auto-generate the schema if you have `spring.jpa.hibernate.ddl-auto=update` configured.

## Key Features

✅ **Unique Sensor Assignment**: Each sensor can only be assigned to one spot at a time
✅ **Automatic Status Updates**: Telemetry data automatically updates spot availability
✅ **Connection Tracking**: IoT status tracks whether sensor is actively communicating
✅ **Serial Number Lookup**: Telemetry updates work with sensor serial number (no need for spot ID)
✅ **RESTful API**: Clean, documented endpoints with Swagger/OpenAPI annotations

## Error Handling

- **404 Not Found**: When spot ID or sensor serial number doesn't exist
- **400 Bad Request**: When trying to assign an already-assigned sensor serial number
- **IllegalArgumentException**: Thrown for invalid operations with descriptive messages

## Next Steps

1. Run the application to generate database schema changes
2. Test the endpoints using Postman or curl
3. Integrate with your Edge Server to call the telemetry endpoint
4. Consider adding authentication/authorization to the telemetry endpoint
5. Add monitoring/logging for IoT sensor communications

