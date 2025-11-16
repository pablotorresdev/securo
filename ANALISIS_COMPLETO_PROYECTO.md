# ANÁLISIS COMPLETO DEL PROYECTO CONITRACK

**Fecha**: 2025-11-09
**Versión Analizada**: Commit 3f91547
**Analista**: Claude Code (Sonnet 4.5)

---

## RESUMEN EJECUTIVO

Conitrack es un sistema Spring Boot 3.4.1 de trazabilidad de lotes farmacéuticos con excelente arquitectura base pero con **14 vulnerabilidades de seguridad** (3 críticas), **20 problemas de código** prioritarios, y **10 servicios críticos sin tests** (30% cobertura en servicios CU).

**Contexto del Sistema**:
- 10 usuarios máximo, 10 operaciones diarias
- Actualmente en Heroku (~$12-22/mes)
- Requiere: backup diario, logging auditable, cumplimiento regulatorio (FDA/ANMAT)

**Puntuación General**: 6.5/10 (puede llegar a 9.5/10 con correcciones)

---

## PARTE 1: ARQUITECTURA Y DEPLOYMENT

### 1.1 Hosting Actual y Recomendación

**Estado Actual**: Heroku
- **Costo**: $12-22/mes
- **Backups**: Solo en planes $50+/mes
- **Problema**: Caro para 10 usuarios

**Recomendación**: Migrar a **Render**
- **Costo**: $14/mes (Web Service Starter + PostgreSQL Starter)
- **Backups**: Diarios automáticos incluidos
- **Retención**: 7 días (Starter), 30 días (Pro)
- **Restore**: Un click desde dashboard
- **Ya probado**: 6 commits en git evidencian testing previo

**Plan de Migración** (4-6 horas, downtime <5 min):
1. Crear PostgreSQL en Render
2. Backup Heroku → Restore en Render
3. Crear Web Service conectado a GitHub
4. Configurar variables de entorno
5. Deploy y testing
6. Cutover (actualizar DNS si aplica)
7. Monitorear 1 semana antes de eliminar Heroku

### 1.2 Configuraciones Críticas a Corregir

#### CRÍTICO 1: Logging en DEBUG
```yaml
# application.yml - CAMBIAR ANTES DE PRODUCCION
logging:
  level:
    org.springframework.security: DEBUG  # Cambiar a WARN
    org.springframework.web: DEBUG        # Cambiar a WARN
```
**Impacto**: Degradación de performance, logs gigantes, exposición de información sensible.

#### CRÍTICO 2: Error Exposure
```yaml
# application.yml - CAMBIAR
server:
  error:
    include-message: always  # Cambiar a 'never'
    include-binding-errors: always  # Cambiar a 'never'
```
**Impacto**: Expone detalles internos del sistema a atacantes.

#### CRÍTICO 3: Session Timeout Insuficiente
```yaml
# application.yml - AUMENTAR
server:
  servlet:
    session:
      timeout: 300s  # 5 min - Cambiar a 1800s (30 min)
```
**Impacto**: Timeouts frecuentes en operaciones de análisis largas.

#### CRÍTICO 4: Inconsistencia de Variables de Entorno
- **Dockerfile** usa: `PGHOST`, `PGPORT`, `PGDATABASE`
- **application.yml** usa: `PROD_DB_HOST`, `PROD_DB_PORT`, `PROD_DB_NAME`

**Solución**: Unificar en application.yml (Render usa PGHOST por defecto).

### 1.3 Sistema de Backups

**Docker Local** (actual):
- Cron job cada 12 horas
- **PROBLEMA**: Sin rotación → acumulación infinita
- **Solución**: Agregar a `backup.sh`:
  ```bash
  find /backups -name "backup_*.sql" -mtime +30 -delete
  ```

**Producción** (recomendado con Render):
- Backups diarios automáticos
- Retención 7-30 días según plan
- Restore con un click

---

## PARTE 2: SEGURIDAD (14 VULNERABILIDADES)

### VULNERABILIDADES CRÍTICAS (3)

#### V-001: Política de Contraseñas Débil
**Archivo**: `User.java:33`
```java
@Size(min = 3, message = "Password must be at least 3 characters")
```
**Riesgo**: Facilita ataques de fuerza bruta
**Solución**: Mínimo 12 caracteres + complejidad (mayúsculas, números, símbolos)

#### V-002: Usuarios por Defecto Predecibles
**Archivo**: `CustomUserDetailsService.java:60-69`
```java
createUserIfNotExists("admin", "admin", adminRole);
createUserIfNotExists("ptorres", "ptorres", adminRole);
// ... 7 usuarios más con password = username
```
**Riesgo**: Acceso no autorizado inmediato
**Solución**:
- Forzar cambio de contraseña en primer login
- Generar passwords aleatorias
- Eliminar credenciales del código

#### V-009: Credenciales en Texto Plano
**Archivos**:
- `application-DEV.yml:15-17` → `password: root`
- `docker-compose.yml:8-10` → `POSTGRES_PASSWORD: root`

**Riesgo**: Compromiso de base de datos
**Solución**:
- Variables de entorno
- Spring Cloud Config con cifrado
- HashiCorp Vault
- Rotar INMEDIATAMENTE todas las contraseñas expuestas

### VULNERABILIDADES ALTAS (5)

#### V-003: Sin Protección contra Fuerza Bruta
**Solución**: Implementar rate limiting (max 5 intentos / 15 min)

#### V-006: Falta Validación @Valid en Controllers
**Archivo**: `ABMUsersController.java:132`
```java
@PostMapping("/edit-user/{id}")
public String editUser(@PathVariable Long id,
    @RequestParam(required = false) String password,  // SIN VALIDACION
```
**Solución**: Crear DTOs con validaciones y usar `@Valid`

#### V-010: Logging DEBUG en Producción
**Impacto**: Exposición de tokens CSRF, datos sensibles

#### V-011: @PreAuthorize Comentado en TODOS los Controllers
**Archivo**: Todos los controllers CU
```java
// @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")  // COMENTADO!
@GetMapping("/ingreso-compra")
```
**Riesgo**: **CRÍTICO** - Cualquier usuario autenticado puede ejecutar cualquier CU
**Solución**: Descomentar INMEDIATAMENTE

#### V-014: CSRF Token con HttpOnly=false
**Archivo**: `SecurityConfig.java:32`
```java
.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
```
**Riesgo**: Vulnerable a XSS → robo de token CSRF

### VULNERABILIDADES MEDIAS (4)

- V-004: Sin 2FA para usuarios privilegiados
- V-007: Queries JPQL (bajo riesgo actual, vigilar)
- V-008: Sanitización Thymeleaf (bajo riesgo actual)
- V-013: Sin HTTPS forzado en código

### VULNERABILIDADES BAJAS (2)

- V-005: Sin invalidación de sesión en cambio de contraseña
- V-012: Enumeración de usuarios

### Dependencias Vulnerables

**PostgreSQL JDBC 42.6.0**:
- CVE-2024-1597 (SQL Injection)
- **Actualizar a**: 42.7.4

**Jackson 2.15.2**:
- Versiones mezcladas (2.15.2 y 2.18.2)
- **Unificar en**: 2.18.2

---

## PARTE 3: LOGGING Y AUDITORÍA

### Estado Actual

**✅ EXCELENTE**:
- Tabla `movimientos` registra TODAS las operaciones
- Campo `creadoPor` con usuario en todos los movimientos
- Tabla `auditoria_accesos` para rol AUDITOR
- Soft delete (registros nunca se borran físicamente)
- Timestamps con OffsetDateTime (UTC)

**❌ CRÍTICO - FALTANTE**:
- **Sin logging en archivos** de operaciones de negocio
- Solo 2 clases usan `log.info()` efectivamente
- No hay `logback-spring.xml` configurado

### Implementación Requerida

#### 1. Crear logback-spring.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Log de aplicación general -->
    <appender name="FILE_APP" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/conitrack-app.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/conitrack-app-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>90</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Log de AUDITORIA separado -->
    <appender name="FILE_AUDIT" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/conitrack-audit.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/conitrack-audit-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>365</maxHistory>  <!-- 1 año para auditoría -->
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} | %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="com.mb.conitrack.service.AuditorAccessLogger" level="INFO" additivity="false">
        <appender-ref ref="FILE_AUDIT"/>
    </logger>

    <logger name="com.mb.conitrack.service.cu" level="INFO"/>

    <root level="INFO">
        <appender-ref ref="FILE_APP"/>
    </root>
</configuration>
```

#### 2. Agregar Logging en TODOS los Servicios CU (16 servicios)
```java
@Service
@Slf4j
public class AltaIngresoCompraService {
    @Transactional
    public LoteDTO altaStockPorCompra(LoteDTO loteDTO) {
        User currentUser = securityContextService.getCurrentUser();

        // AGREGAR ESTO:
        log.info("ALTA_COMPRA | user={} | lote={} | producto={} | cantidad={} | proveedor={}",
            currentUser.getUsername(),
            loteDTO.getCodigoLote(),
            loteDTO.getProductoId(),
            loteDTO.getCantidadInicial(),
            loteDTO.getProveedorId()
        );

        // ... resto del código
    }
}
```

### Cumplimiento Regulatorio

**✅ Cumple**:
- Registro de movimientos en BD
- Trazabilidad de usuario
- Auditoría de AUDITOR
- Inmutabilidad del historial

**⚠️ Debe Implementar**:
- Logging estructurado en archivos
- Logs rotativos con retención de 1 año
- Documentar política de retención

---

## PARTE 4: CALIDAD DE CÓDIGO (TOP 20 PROBLEMAS)

### CRÍTICOS (Severidad Alta)

#### 1. Método Gigante: ModifReversoMovimientoService (654 líneas)
**Problema**: God Class con método de 120+ líneas + 12 métodos privados
**Solución**: Dividir en ReversoAltaService, ReversoBajaService, ReversoModificacionService

#### 2. Clase Base Excesiva: AbstractCuService (633 líneas)
**Problema**: 29 métodos de validación, 7 repositorios inyectados a todos
**Solución**: Dividir en ValidadorBultosService, ValidadorFechasService, etc.

#### 3. Autorizaciones Comentadas en Controllers
**Problema**: TODOS los @PreAuthorize comentados (30+ endpoints)
**Riesgo**: Cualquier usuario puede ejecutar cualquier CU
**Acción**: Descomentar INMEDIATAMENTE

#### 4. Recursión Sin Protección: Lote.getRootLote()
**Problema**: Sin límite explícito en código (solo MAX_GENEALOGY_DEPTH=100 documentado)
**Riesgo**: StackOverflowError con referencias circulares
**Solución**: Implementar contador con límite en código

#### 5. Lista Hardcodeada de Usuarios en Controller
**Problema**: Campo `usernames` con usuarios hardcodeados, nunca usado
**Acción**: Eliminar código muerto

### GRAVES (Severidad Media-Alta)

6. Queries SQL excesivamente complejas (18 queries de 30-50 líneas)
7. Catch de Exception genérico (8 instancias)
8. Duplicación: Conversión de unidades repetida 4+ veces
9. Validación inconsistente de errores en bindings
10. Lógica duplicada: Estado de bultos/lotes (10+ lugares)

### IMPORTANTES (Severidad Media)

11. Código comentado en producción
12. TODO en código de producción (línea 88 de ModifReversoMovimientoService)
13. Métodos muy largos (150+ líneas en BajaMuestreoBultoService)
14. Falta validación de nulos (métodos retornan null sin @Nullable)
15. Comparación inconsistente de BigDecimal
16. Lógica compleja en DTOUtils (319 líneas, 15+ métodos estáticos)
17. Lazy loading no documentado en @ToString
18. Falta validación de permisos a nivel de servicio
19. Magic numbers y strings sin constantes
20. Manejo inconsistente de transacciones (@Transactional en validaciones)

---

## PARTE 5: TESTING (30% COBERTURA EN SERVICIOS CU)

### Estado Actual

**Cobertura Global**: 54% instrucciones, 47% branches
**Servicios CU**: 30% cobertura - **CRÍTICO**

### Servicios con Tests Completos (100%)

1. AltaIngresoCompraService
2. BajaDevolucionCompraService
3. BajaConsumoProduccionService
4. ModifResultadoAnalisisService
5. ModifDictamenCuarentenaService

### Servicios SIN Tests (0% cobertura) - PRIORIDAD

**CRÍTICOS** (Semana 1):
1. **ModifReversoMovimientoService** - 0% (1,784 instrucciones) - **MUY CRÍTICO**
2. **BajaVentaProductoService** - 0% (356 instrucciones) - Operación más común
3. **AltaDevolucionVentaService** - 0% (601 instrucciones) - Devolución cliente

**ALTOS** (Semana 2):
4. **AltaIngresoProduccionService** - 1% (168 instrucciones)
5. **ModifRetiroMercadoService** - 0% (770 instrucciones) - Recall crítico
6. **ModifLiberacionVentasService** - 2% (139 instrucciones)

**MEDIOS** (Semana 3):
7. ModifAnulacionAnalisisService - 1%
8. ModifReanalisisLoteService - 2%
9. ModifTrazadoLoteService - 2%
10. BajaAjusteStockService - 0%

### Flujos E2E Sin Tests

**Flujo 1**: Ingreso → Análisis → Venta
**Flujo 2**: Producción → Trazado → Venta
**Flujo 3**: Devolución Cliente
**Flujo 4**: Recall de Producto

---

## PARTE 6: CHECKLIST PRE-PRODUCCIÓN

### Seguridad (CRÍTICO)

- [ ] Cambiar longitud mínima de contraseña a 12 caracteres
- [ ] Eliminar usuarios por defecto con password = username
- [ ] Forzar cambio de contraseña en primer login
- [ ] Rotar credenciales de BD expuestas en archivos
- [ ] Descomentar TODOS los @PreAuthorize en controllers
- [ ] Implementar rate limiting en login (5 intentos / 15 min)
- [ ] Actualizar PostgreSQL JDBC a 42.7.4
- [ ] Unificar Jackson a 2.18.2

### Configuración (CRÍTICO)

- [ ] Crear application-PROD.yml con:
  - Logging: WARN/ERROR (no DEBUG)
  - Session timeout: 1800s (30 min)
  - Error exposure: never
- [ ] Unificar variables de entorno (PGHOST vs PROD_DB_HOST)
- [ ] Agregar rotación de backups (find -mtime +30 -delete)
- [ ] Exponer endpoint /health (Actuator)

### Logging y Auditoría (OBLIGATORIO)

- [ ] Crear logback-spring.xml con logs rotativos
- [ ] Agregar logging en 16 servicios CU
- [ ] Logs de auditoría con retención de 1 año
- [ ] Logs de operaciones de seguridad (login, password change)
- [ ] Documentar política de retención

### Hosting (RECOMENDADO)

- [ ] Migrar de Heroku a Render
- [ ] Configurar backups diarios automáticos
- [ ] Verificar restore de backups
- [ ] Configurar monitoreo (UptimeRobot)
- [ ] Documentar procedimientos de deploy/restore/rollback

### Testing (ALTA PRIORIDAD)

- [ ] Tests para ModifReversoMovimientoService (1,784 instrucciones)
- [ ] Tests para BajaVentaProductoService (operación más común)
- [ ] Tests para AltaDevolucionVentaService
- [ ] Tests E2E de flujos completos
- [ ] Meta: 80% cobertura en servicios CU

### Código (PRIORITARIO)

- [ ] Proteger recursión en Lote.getRootLote() con contador
- [ ] Refactorizar ModifReversoMovimientoService (654 líneas)
- [ ] Dividir AbstractCuService (633 líneas)
- [ ] Eliminar código muerto (lista de usuarios hardcodeada)
- [ ] Resolver TODO en línea 88 de ModifReversoMovimientoService

---

## PARTE 7: ESTIMACIONES

### Tiempo de Correcciones

**Seguridad CRÍTICA**: 1 semana (40 horas)
**Configuración**: 1 día (8 horas)
**Logging**: 2 semanas (80 horas)
**Migración Render**: 1 día (6 horas)
**Testing Crítico**: 3 semanas (120 horas)
**Refactoring Código**: 4 semanas (160 horas)

**Total**: ~10 semanas (~400 horas)

### Costos

**Hosting actual** (Heroku): $12-22/mes
**Hosting recomendado** (Render): $14/mes con backups incluidos
**Ahorro anual**: $0-96/año + backups automáticos

---

## CONCLUSIÓN

El proyecto Conitrack tiene una **arquitectura sólida y bien pensada** con excelente sistema de auditoría en BD, pero presenta **gaps críticos de seguridad y testing** que deben corregirse antes de producción.

**Puntos Fuertes**:
- ✅ Arquitectura de casos de uso bien definida
- ✅ Sistema de auditoría robusto (Movimiento + AuditoriaAcceso)
- ✅ Trazabilidad completa (lotes, movimientos, usuarios)
- ✅ Soft delete (historial inmutable)
- ✅ Rol AUDITOR para entes reguladores
- ✅ 8 roles con jerarquía implementada

**Urgente**:
- ❌ 3 vulnerabilidades críticas de seguridad
- ❌ 30+ endpoints sin autorización (@PreAuthorize comentado)
- ❌ Sin logging de operaciones de negocio
- ❌ 10 servicios críticos sin tests (0% cobertura)

**Recomendación Final**:
1. **Semana 1**: Corregir seguridad crítica (V-001, V-002, V-003, V-009, V-011)
2. **Semana 2**: Implementar logging estructurado
3. **Semana 3**: Migrar a Render con backups automáticos
4. **Semanas 4-6**: Testing de servicios críticos
5. **Continuo**: Refactoring de código (God Classes)

Con estas correcciones, el sistema estará **listo para producción** y cumplirá con requisitos regulatorios (FDA 21 CFR Part 11, ISO 13485, ANMAT).

---

**Siguiente Documento**: Ver `PROMPTS_PENDIENTES.md` para lista de prompts simples a ejecutar en futuras sesiones.
