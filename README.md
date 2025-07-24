# App de Rastreo de Contactos por BLE
## Integrantes del Proyecto

* **Ayma Cutipa Jordy Emanuel** - Porcentaje 100%
* **Chua Aguilar Jean Carlo Leonel** - Porcentaje 100%
* **Miyo Ollachica Anthony John** - Porcentaje 100%

## Descripción General

Esta aplicación utiliza tecnología Bluetooth Low Energy (BLE) para detectar y registrar contactos de proximidad entre usuarios de manera anónima, sin revelar ubicaciones exactas.

## Estructura de la Aplicación

### 1. Pantalla de Ingreso de Datos del Usuario

**Objetivo**: Solicitar y almacenar datos básicos del usuario de forma única durante la instalación.

**Características**:
- El usuario ingresa los datos **una sola vez** al instalar la aplicación
- Los datos se almacenan de forma local y segura (recomendado: DataStore)
- **Los datos no podrán ser modificados posteriormente**

**Datos solicitados**:
- Nombre
- Apellido

### 2. Pantalla de Advertencia y Activación de Escaneo

**Objetivo**: Informar al usuario sobre la activación del escaneo BLE para detectar personas cercanas.

**Requisitos Técnicos**:

#### Permisos necesarios:
- **Bluetooth**: Para emitir y escanear señales BLE
- **Ubicación**: Requerido por Android para el escaneo BLE
- **Internet**: Para sincronización de datos (si aplica)

#### Acciones:
- Activar BLE scanner (para detectar otros dispositivos)
- Activar BLE advertiser (para ser detectado por otros)

### 3. Módulo de Detección por Proximidad (BLE)

**Objetivo**: Escanear constantemente dispositivos cercanos con la aplicación instalada, manteniendo el anonimato del usuario.

#### Proceso Técnico:

1. **Emisión de ID anónimo**:
   - Generar y emitir un **UUID único** a través de BLE
   - Este ID no revela información personal del usuario

2. **Escaneo de dispositivos**:
   - Buscar continuamente otros dispositivos BLE cercanos
   - Filtrar solo dispositivos con la aplicación instalada

3. **Medición de proximidad**:
   - Medir la **intensidad de señal (RSSI)**
   - Registrar el contacto si RSSI > -70 (indica proximidad cercana)

4. **Almacenamiento de contactos**:
   - Guardar información del contacto detectado
   - Mantener registro temporal para análisis posterior

## Base de Datos

### Tabla: `detected_contacts`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | INTEGER (PK) | Identificador único autoincremental (clave primaria) |
| `detector_user_id` | UUID | ID del usuario actual (quien detecta) |
| `detected_user_id` | UUID | ID del usuario detectado por BLE |
| `detected_at` | TIMESTAMP | Fecha y hora de detección (formato ISO 8601) |
| `rssi` | INTEGER | Intensidad de la señal BLE para estimar cercanía |

## Consideraciones de Privacidad

- Los UUIDs son anónimos y no contienen información personal
- No se almacena ubicación GPS exacta
- Los datos se mantienen localmente en el dispositivo
- La proximidad se determina únicamente por intensidad de señal BLE

## Consideraciones Técnicas

### Umbrales de Proximidad
- **RSSI > -70**: Contacto cercano registrado
- **RSSI ≤ -70**: Contacto lejano, no registrado

### Optimización de Batería
- Implementar intervalos de escaneo eficientes
- Usar modos de bajo consumo cuando sea posible
- Optimizar frecuencia de advertiser BLE

## Flujo de Usuario

1. **Instalación**: Usuario ingresa datos básicos (una sola vez)
2. **Configuración**: Otorgar permisos necesarios
3. **Activación**: Iniciar escaneo BLE automático
4. **Funcionamiento**: La app funciona en segundo plano detectando contactos
5. **Registro**: Los contactos se almacenan automáticamente en la base de datos local

## Tecnologías Requeridas

- **BLE (Bluetooth Low Energy)**
- **DataStore** para almacenamiento local seguro
- **Base de datos local** (SQLite/Room)
- **Gestión de permisos** Android/iOS
- **Servicios en segundo plano**
