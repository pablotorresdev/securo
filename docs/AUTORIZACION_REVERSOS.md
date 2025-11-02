# Sistema de Autorización para Reversos de Movimientos

## Resumen

Este documento describe el sistema de autorización jerárquica implementado para controlar quién puede reversar movimientos en el sistema Conitrack.

## Jerarquía de Roles

El sistema implementa una jerarquía de 8 roles con niveles numéricos:

| Rol | Nivel | Descripción | Puede Modificar | Solo Lectura |
|-----|-------|-------------|-----------------|--------------|
| **ADMIN** | 6 | Administrador del sistema | ✅ Sí | ❌ No |
| **DT** | 5 | Director Técnico | ✅ Sí | ❌ No |
| **GERENTE_GARANTIA_CALIDAD** | 4 | Gerente de Garantía de Calidad | ✅ Sí | ❌ No |
| **GERENTE_CONTROL_CALIDAD** | 3 | Gerente de Control de Calidad | ✅ Sí | ❌ No |
| **SUPERVISOR_PLANTA** | 3 | Supervisor de Planta | ✅ Sí | ❌ No |
| **ANALISTA_CONTROL_CALIDAD** | 2 | Analista de Control de Calidad | ✅ Sí | ❌ No |
| **ANALISTA_PLANTA** | 2 | Analista de Planta | ✅ Sí | ❌ No |
| **AUDITOR** | 1 | Auditor Externo (FDA, ANMAT, etc.) | ❌ No | ✅ Sí |

**Nota**: Niveles más altos tienen más privilegios que niveles inferiores.

---

## Reglas de Autorización para Reversos

### Regla 1: El Creador Siempre Puede Reversar

**Descripción**: El usuario que CREÓ un movimiento siempre puede reversarlo, sin importar su nivel jerárquico.

**Ejemplo**:
- `analista_planta` (nivel 2) crea un movimiento
- `analista_planta` puede reversar su propio movimiento ✅

**Implementación**:
```java
if (movimiento.getCreadoPor().getId().equals(userActual.getId())) {
    return true;
}
```

---

### Regla 2: Jerarquía Superior Puede Reversar

**Descripción**: Un usuario con nivel jerárquico SUPERIOR puede reversar movimientos creados por usuarios de nivel INFERIOR.

**Ejemplos**:
- `admin` (nivel 6) puede reversar movimientos de `dt` (nivel 5) ✅
- `dt` (nivel 5) puede reversar movimientos de `gerente_garantia` (nivel 4) ✅
- `gerente_control` (nivel 3) puede reversar movimientos de `analista_planta` (nivel 2) ✅

**Implementación**:
```java
Integer nivelCreador = movimiento.getCreadoPor().getRole().getNivel();
Integer nivelActual = userActual.getRole().getNivel();
return nivelActual > nivelCreador;
```

---

### Regla 3: Mismo Nivel NO Puede Reversar

**Descripción**: Usuarios del mismo nivel jerárquico NO pueden reversar movimientos entre sí (excepto si son el creador - ver Regla 1).

**Ejemplos**:
- `analista_control` (nivel 2) NO puede reversar movimientos de `analista_planta` (nivel 2) ❌
- `gerente_control` (nivel 3) NO puede reversar movimientos de `supervisor_planta` (nivel 3) ❌
- `analista_planta` (nivel 2) NO puede reversar movimientos de `gerente_garantia` (nivel 4) ❌

**Implementación**:
```java
// Después de verificar que no es el creador
Integer nivelCreador = movimiento.getCreadoPor().getRole().getNivel();
Integer nivelActual = userActual.getRole().getNivel();

if (nivelActual <= nivelCreador) {
    return false; // Mismo nivel o inferior
}
```

---

### Regla 4: AUDITOR NUNCA Puede Reversar

**Descripción**: El rol AUDITOR es de solo lectura y NUNCA puede reversar movimientos, ni siquiera los que hubiera creado (aunque no debería crear ninguno).

**Razón**: Los auditores son inspectores externos (FDA, ANMAT, etc.) que solo tienen permiso de lectura.

**Ejemplos**:
- `auditor_fda` NO puede reversar ningún movimiento ❌
- `auditor_anmat` NO puede reversar ningún movimiento ❌

**Implementación**:
```java
if (userActual.isAuditor()) {
    return false; // AUDITOR nunca puede reversar
}
```

**Características adicionales del rol AUDITOR**:
- ✅ Puede acceder a `/api/reportes/**` para ver todos los datos
- ✅ Puede ver registros con baja lógica (`activo=false`)
- ❌ No puede acceder a `/api/lotes/**`, `/api/movimientos/**`, `/api/bultos/**`
- ✅ Sus accesos son registrados automáticamente (archivo + base de datos)
- ✅ Puede tener fecha de expiración (`fechaExpiracion`)

---

### Regla 5: Movimientos Legacy (Sin Creador)

**Descripción**: Movimientos creados antes de implementar el sistema de usuarios (`creado_por_user_id = NULL`) solo pueden ser revers ados por ADMIN.

**Razón**: No hay información de quién creó el movimiento, por lo que solo el administrador puede asegurar la autorización.

**Ejemplos**:
- Movimiento antiguo sin `creado_por` → Solo `admin` puede reversar ✅
- `dt`, `gerentes`, `analistas` NO pueden reversar movimientos legacy ❌

**Implementación**:
```java
if (movimiento.getCreadoPor() == null) {
    return esAdmin(userActual); // Solo ADMIN puede reversar legacy
}
```

---

## Matriz de Autorización

Esta matriz muestra quién puede reversar movimientos creados por cada rol:

| Creador / Reversor | ADMIN | DT | Gerente (4) | Gerente (3) | Supervisor (3) | Analista (2) | AUDITOR |
|--------------------|-------|----|--------------|--------------|-----------------|--------------|----|
| **ADMIN (6)** | ✅* | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **DT (5)** | ✅ | ✅* | ❌ | ❌ | ❌ | ❌ | ❌ |
| **GERENTE_GARANTIA (4)** | ✅ | ✅ | ✅* | ❌ | ❌ | ❌ | ❌ |
| **GERENTE_CONTROL (3)** | ✅ | ✅ | ✅ | ✅* | ❌** | ❌ | ❌ |
| **SUPERVISOR_PLANTA (3)** | ✅ | ✅ | ✅ | ❌** | ✅* | ❌ | ❌ |
| **ANALISTA_CONTROL (2)** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅* | ❌ |
| **ANALISTA_PLANTA (2)** | ✅ | ✅ | ✅ | ✅ | ✅ | ❌** | ❌ |
| **AUDITOR (1)** | ❌*** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Legacy (NULL)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

**Leyenda**:
- ✅ = Puede reversar
- ✅* = Puede reversar (porque es el creador)
- ❌ = NO puede reversar
- ❌** = NO puede (mismo nivel)
- ❌*** = AUDITOR nunca puede reversar, ni sus propios movimientos

---

## Implementación Técnica

### Clase Principal: `ReversoAuthorizationService`

**Ubicación**: `com.mb.conitrack.service.ReversoAuthorizationService`

**Métodos principales**:

```java
/**
 * Verifica si un usuario puede reversar un movimiento.
 * @return true si tiene permiso, false caso contrario
 */
public boolean puedeReversar(Movimiento movimiento, User userActual)

/**
 * Valida permiso y lanza excepción si no está autorizado.
 * @throws ReversoNotAuthorizedException si no tiene permiso
 */
public void validarPermisoReverso(Movimiento movimiento, User userActual)
```

### Uso en Servicios

**Ejemplo en `ModifReversoMovimientoService`**:

```java
@Transactional
public LoteDTO persistirReversoMovmiento(final MovimientoDTO dto) {
    User currentUser = securityContextService.getCurrentUser();

    final Movimiento movOrigen = /* obtener movimiento original */;

    // VALIDACIÓN DE AUTORIZACIÓN
    reversoAuthorizationService.validarPermisoReverso(movOrigen, currentUser);

    // Si llega aquí, está autorizado - proceder con el reverso
    // ...
}
```

### Tracking de Usuario Creador

Todos los movimientos ahora registran quién los creó:

```java
// En Utils (MovimientoAltaUtils, MovimientoBajaUtils, etc.)
movimiento.setCreadoPor(creadoPor); // User que creó el movimiento

// En Services
User currentUser = securityContextService.getCurrentUser();
Movimiento mov = createMovimientoXXX(dto, lote, currentUser);
```

---

## Excepciones y Mensajes de Error

### `ReversoNotAuthorizedException`

Excepción lanzada cuando un usuario intenta reversar un movimiento sin autorización.

**Mensajes de error**:

| Caso | Mensaje |
|------|---------|
| AUDITOR intenta reversar | "El rol AUDITOR no tiene permisos para reversar movimientos (rol de solo lectura)" |
| Movimiento legacy sin ADMIN | "Solo el ADMIN puede reversar movimientos legacy creados antes del sistema de usuarios" |
| Sin jerarquía suficiente | "No tienes permisos para reversar este movimiento. Fue creado por [usuario] (nivel [N]). Tu nivel es [M]. Solo el creador o usuarios de nivel superior pueden reversar." |

---

## Auditoría de Reversos

Todos los reversos quedan registrados con:
- ✅ Usuario que ejecutó el reverso (`creado_por_user_id`)
- ✅ Fecha y hora del reverso
- ✅ Movimiento original revertido (relación `movimiento_origen_id`)
- ✅ Motivo: `REVERSO`
- ✅ Observaciones con código CU

**Nota**: La validación de autorización se realiza ANTES de crear el movimiento de reverso, garantizando que solo usuarios autorizados puedan crear estos registros.

---

## Configuración de Usuarios

### Crear Usuario con Rol Específico

```java
Role analista = roleRepository.findByName("ANALISTA_PLANTA").orElseThrow();
User user = new User("juan.perez", "password", analista);
userRepository.save(user);
```

### Crear AUDITOR con Expiración

```java
Role auditor = roleRepository.findByName("AUDITOR").orElseThrow();
User auditorFDA = new User("auditor_fda", "temp_password", auditor);
auditorFDA.setFechaExpiracion(LocalDate.now().plusMonths(3)); // Expira en 3 meses
userRepository.save(auditorFDA);
```

### Verificar si Usuario está Expirado

```java
if (user.isExpired()) {
    throw new SecurityException("Usuario expirado");
}
```

---

## Endpoints de Reportes para AUDITOR

Los siguientes endpoints están disponibles para el rol AUDITOR (y todos los demás roles):

| Endpoint | Descripción |
|----------|-------------|
| `GET /api/reportes/lotes` | Obtiene todos los lotes (incluye bajas lógicas) |
| `GET /api/reportes/lotes/{id}` | Obtiene un lote específico por ID |
| `GET /api/reportes/lotes/activos` | Obtiene solo lotes activos (`activo=true`) |
| `GET /api/reportes/movimientos` | Obtiene todos los movimientos (incluye bajas lógicas) |
| `GET /api/reportes/movimientos/lote/{loteId}` | Obtiene movimientos de un lote específico |
| `GET /api/reportes/usuario/permisos` | Obtiene resumen de permisos del usuario actual |

**Seguridad**:
- Estos endpoints están protegidos con `@PreAuthorize`
- Requieren autenticación válida
- AUDITOR solo puede acceder a `/api/reportes/**`
- AUDITOR NO puede acceder a endpoints de ABM (`/api/lotes/**`, etc.)

---

## Testing

### Tests de Autorización

**Ubicación**: `src/test/java/com/mb/conitrack/service/ReversoAuthorizationServiceTest.java`

**Cobertura**: 26 tests que verifican:
- ✅ Regla 1: Creador puede reversar
- ✅ Regla 2: Jerarquía superior puede reversar
- ✅ Regla 3: Mismo nivel no puede
- ✅ Regla 4: AUDITOR nunca puede
- ✅ Regla 5: Legacy solo ADMIN

### Ejemplo de Test

```java
@Test
@DisplayName("Regla 2: ADMIN puede reversar movimientos de cualquier nivel inferior")
void testAdminPuedeReversarCualquierNivel() {
    Movimiento mov = crearMovimiento(analistaControlUser);

    boolean resultado = service.puedeReversar(mov, adminUser);

    assertTrue(resultado, "ADMIN debe poder reversar movimientos de niveles inferiores");
}
```

---

## Preguntas Frecuentes (FAQ)

### ¿Qué pasa si dos usuarios del mismo nivel trabajan en equipo?

Solo el creador del movimiento puede reversarlo. Si necesitan trabajar en equipo, deben involucrar a un supervisor o gerente de nivel superior para reversar movimientos.

### ¿Puede un ADMIN reversar movimientos de otro ADMIN?

Solo si es el creador del movimiento (Regla 1). De lo contrario, NO puede (Regla 3 - mismo nivel).

### ¿Qué pasa con los movimientos automáticos del sistema (CU9/CU10)?

Los procesos automáticos (expiraciones de análisis, vencimientos) usan un usuario especial `system_auto` con rol ADMIN. Estos movimientos solo pueden ser revert ados por ADMIN.

### ¿Se puede cambiar el nivel de un rol?

Sí, pero requiere modificar el enum `RoleEnum` y ejecutar una migración de base de datos. Los niveles están definidos en el código para mantener consistencia.

### ¿Cómo se eliminan usuarios AUDITOR expirados?

Actualmente, los usuarios expirados siguen en la base de datos pero el sistema verifica `isExpired()` en el login. Se puede implementar un job para desactivarlos automáticamente o eliminarlos después de cierto tiempo.

---

## Diagrama de Flujo

```
┌─────────────────────────────────────────┐
│  Usuario intenta reversar movimiento   │
└───────────────┬─────────────────────────┘
                │
                ▼
        ┌───────────────┐
        │ ¿Es AUDITOR?  │
        └───────┬───────┘
                │
        ┌───────┴────────┐
        │                │
       Sí               No
        │                │
        │                ▼
        │       ┌─────────────────┐
        │       │ ¿Sin creador?   │
        │       │   (Legacy)      │
        │       └────────┬─────────┘
        │                │
        │        ┌───────┴────────┐
        │        │                │
        │       Sí               No
        │        │                │
        │        ▼                ▼
        │   ┌─────────┐    ┌──────────────┐
        │   │¿Es ADMIN?│    │¿Es creador?  │
        │   └────┬────┘    └──────┬───────┘
        │        │                 │
        │  ┌─────┴──────┐   ┌─────┴──────┐
        │  │            │   │            │
        │ Sí           No  Sí           No
        │  │            │   │            │
        │  ▼            │   ▼            ▼
        │ ✅           │  ✅      ┌──────────────┐
        │              │          │¿Nivel mayor? │
        │              │          └──────┬───────┘
        │              │                 │
        │              │           ┌─────┴──────┐
        │              │           │            │
        ▼              ▼          Sí           No
       ❌             ❌          ▼            ▼
   NEGADO         NEGADO        ✅           ❌
                              PERMITIDO   NEGADO
```

---

## Changelog

### Versión 1.0 (Noviembre 2025)
- ✅ Implementación inicial del sistema de jerarquía y autorización
- ✅ 8 roles con niveles numéricos
- ✅ 5 reglas de autorización para reversos
- ✅ Rol AUDITOR con acceso de solo lectura
- ✅ Tracking de usuario creador en todos los movimientos
- ✅ 26 tests de autorización
- ✅ Endpoint `/api/reportes/**` para auditores
- ✅ Logging automático de accesos de auditores

---

## Contacto y Soporte

Para preguntas sobre el sistema de autorización, contactar al equipo de desarrollo o consultar el código fuente en:

- `ReversoAuthorizationService.java`
- `SecurityContextService.java`
- `ReversoNotAuthorizedException.java`
- Tests: `ReversoAuthorizationServiceTest.java`

---

**Última actualización**: Noviembre 2025
**Versión del documento**: 1.0
**Autor**: Sistema Conitrack
