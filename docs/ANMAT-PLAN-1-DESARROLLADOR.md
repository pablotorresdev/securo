# Plan de Cumplimiento ANMAT - 1 Desarrollador Senior
## Sistema de Stock Conitrack - Versión Realista

**Contexto:**
- Sistema: Gestión de stock (reemplaza Excel)
- Recursos: **1 desarrollador senior** disponible
- Objetivo: Cumplimiento mínimo ANMAT para pasar auditoría
- Presupuesto: ~$13,000 USD (solo desarrollador)
- Duración: **3 meses** (considerando 1 solo recurso)

---

## Resumen Ejecutivo

### Inversión Total

| Concepto | Monto USD | Justificación |
|----------|-----------|---------------|
| **Desarrollador Senior** | $0 | Recurso interno de la empresa (6 semanas FTE asignadas) |
| **Herramientas/Licencias** | $500 | Documentación, testing |
| **Consultor externo (opcional)** | $2,000 | Review de documentación por experto BPF (2 días) |
| **TOTAL** | **$2,500** | |

**Nota:** El desarrollador es recurso interno, no representa gasto adicional. El costo real es solo herramientas + consultor opcional.

### Duración: 3 Meses (12 Semanas)

**Distribución del tiempo del desarrollador:**
- 50% tiempo en cumplimiento ANMAT (6 semanas full-time equivalente)
- 50% tiempo en operaciones normales / soporte

---

## Cambios Mínimos Requeridos

### 1. Audit Trail Básico
**Impacto:** Registro automático de cambios
**Complejidad:** Media
**Tiempo:** 1.5 semanas

### 2. Backup Automatizado
**Impacto:** Seguridad de datos
**Complejidad:** Baja
**Tiempo:** 0.5 semanas

### 3. Mejoras de Seguridad
**Impacto:** Contraseñas más robustas
**Complejidad:** Baja
**Tiempo:** 0.5 semanas

### 4. Documentación
**Impacto:** Cumplimiento regulatorio
**Complejidad:** Media
**Tiempo:** 3.5 semanas

---

## Cronograma Detallado (3 Meses)

### 📅 Mes 1: Implementación Técnica

#### Semana 1-2: Audit Trail Básico (1.5 semanas)

**Lunes-Martes (2 días):**
- [ ] Crear migración Flyway `V3__audit_trail_basico.sql`
```sql
CREATE TABLE auditoria_cambios_stock (
    id BIGSERIAL PRIMARY KEY,
    tabla VARCHAR(50) NOT NULL,      -- 'lote', 'movimiento'
    registro_id BIGINT NOT NULL,
    campo VARCHAR(50),                -- NULL si es CREATE/DELETE completo
    valor_anterior TEXT,
    valor_nuevo TEXT,
    accion VARCHAR(20) NOT NULL,      -- 'INSERT', 'UPDATE', 'DELETE'
    usuario_id BIGINT NOT NULL REFERENCES users(id),
    username VARCHAR(50) NOT NULL,    -- Snapshot
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),

    INDEX idx_audit_tabla_registro (tabla, registro_id),
    INDEX idx_audit_fecha (fecha_hora DESC),
    INDEX idx_audit_usuario (usuario_id)
);
```

**Miércoles-Jueves (2 días):**
- [ ] Opción A (Simple): **Triggers de base de datos** (recomendado para 1 dev)
```sql
-- Ejemplo trigger para tabla lote
CREATE OR REPLACE FUNCTION audit_lote_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        -- Auditar solo campos críticos
        IF OLD.cantidad_actual != NEW.cantidad_actual THEN
            INSERT INTO auditoria_cambios_stock (tabla, registro_id, campo, valor_anterior, valor_nuevo, accion, usuario_id, username)
            VALUES ('lote', NEW.id, 'cantidad_actual', OLD.cantidad_actual::TEXT, NEW.cantidad_actual::TEXT, 'UPDATE',
                    current_setting('app.current_user_id')::BIGINT, current_setting('app.current_username'));
        END IF;
        -- Repetir para otros campos críticos: dictamen, estado, etc.
    ELSIF TG_OP = 'INSERT' THEN
        INSERT INTO auditoria_cambios_stock (tabla, registro_id, accion, usuario_id, username, valor_nuevo)
        VALUES ('lote', NEW.id, 'INSERT',
                current_setting('app.current_user_id')::BIGINT, current_setting('app.current_username'),
                json_build_object('numero_lote', NEW.numero_lote, 'cantidad', NEW.cantidad_actual)::TEXT);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_lote_trigger
AFTER INSERT OR UPDATE ON lote
FOR EACH ROW EXECUTE FUNCTION audit_lote_changes();
```

- [ ] Opción B (Más compleja): Aspect AOP en Java
  - *Solo si el desarrollador domina Spring AOP*
  - *Requiere más tiempo (3-4 días adicionales)*

**Viernes (1 día):**
- [ ] Modificar servicios para setear usuario en sesión BD
```java
// En cada @Transactional service
@Transactional
public Lote modificarLote(Long id, LoteDTO dto) {
    // Setear usuario en contexto de BD para triggers
    String sql = "SELECT set_config('app.current_user_id', ?, false)";
    jdbcTemplate.execute(sql, currentUser.getId().toString());
    jdbcTemplate.execute("SELECT set_config('app.current_username', ?, false)", currentUser.getUsername());

    // Continuar lógica normal...
}
```

**Lunes siguiente (1 día):**
- [ ] Crear endpoint y vista para consultar audit trail
```java
@GetController
@RequestMapping("/auditoria")
public class AuditoriaController {

    @GetMapping("/historial/{tabla}/{id}")
    public String verHistorial(@PathVariable String tabla, @PathVariable Long id, Model model) {
        List<AuditoriaCambios> cambios = auditRepository.findByTablaAndRegistroId(tabla, id);
        model.addAttribute("cambios", cambios);
        return "auditoria/historial";
    }
}
```

**Martes siguiente (0.5 día):**
- [ ] Template HTML básico para ver historial
```html
<!-- templates/auditoria/historial.html -->
<table class="table">
    <tr th:each="cambio : ${cambios}">
        <td>[[${cambio.fechaHora}]]</td>
        <td>[[${cambio.username}]]</td>
        <td>[[${cambio.campo}]]</td>
        <td>[[${cambio.valorAnterior}]] → [[${cambio.valorNuevo}]]</td>
    </tr>
</table>
```

- [ ] Testing manual: modificar lote, verificar que se registra en audit trail

**ENTREGABLE SEMANA 1-2:** Audit trail funcional con triggers DB

---

#### Semana 3: Backup + Seguridad (1 semana)

**Lunes-Martes (2 días): Backup Automatizado**

- [ ] Crear script mejorado de backup
```bash
#!/bin/bash
# backups/auto_backup.sh

BACKUP_DIR="/opt/securo/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/conitrack_$DATE.sql"

# Backup
docker exec postgres-db pg_dump -U postgres conitrack > "$BACKUP_FILE"

# Comprimir
gzip "$BACKUP_FILE"

# Retener solo últimos 8 backups (2 meses si es semanal)
cd "$BACKUP_DIR"
ls -t conitrack_*.sql.gz | tail -n +9 | xargs rm -f

# Log
echo "$(date): Backup exitoso - $BACKUP_FILE.gz" >> "$BACKUP_DIR/backup.log"
```

- [ ] Configurar cron job
```bash
# En servidor (Linux) o Programador de Tareas (Windows)
# Ejecutar todos los domingos a las 2 AM
0 2 * * 0 /opt/securo/backups/auto_backup.sh
```

- [ ] Test de restore
```bash
# 1. Crear backup de prueba
./auto_backup.sh

# 2. Restaurar en BD temporal
gunzip -c conitrack_20250120.sql.gz | docker exec -i postgres-db psql -U postgres -d conitrack_test

# 3. Verificar integridad (contar registros)
docker exec postgres-db psql -U postgres -d conitrack_test -c "SELECT COUNT(*) FROM lote;"

# 4. Documentar resultado en: docs/PRUEBA-RESTORE.md
```

**Miércoles-Jueves (2 días): Mejoras de Seguridad**

- [ ] Modificar validación de contraseña en `User.java`
```java
@Entity
public class User {
    @NotNull
    @Size(min = 8, max = 50, message = "Contraseña debe tener entre 8 y 50 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
             message = "Contraseña debe contener mayúsculas, minúsculas y números")
    private String password;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private OffsetDateTime accountLockedUntil;

    @Column(name = "password_changed_at")
    private LocalDate passwordChangedAt;

    public boolean isAccountLocked() {
        if (accountLockedUntil == null) return false;
        return OffsetDateTime.now().isBefore(accountLockedUntil);
    }

    public boolean isPasswordExpired() {
        if (passwordChangedAt == null) return false;
        return LocalDate.now().isAfter(passwordChangedAt.plusDays(180)); // 6 meses
    }
}
```

- [ ] Migración BD
```sql
-- V4__security_improvements.sql
ALTER TABLE users ADD COLUMN failed_login_attempts INTEGER DEFAULT 0;
ALTER TABLE users ADD COLUMN account_locked_until TIMESTAMP WITH TIME ZONE;
ALTER TABLE users ADD COLUMN password_changed_at DATE DEFAULT CURRENT_DATE;
```

- [ ] Modificar `CustomUserDetailsService` para lockout
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Verificar lockout
        if (user.isAccountLocked()) {
            throw new LockedException("Cuenta bloqueada. Intente más tarde.");
        }

        // Verificar expiración de contraseña (warning, no bloqueo)
        if (user.isPasswordExpired()) {
            // Log warning o forzar cambio en login success
            log.warn("Contraseña de {} expirada", username);
        }

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            getAuthorities(user.getRole())
        );
    }

    @Transactional
    public void recordFailedLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountLockedUntil(OffsetDateTime.now().plusMinutes(30));
                log.warn("Cuenta {} bloqueada por 30 minutos", username);
            }

            userRepository.save(user);
        });
    }

    @Transactional
    public void resetFailedAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        });
    }
}
```

- [ ] Hook en login success/failure (SecurityConfig)
```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.formLogin(form -> form
            .successHandler((request, response, authentication) -> {
                userDetailsService.resetFailedAttempts(authentication.getName());
                response.sendRedirect("/home");
            })
            .failureHandler((request, response, exception) -> {
                String username = request.getParameter("username");
                userDetailsService.recordFailedLogin(username);
                response.sendRedirect("/login?error");
            })
        );
        // ...
    }
}
```

**Viernes (1 día):**
- [ ] Testing de seguridad:
  - Intentar crear contraseña débil → debe rechazar
  - 5 intentos fallidos → cuenta bloqueada 30 min
  - Esperar 30 min → cuenta desbloqueada automáticamente
- [ ] Fix de bugs si los hay

**ENTREGABLE SEMANA 3:** Backup automático + Seguridad mejorada

---

#### Semana 4: Testing y Refinamiento (1 semana)

**Lunes-Miércoles (3 días):**
- [ ] Testing exhaustivo de audit trail
  - Crear lote → verificar INSERT registrado
  - Modificar cantidad → verificar UPDATE registrado
  - Eliminar (soft delete) → verificar DELETE registrado
  - Probar en TODOS los módulos críticos

- [ ] Testing de backup
  - Ejecutar backup automático
  - Simular pérdida de datos
  - Restaurar backup
  - Verificar integridad

- [ ] Testing de seguridad
  - Probar políticas de contraseña
  - Probar lockout
  - Probar roles y permisos

**Jueves-Viernes (2 días):**
- [ ] Refinamiento y fixes
- [ ] Optimizaciones de performance si necesario
- [ ] Preparar ambiente de "producción"

**ENTREGABLE SEMANA 4:** Sistema técnicamente completo y testeado

---

### 📅 Mes 2-3: Documentación (6 semanas)

#### Semana 5-6: Especificaciones del Sistema (ERU Simplificado)

**Documento: CONITRACK-ERU-001.pdf (~10-15 páginas)**

**Contenido:**
```markdown
# ESPECIFICACIONES DE REQUERIMIENTOS DE USUARIO
## Sistema Conitrack - Gestión de Stock

1. INTRODUCCIÓN
   1.1 Propósito del sistema
   1.2 Alcance (qué hace y qué NO hace)
   1.3 Usuarios y roles

2. DESCRIPCIÓN FUNCIONAL
   2.1 Gestión de Lotes
       - Alta de ingreso de compra
       - Modificación de datos de lote
       - Consulta de lotes
   2.2 Gestión de Movimientos
       - Registros de movimientos de stock
       - Consulta de movimientos
   2.3 Control de Acceso
       - Roles: ADMIN, GERENTE, ANALISTA, AUDITOR
       - Permisos por rol
   2.4 Auditoría
       - Registro automático de cambios
       - Consulta de historial

3. REQUERIMIENTOS NO FUNCIONALES
   3.1 Seguridad
   3.2 Backup y recuperación
   3.3 Performance

4. CRITERIOS DE ACEPTACIÓN
   - Por funcionalidad
```

**Tiempo:** 10 días (2 semanas)
**Formato:** Word/PDF con screenshots del sistema

---

#### Semana 7-8: Protocolo de Calificación (IQ/OQ Combinado)

**Documento: CONITRACK-IQ-OQ-001.pdf (~20-25 páginas)**

**Contenido:**
```markdown
# PROTOCOLO DE CALIFICACIÓN IQ/OQ
## Sistema Conitrack

1. INFORMACIÓN GENERAL
   1.1 Objetivo
   1.2 Alcance
   1.3 Responsables
   1.4 Aprobaciones (firmas)

2. INSTALACIÓN (IQ)
   2.1 Verificar versiones de software
       - Java 17
       - Spring Boot 3.4.1
       - PostgreSQL 17
   2.2 Verificar configuración
       - application.yml
       - Base de datos
       - Usuarios creados
   2.3 Verificar conectividad
       - BD accesible
       - Sistema responde

3. OPERACIÓN (OQ) - 15-20 Test Cases

   TC-001: Alta de Lote
   Objetivo: Verificar creación de lote
   Prerequisitos: Usuario ANALISTA logueado
   Procedimiento:
   1. Acceder a /lote/nuevo
   2. Completar: número lote, producto, cantidad
   3. Guardar
   Resultado Esperado: Lote creado, visible en lista
   Criterio Aceptación: ✅ PASS / ❌ FAIL
   Ejecutado por: _____________ Fecha: _____

   TC-002: Modificación de Lote
   ...

   TC-003: Audit Trail - Verificar Registro
   Objetivo: Verificar que cambios se registran
   Procedimiento:
   1. Modificar cantidad de lote
   2. Acceder a /auditoria/historial/lote/{id}
   3. Verificar registro con old/new value
   Resultado Esperado: Cambio visible en audit trail
   Criterio Aceptación: ✅ PASS

   TC-004: Backup y Restore
   ...

   TC-015: Control de Acceso - AUDITOR no puede modificar
   Objetivo: Verificar que AUDITOR solo lee
   Procedimiento:
   1. Loguear como AUDITOR
   2. Intentar modificar lote
   Resultado Esperado: Acceso denegado
   Criterio Aceptación: ✅ PASS

4. RESUMEN DE RESULTADOS
   Total tests: 15
   Pasados: __
   Fallidos: __
   Desviaciones: __

5. CONCLUSIÓN
   El sistema Conitrack [ ] APRUEBA [ ] NO APRUEBA la calificación.

6. FIRMAS
   Ejecutado por: _____________ Fecha: _____
   Revisado por QA: ___________ Fecha: _____
   Aprobado por: ______________ Fecha: _____
```

**Tiempo:** 10 días
  - 5 días escribir protocolo
  - 3 días ejecutar tests
  - 2 días documentar resultados

**Formato:** PDF con screenshots de cada test

---

#### Semana 9: Manual de Usuario

**Documento: CONITRACK-MANUAL-USUARIO.pdf (~10-15 páginas)**

**Contenido:**
```markdown
# MANUAL DE USUARIO
## Sistema Conitrack

1. ACCESO AL SISTEMA
   1.1 Login
       Screenshot del login
   1.2 Recuperación de contraseña

2. PANTALLA PRINCIPAL
   Screenshot con áreas identificadas

3. GESTIÓN DE LOTES
   3.1 Crear nuevo lote
       - Screenshot paso a paso
       - Campos obligatorios
       - Validaciones
   3.2 Consultar lotes
   3.3 Modificar lote
   3.4 Ver historial de cambios

4. GESTIÓN DE MOVIMIENTOS
   ...

5. REPORTES
   ...

6. ADMINISTRACIÓN (solo ADMIN)
   6.1 Gestión de usuarios
   6.2 Gestión de productos

7. PREGUNTAS FRECUENTES (FAQ)

8. CONTACTO DE SOPORTE
```

**Tiempo:** 5 días
**Formato:** PDF con screenshots

---

#### Semana 10: SOP de Backup y Procedimientos

**Documento: SOP-BACKUP-001.pdf (~5 páginas)**

**Contenido:**
```markdown
# PROCEDIMIENTO OPERATIVO ESTÁNDAR
## Backup y Recuperación de Conitrack

1. OBJETIVO
   Establecer procedimiento de backup y restore

2. ALCANCE
   Sistema Conitrack - Base de datos PostgreSQL

3. RESPONSABILIDADES
   - IT Manager: Configurar y monitorear backups
   - System Owner: Aprobar procedimiento

4. PROCEDIMIENTO DE BACKUP
   4.1 Frecuencia: Semanal (domingos 2 AM)
   4.2 Método: Automático vía cron job
   4.3 Ubicación: /opt/securo/backups
   4.4 Retención: 8 backups (2 meses)
   4.5 Verificación: Revisar log mensualmente

5. PROCEDIMIENTO DE RESTORE
   5.1 Identificar backup a restaurar
   5.2 Detener aplicación
   5.3 Ejecutar restore:
       gunzip -c backup.sql.gz | psql ...
   5.4 Verificar integridad
   5.5 Reiniciar aplicación

6. PRUEBAS DE RESTORE
   Realizar test de restore trimestral

7. ANEXOS
   - Script auto_backup.sh
   - Comandos de restore
```

**Tiempo:** 2 días

**Otros SOPs opcionales (si hay tiempo):**
- SOP-CAMBIOS-001: Gestión de Cambios (3 páginas)
- SOP-USUARIOS-001: Alta/Baja de Usuarios (2 páginas)

**TOTAL SEMANA 10:** SOPs completos

---

#### Semana 11-12: Revisión y Preparación para Auditoría

**Semana 11: Revisión Interna**
- [ ] Revisar TODA la documentación
- [ ] Verificar consistencia entre docs
- [ ] Correcciones y ajustes
- [ ] Imprimir y encuadernar documentos
- [ ] Preparar carpeta de auditoría

**Semana 12: Mock Audit (Opcional pero recomendado)**
- [ ] Contratar consultor externo BPF (2 días, $2K)
- [ ] Simular auditoría ANMAT
- [ ] Identificar findings
- [ ] Correcciones rápidas si hay issues menores

**ENTREGABLE MES 2-3:** Documentación completa + Sistema auditoria-ready

---

## Distribución del Tiempo del Desarrollador

### Semanas 1-4 (Mes 1): 80% Conitrack ANMAT, 20% Otros

**Justificación:** Implementación técnica requiere enfoque

**Por semana:**
- 4 días en ANMAT (32 horas)
- 1 día en soporte/otros (8 horas)

### Semanas 5-12 (Mes 2-3): 40% Conitrack ANMAT, 60% Otros

**Justificación:** Documentación puede hacerse en paralelo con otras tareas

**Por semana:**
- 2 días en ANMAT (16 horas)
- 3 días en soporte/otros (24 horas)

---

## Checklist de Entregables

### Técnicos
- [ ] Audit trail funcional con triggers BD
- [ ] Vista de consulta de historial
- [ ] Backup automático configurado (cron)
- [ ] Prueba de restore exitosa
- [ ] Contraseñas 8+ caracteres, lockout 5 intentos
- [ ] Expiración de contraseña (180 días)
- [ ] Tests manuales ejecutados y documentados

### Documentación
- [ ] CONITRACK-ERU-001.pdf (Especificaciones)
- [ ] CONITRACK-IQ-OQ-001.pdf (Calificación)
- [ ] CONITRACK-MANUAL-USUARIO.pdf (Manual)
- [ ] SOP-BACKUP-001.pdf (Procedimiento Backup)
- [ ] PRUEBA-RESTORE.md (Evidencia de restore)
- [ ] CHANGELOG.md (Historial de versiones)

### Evidencia
- [ ] Screenshots de audit trail funcionando
- [ ] Screenshots de tests de calificación
- [ ] Logs de backup automático
- [ ] Evidencia de restore exitoso

---

## Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Desarrollador se enferma | Media | Alto | Buffer de 1 semana extra |
| Triggers BD no funcionan | Baja | Medio | Fallback a logging manual temporal |
| Documentación toma más tiempo | Media | Medio | Priorizar ERU e IQ/OQ, SOPs pueden ser más breves |
| Auditor pide cambios | Media | Medio | Mock audit pre-ANMAT para detectar issues |

---

## Presupuesto Detallado

| Concepto | Cantidad | Costo Unitario | Total USD |
|----------|----------|----------------|-----------|
| **Desarrollador Senior** | 6 semanas FTE | $0 (interno) | $0 |
| **Herramientas** | - | - | $500 |
| - Licencia PDF editor | 1 | $100 | $100 |
| - Herramienta diagramas | 1 | $50 | $50 |
| - Screenshots/captura | - | $50 | $50 |
| - Contingencia | - | $300 | $300 |
| **Consultor BPF** | 2 días | $1,000/día | $2,000 |
| **TOTAL** | | | **$2,500** |

**Nota:**
- Desarrollador es recurso interno, no implica costo adicional (solo asignación de tiempo)
- Si presupuesto es limitado, consultor BPF es opcional (ahorrar $2K, inversión mínima $500)

---

## Criterios de Éxito

### Técnicos
✅ Audit trail registra 100% de cambios críticos a stock
✅ Backup automático ejecutándose semanalmente sin fallos
✅ Restore probado exitosamente al menos una vez
✅ Contraseñas cumplen mínimo 8 caracteres
✅ Lockout funciona después de 5 intentos fallidos

### Documentación
✅ ERU describe sistema completo (10-15 pgs)
✅ IQ/OQ ejecutado con 15+ test cases (20-25 pgs)
✅ Manual de usuario con screenshots (10-15 pgs)
✅ SOP de backup documentado (5 pgs)

### Auditoría
✅ Sistema pasa mock audit interna (si se realiza)
✅ 0 findings críticos
✅ Findings menores (si los hay) cerrados antes de auditoría oficial

---

## FAQ del Desarrollador

**P: ¿Puedo usar Aspect AOP en vez de triggers?**
R: Sí, si te sientes cómodo con AOP. Pero triggers son más simples y robustos.

**P: ¿Qué pasa si no termino en 3 meses?**
R: Prioriza en este orden: 1) Audit trail, 2) Backup, 3) Documentación ERU e IQ/OQ. Los SOPs pueden ser más breves.

**P: ¿Necesito contratar el consultor BPF?**
R: Recomendado pero opcional. Ayuda a validar documentación antes de auditoría real.

**P: ¿Cuánto tiempo por día debo dedicar?**
R: Mes 1: ~6-7 hrs/día. Mes 2-3: ~3-4 hrs/día. Total: ~320 horas en 3 meses.

**P: ¿Qué pasa si ANMAT pide más documentación?**
R: Con lo mínimo debería pasar. Si piden más, es trabajo adicional no contemplado aquí.

---

## Próximos Pasos Inmediatos

### Semana Actual
1. [ ] Leer este plan completo
2. [ ] Aprobar presupuesto y timeline con management
3. [ ] Bloquear calendario: 50% tiempo próximos 3 meses
4. [ ] Configurar ambiente de desarrollo

### Próxima Semana (Iniciar Mes 1)
1. [ ] Día 1: Crear migración audit trail
2. [ ] Día 2-3: Implementar triggers BD
3. [ ] Día 4: Modificar servicios para setear usuario
4. [ ] Día 5: Endpoint y vista de consulta

**¡A trabajar! 🚀**

---

**Versión:** 1.0 - Plan 1 Desarrollador
**Fecha:** 2025-11-20
**Aprobado por:** ________________
**Fecha inicio:** ________________
