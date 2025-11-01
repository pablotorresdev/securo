# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Conitrack** is a Spring Boot 3.4.1 application for batch/lot tracking and inventory management with comprehensive traceability. It manages the lifecycle of product lots from purchase/production through analysis, quarantine, sales, and returns. The system uses PostgreSQL for persistence and Thymeleaf for server-side rendering.

## Build and Development Commands

### Gradle Commands
```bash
# Build the project (skip tests)
./gradlew clean build -x test

# Build JAR for deployment
./gradlew clean bootJar -x test

# Compile Java only
./gradlew compileJava

# Run tests
./gradlew test

# Run tests with fresh state
./gradlew cleanTest test

# Generate JaCoCo test coverage report (HTML in build/reports/jacoco)
./gradlew jacocoTestReport
```

### Docker Commands
```bash
# Build and start all containers (PostgreSQL + Spring Boot app)
docker-compose up -d

# Build images only
docker-compose build

# Stop all containers
docker-compose down

# View Spring Boot logs
docker logs spring-app

# View PostgreSQL logs
docker logs postgres-db

# Access PostgreSQL shell
docker exec -it postgres-db psql -U postgres -d conitrack
```

### Running Tests
- **Run all tests**: `./gradlew test`
- **Run specific test class**: `./gradlew test --tests ClassName`
- **Run test method**: `./gradlew test --tests ClassName.methodName`
- Test coverage excludes: config, entity, dto, exception, and Application classes

## Environment Profiles

- **DEV Profile** (application-DEV.yml): Local development with localhost PostgreSQL, `ddl-auto: update`
- **Production Profile** (application.yml): Uses environment variables for database connection, `ddl-auto: none`
- Set profile via `SPRING_PROFILES_ACTIVE` environment variable

## Architecture Overview

### Domain Model - Core Entities

The system revolves around tracking **Lotes** (batches/lots) through their lifecycle:

1. **Lote** (src/main/java/com/mb/conitrack/entity/Lote.java): Central entity representing a batch of product
   - Tracks quantities (initial/current), units of measure, expiration dates
   - Contains genealogy tracking via `loteOrigen` (for lots derived from other lots like production batches)
   - Has methods: `duplicateNumber()`, `getRootLote()` with circular reference protection (MAX_GENEALOGY_DEPTH=100)
   - Related to: Producto, Proveedor, Fabricante
   - Status tracked via `EstadoEnum` and `DictamenEnum` (APROBADO, RECHAZADO, CUARENTENA)

2. **Movimiento** (src/main/java/com/mb/conitrack/entity/Movimiento.java): Records all lot transactions
   - Types: ALTA (purchase/production), BAJA (sale/consumption), MODIFICACION (analysis/status changes)
   - Tracks `dictamenInicial` and `dictamenFinal` for status transitions
   - Can reference `movimientoOrigen` for reversals/returns
   - Contains `DetalleMovimiento` for line items

3. **Bulto** (entity/Bulto.java): Individual packages/containers within a Lote
   - Tracks individual quantities and units per package
   - Each lot can have multiple bultos (e.g., 10 boxes of 50kg each)

4. **Analisis** (entity/Analisis.java): Quality control analysis results
   - Linked to Lote, tracks analysis number, result (dictamen), dates (reanalysis, expiration)
   - Supports multiple analyses per lot (reanalysis scenarios)

5. **Traza** (entity/Traza.java): Traceability records for tracking lot usage in downstream products

### Service Layer - Use Case Pattern

The system implements **Use Case Controllers** (CU) in `controller/cu/` and `service/cu/` following a specific naming pattern:

- **Alta** = Create/Inbound operations (e.g., `AltaIngresoCompraService` for purchases)
- **Baja** = Reduce/Outbound operations (e.g., `BajaVentaProductoService` for sales)
- **Modif** = Modification operations (e.g., `ModifResultadoAnalisisService` for analysis results)

**Key Use Cases:**
- Alta: IngresoCompra, IngresoProduccion, DevolucionVenta, RetiroMercado
- Baja: VentaProducto, ConsumoProduccion, MuestreoBulto, DevolucionCompra
- Modif: ResultadoAnalisis, AnulacionAnalisis, TrazadoLote, LiberacionVentas, ReanalisisLote, DictamenCuarentena, ReversoMovimiento

All use case services extend `AbstractCuService` which provides common validation methods:
- `validarBultos()`, `validarCantidades()`, `validarFechas()`, `validarNroAnalisis()`
- Country list generation, unit conversion validation

All use case controllers extend `AbstractCuController` which injects common services: `LoteService`, `ProductoService`, `ProveedorService`, `AnalisisService`

### Repository Layer
Standard Spring Data JPA repositories in `repository/` package:
- Domain repositories: `LoteRepository`, `MovimientoRepository`, `BultoRepository`, etc.
- Master data in `repository/maestro/`: `ProductoRepository`, `ProveedorRepository`, `UserRepository`

### Utility Classes
- **LoteEntityUtils** (utils/LoteEntityUtils.java): Factory methods for creating Lote entities
- **MovimientoEntityUtils** (utils/MovimientoEntityUtils.java): Factory methods for Movimiento entities
- **UnidadMedidaUtils** (utils/UnidadMedidaUtils.java): Unit of measure conversions
- **DTOUtils** (dto/DTOUtils.java): Entity to DTO conversions

### Soft Delete Pattern
Entities use `@SQLDelete` annotation to implement soft deletes (sets `activo = false` instead of physical deletion). Always filter by `activoTrue` in queries.

## Database Management

### Backup and Restore
The PostgreSQL container includes automated backups every 10 minutes to `/backups`.

**Manual backup:**
```bash
.\data-base\custom_backup.sh
```

**Restore backup:**
```bash
# 1. Copy backup file to container
docker cp backups/backup_YYYY-MM-DD_HH-MM-SS.sql postgres-db:/backup.sql

# 2. Terminate connections and drop database
docker exec -it postgres-db psql -U postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'conitrack';"
docker exec -it postgres-db psql -U postgres -c "DROP DATABASE conitrack;"

# 3. Recreate and restore
docker exec -it postgres-db psql -U postgres -c "CREATE DATABASE conitrack;"
docker exec -it postgres-db psql -U postgres -d conitrack -f /backup.sql
```

## Security
- Spring Security configured in `config/SecurityConfig.java`
- User authentication via `CustomUserDetailsService`
- Role-based access control with `Role` and `User` entities

## Testing Guidelines
- Tests located in `src/test/java/com/mb/conitrack/`
- Test structure mirrors main source structure (controller, service, entity, etc.)
- Use Mockito for mocking dependencies
- Lombok available in test scope

## Key Enums
- **EstadoEnum**: VIGENTE, SUSPENDIDO, CUARENTENA, etc.
- **DictamenEnum**: APROBADO, RECHAZADO, CUARENTENA, PENDIENTE
- **TipoMovimientoEnum**: ALTA, BAJA, MODIFICACION
- **MotivoEnum**: COMPRA, VENTA, PRODUCCION, ANALISIS, etc.
- **UnidadMedidaEnum**: KILOGRAMO, GRAMO, LITRO, MILILITRO, UNIDAD (with conversion factors)
- **TipoProductoEnum**: Product type classification

## Important Implementation Notes

1. **Date Handling**: System uses `OffsetDateTime` for creation timestamps and `LocalDate` for business dates
2. **Decimal Precision**: Quantities use `BigDecimal` with precision 12, scale 4
3. **Lazy Loading**: Most entity relationships are `FetchType.LAZY`, use `@ToString(exclude={...})` to prevent LazyInitializationException
4. **Validation**: Use validation groups like `AltaCompra`, `AltaProduccion`, `ValidacionBaja`, `ValidacionModificacion` in DTOs
5. **Genealogy Protection**: When working with `loteOrigen` chains, be aware of circular reference protection (max depth 100)
6. **Unit Conversions**: Always use `UnidadMedidaUtils` for converting between units, never manual calculations
