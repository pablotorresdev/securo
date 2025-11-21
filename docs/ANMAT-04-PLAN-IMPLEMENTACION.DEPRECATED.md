# 04 - Plan de Implementación
## Roadmap Detallado de Cumplimiento ANMAT

[← Gap Analysis](./ANMAT-03-GAP-ANALYSIS.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md) | [Siguiente: Especificaciones Técnicas →](./ANMAT-05-ESPECIFICACIONES-TECNICAS.md)

---

## Visión General del Plan

### Objetivo
Transformar Conitrack de su estado actual (58% cumplimiento) a un sistema **100% conforme con ANMAT Anexo 6**, validado y listo para inspección regulatoria.

### Duración Total
**12 meses** divididos en 4 fases

### Inversión Total
**USD $205,000 - $270,000**

### Criterio de Éxito
Sistema aprobado en mock audit interna con **cero findings críticos** y documentación completa lista para inspección ANMAT.

---

## Fase 1: Correcciones Críticas (Meses 1-3)

### Objetivo
Implementar funcionalidades críticas faltantes que bloquean uso GxP del sistema.

### Duración
**3 meses (12 semanas)**

### Entregables Clave
1. ✅ Sistema de Audit Trail completo implementado
2. ✅ Sistema de Firma Electrónica funcional
3. ✅ Políticas de contraseña robustas
4. ✅ Especificaciones de Requerimientos de Usuario (ERU) documentadas

### Presupuesto Fase 1
**USD $50,000 - $70,000**
- Recursos humanos: 960 horas
- Herramientas/licencias: $2,000

---

### Semana 1-2: Kick-off y Preparación

#### Actividades

**Semana 1:**
- [ ] **Kick-off meeting** con todos los stakeholders
  - System Owner
  - Process Owner
  - QA Manager
  - IT Manager
  - Equipo de desarrollo (2-3 devs)

- [ ] **Decisión crítica: Firma Electrónica**
  - Opción A: Desarrollo custom (recomendado)
  - Opción B: Integración con proveedor (DocuSign, Adobe Sign)
  - Aprobar opción seleccionada

- [ ] **Contratación/Asignación**
  - Validation Specialist (externo o interno) - CRÍTICO
  - QA lead asignado 50% tiempo
  - Devs senior asignados 100% tiempo

- [ ] **Setup de ambiente**
  - Crear branch `feature/anmat-compliance`
  - Configurar ambiente de desarrollo dedicado
  - Setup de herramientas (Jira para tracking, etc.)

**Semana 2:**
- [ ] **Reuniones de análisis**
  - Workshop ERU: Identificar requerimientos críticos GxP
  - Workshop Risk Assessment: Clasificar funcionalidades por criticidad
  - Workshop Data Classification: Identificar datos GxP relevantes

- [ ] **Diseño de soluciones**
  - Diseño técnico detallado de Audit Trail
  - Diseño técnico detallado de Firma Electrónica
  - Diseño de políticas de seguridad

- [ ] **Documentación inicial**
  - Comenzar ERU (Especificaciones de Requerimientos de Usuario)
  - Comenzar Risk Assessment formal
  - Definir criterios de aceptación por funcionalidad

**Entregables Semana 1-2:**
- ✅ Equipo completo asignado
- ✅ Decisión de firma electrónica tomada
- ✅ Diseños técnicos aprobados
- ✅ Borrador de ERU (30% completo)

**Recursos:**
- System Owner: 20% tiempo
- Process Owner: 20% tiempo
- Validation Specialist: 100% tiempo
- Dev Senior: 100% tiempo

---

### Semana 3-6: Implementación Audit Trail

#### Objetivo
Implementar sistema completo de auditoría de cambios con captura de old/new values y motivo.

#### Actividades

**Semana 3: Fundamentos**
- [ ] **Base de datos**
  - Crear migración Flyway `V3__create_auditoria_cambios.sql`
  - Crear tabla `auditoria_cambios` con índices optimizados
  - Crear tabla `gxp_data_classification` para clasificación de datos
  - Ejecutar migración en ambiente de desarrollo

- [ ] **Entidades JPA**
  - Crear `entity/AuditoriaCambios.java`
  - Crear enum `ChangeType` (CREATE, UPDATE, DELETE)
  - Crear enum `GxpDataClassification` (CRITICAL, IMPORTANT, STANDARD)
  - Crear repositorio `AuditoriaCambiosRepository.java`

- [ ] **Testing básico**
  - Tests unitarios de entidad
  - Tests de repositorio (queries)

**Semana 4: Lógica de Captura**
- [ ] **Aspect AOP**
  - Crear `aspect/AuditTrailAspect.java`
  - Implementar interceptor de métodos `@Transactional`
  - Implementar captura de estado antes/después
  - Implementar comparación de valores (old vs new)
  - Implementar detección automática de campos GxP relevantes

- [ ] **Servicio de Auditoría**
  - Crear `service/AuditTrailService.java`
  - Método `recordChange(entity, field, oldValue, newValue, reason)`
  - Método `getAuditHistory(entityType, entityId)`
  - Método `searchAuditTrail(filters)`

- [ ] **Testing de captura**
  - Tests de Aspect con mocks
  - Tests de integración con BD real
  - Verificar captura en todos los servicios CU críticos

**Semana 5: Integración UI**
- [ ] **Modificar Controllers**
  - Agregar parámetro `motivoCambio` en todos los POST/PUT
  - Validar que motivo sea obligatorio para cambios GxP
  - Pasar motivo al servicio correspondiente

- [ ] **Modificar Templates Thymeleaf**
  - Agregar campo `<textarea name="motivoCambio">` en todos los forms de modificación
  - Agregar validación frontend (min 20 caracteres)
  - Agregar ejemplos/placeholders para guiar usuario
  - Aplicar a todos los módulos CU:
    - `cu/resultado-analisis/form.html`
    - `cu/liberacion-ventas/form.html`
    - `cu/dictamen-cuarentena/form.html`
    - `cu/reverso-movimiento/form.html`
    - (15+ templates)

- [ ] **Vista de Audit Trail**
  - Crear `templates/auditoria/historial.html`
  - Tabla con: fecha, usuario, campo, valor anterior, valor nuevo, motivo
  - Filtros: fecha, usuario, entidad, tipo de cambio
  - Botón "Imprimir Audit Trail"
  - CSS para impresión

**Semana 6: Testing y Refinamiento**
- [ ] **Testing exhaustivo**
  - Test de cada caso de uso CU (25+ casos)
  - Verificar captura correcta en escenarios:
    - Alta de lote
    - Modificación de dictamen
    - Resultado de análisis
    - Liberación de lote
    - Reverso de movimiento
  - Verificar que cambios NO GxP no requieren motivo

- [ ] **Performance testing**
  - Verificar que audit trail no degrada performance
  - Optimizar queries si necesario
  - Verificar índices de BD

- [ ] **Refinamiento**
  - Ajustes de UI/UX basados en testing
  - Corrección de bugs
  - Optimizaciones

- [ ] **Documentación técnica**
  - Javadoc completo
  - Diagrama de arquitectura de Audit Trail
  - Guía de uso para desarrolladores

**Entregables Semana 3-6:**
- ✅ Audit Trail completamente funcional
- ✅ Todos los cambios GxP capturados con motivo
- ✅ UI actualizada en todos los módulos
- ✅ Tests pasando (cobertura >80%)
- ✅ Documentación técnica completa

**Recursos:**
- Dev Senior: 100% tiempo (arquitectura, Aspect AOP)
- Dev Junior: 100% tiempo (modificación de forms)
- QA: 50% tiempo (testing)

---

### Semana 7-10: Implementación Firma Electrónica

#### Objetivo
Implementar sistema de firma electrónica conforme a 21 CFR Part 11 y ANMAT Anexo 6.

#### Actividades

**Semana 7: Fundamentos**
- [ ] **Base de datos**
  - Crear migración `V4__create_firma_electronica.sql`
  - Crear tabla `firmas_electronicas`
  - Crear tabla `user_pins` (para segundo factor)
  - Agregar campos a entidades que requieren firma:
    - `lote.firma_liberacion_id`
    - `analisis.firma_resultado_id`
    - `movimiento.firma_reverso_id`

- [ ] **Entidades**
  - Crear `entity/FirmaElectronica.java` (ver spec en Gap Analysis)
  - Crear `entity/UserPin.java` para segundo factor
  - Crear repositorios correspondientes

- [ ] **Testing básico**
  - Tests unitarios de entidades
  - Tests de generación de hash criptográfico
  - Tests de verificación de integridad

**Semana 8: Lógica de Firma**
- [ ] **Servicio de Firma**
  - Crear `service/ElectronicSignatureService.java`
  - Implementar `signRecord(request)` (ver spec en Gap Analysis)
  - Implementar verificación de segundo factor
  - Implementar captura de data snapshot (JSON)
  - Implementar generación de signature hash (SHA-256)
  - Implementar binding permanente firma-registro

- [ ] **Gestión de PINs**
  - Crear `service/UserPinService.java`
  - Método `assignPin(user)` - genera PIN aleatorio 6 dígitos
  - Método `verifyPin(user, pin)` - valida PIN
  - Método `resetPin(user)` - regenera PIN
  - PINs hasheados con BCrypt

- [ ] **Autorización**
  - Verificar que solo Personas Cualificadas pueden firmar liberaciones
  - Verificar que usuario tiene rol adecuado para operación
  - Integrar con `ReversoAuthorizationService` existente

- [ ] **Testing**
  - Tests unitarios de servicio
  - Tests de autorización
  - Tests de verificación de PIN
  - Tests de generación de hash

**Semana 9: Integración UI**
- [ ] **Modal de Firma**
  - Crear `templates/firma/modal-firma-electronica.html` (ver spec en Gap Analysis)
  - Formulario con:
    - Re-ingreso de contraseña (primer factor)
    - Ingreso de PIN (segundo factor)
    - Campo de comentarios opcional
    - Checkbox de declaración
  - Validación frontend
  - JavaScript para submit y feedback

- [ ] **Controller de Firma**
  - Crear `controller/FirmaElectronicaController.java`
  - Endpoint `POST /firma/firmar`
  - Validar request
  - Llamar a `ElectronicSignatureService`
  - Retornar resultado (éxito/error)
  - Manejar excepciones (PIN incorrecto, usuario no autorizado, etc.)

- [ ] **Integración en flujos críticos**
  - Liberación de lote (req. firma de Persona Cualificada)
  - Aprobación de resultado de análisis
  - Reverso de movimientos críticos
  - Agregar botón "Firmar" en vistas correspondientes
  - Mostrar modal de firma al hacer clic

- [ ] **Vista de Firma**
  - Mostrar firmas existentes en detalle de registro
  - Tabla con: firmante, fecha/hora, significado, hash
  - Indicador de verificación de integridad (✅ Válida / ❌ Comprometida)
  - Botón "Verificar Integridad" para auditoría

**Semana 10: Testing y Documentación**
- [ ] **Testing exhaustivo**
  - Test de liberación de lote con firma
  - Test de rechazo (usuario sin autorización)
  - Test de rechazo (PIN incorrecto)
  - Test de múltiples firmas en mismo registro
  - Test de verificación de integridad
  - Test de impresión con firmas visibles

- [ ] **Testing de seguridad**
  - Test de firma sin autenticación (debe fallar)
  - Test de modificación de registro firmado (debe invalidar firma)
  - Test de reutilización de firma (debe fallar)
  - Test de firma con usuario expirado (debe fallar)

- [ ] **Gestión de PINs**
  - Implementar UI para asignación inicial de PIN
  - Implementar UI para reset de PIN (solo admin)
  - Notificación por email de PIN asignado
  - Forzar cambio de PIN en primer uso

- [ ] **Documentación**
  - SOP-FIRMA-001: Procedimiento de Uso de Firma Electrónica
  - Guía de usuario: Cómo firmar electrónicamente
  - Documentación técnica de arquitectura
  - ERU-FIRMA-001: Requerimientos de Firma Electrónica

**Entregables Semana 7-10:**
- ✅ Sistema de firma electrónica funcional
- ✅ Liberación de lotes con firma implementada
- ✅ Segundo factor (PIN) funcional
- ✅ Verificación de integridad implementada
- ✅ SOP de firma electrónica aprobado
- ✅ Tests de seguridad pasando

**Recursos:**
- Dev Senior: 100% tiempo
- Dev Junior: 50% tiempo (UI)
- QA: 50% tiempo (testing de seguridad)
- Validation Specialist: 20% tiempo (review de spec)

---

### Semana 11: Políticas de Contraseña

#### Objetivo
Implementar políticas robustas de contraseña conforme a NIST SP 800-63B.

#### Actividades

**Semana 11: Implementación Completa**
- [ ] **Base de datos**
  - Migración `V5__password_policies.sql`
  - Agregar campos a `users`:
    - `password_changed_at`
    - `password_expires_at`
    - `account_locked`
    - `lockout_until`
    - `failed_login_attempts`
    - `last_failed_login`
  - Crear tabla `password_history` (últimas 5 contraseñas)
  - Crear tabla `failed_login_attempts`

- [ ] **Servicio de Políticas**
  - Crear `service/PasswordPolicyService.java` (ver spec en Gap Analysis)
  - Validación de complejidad (12+ chars, mayúsc, minúsc, números, especiales)
  - Validación de historial (no reutilizar últimas 5)
  - Validación de no contener username
  - Expiración automática a 90 días

- [ ] **Servicio de Lockout**
  - Crear `service/AccountLockoutService.java` (ver spec en Gap Analysis)
  - Registrar intentos fallidos
  - Lockout automático después de 5 intentos en 15 minutos
  - Lockout por 30 minutos
  - Notificación a admin de lockout
  - Auto-unlock después de tiempo cumplido

- [ ] **Integración con Login**
  - Modificar `CustomUserDetailsService.java`
  - Verificar si cuenta está bloqueada
  - Verificar si contraseña está expirada
  - Registrar intento fallido
  - Limpiar intentos fallidos en login exitoso
  - Forzar cambio de contraseña si expirada

- [ ] **UI de Cambio de Contraseña**
  - Crear `templates/usuario/cambiar-password.html`
  - Formulario con:
    - Contraseña actual
    - Nueva contraseña
    - Confirmar nueva contraseña
  - Indicador de fortaleza en tiempo real (JavaScript)
  - Mensajes de validación claros
  - Requisitos visibles

- [ ] **Pantalla de Expiración**
  - Interceptar login de usuario con contraseña expirada
  - Redirigir a pantalla de cambio obligatorio
  - No permitir acceso hasta cambiar contraseña

- [ ] **Gestión de Usuarios (Admin)**
  - UI para unlock de cuenta (admin)
  - UI para reset de contraseña (admin)
  - UI para ver historial de intentos fallidos

- [ ] **Testing**
  - Test de validación de complejidad
  - Test de historial (rechazo de reutilización)
  - Test de lockout (5 intentos)
  - Test de auto-unlock
  - Test de expiración forzada

- [ ] **Documentación**
  - SOP-SEC-001: Política de Contraseñas
  - Documento de políticas de seguridad
  - Actualizar manual de usuario

**Entregables Semana 11:**
- ✅ Políticas de contraseña implementadas
- ✅ Account lockout funcional
- ✅ Expiración de contraseña a 90 días
- ✅ Historial de contraseñas
- ✅ UI de cambio de contraseña
- ✅ SOP de políticas de seguridad

**Recursos:**
- Dev Senior: 100% tiempo
- QA: 50% tiempo
- Validation Specialist: 10% tiempo (review SOP)

---

### Semana 12: ERU y Cierre de Fase 1

#### Objetivo
Completar documentación de Especificaciones de Requerimientos de Usuario y preparar para Fase 2.

#### Actividades

**Semana 12: Documentación y Revisión**
- [ ] **Completar ERU**
  - Finalizar documento ERU-001-Especificaciones-Requerimientos-Usuario.pdf
  - Secciones:
    - Requerimientos funcionales (FR-xxx)
    - Requerimientos no funcionales (NFR-xxx)
    - Criterios de aceptación por requerimiento
    - Matriz de trazabilidad preliminar
  - Revisión por Process Owner
  - Revisión por QA
  - Aprobación formal (firmas)

- [ ] **Documentación técnica consolidada**
  - Documento de arquitectura actualizado
  - Diagramas de flujo de datos
  - Diagramas de secuencia para operaciones críticas
  - Modelo de datos actualizado

- [ ] **Testing de integración completo**
  - Smoke tests de todas las funcionalidades
  - Testing de integración Audit Trail + Firma Electrónica
  - Testing de flujo completo: ingreso lote → análisis → firma → liberación
  - Verificar audit trail captura todo el flujo

- [ ] **Demo para stakeholders**
  - Preparar demo de nuevas funcionalidades
  - Mostrar Audit Trail en acción
  - Mostrar Firma Electrónica
  - Mostrar políticas de contraseña
  - Recoger feedback

- [ ] **Deployment a ambiente de pruebas**
  - Deploy de branch `feature/anmat-compliance` a ambiente QA
  - Migrar base de datos
  - Ejecutar smoke tests en QA
  - Permitir testing por usuarios finales (UAT inicial)

- [ ] **Planificación Fase 2**
  - Definir cronograma detallado de Fase 2
  - Asignar recursos para validación formal
  - Programar workshops de validación
  - Programar auditorías de proveedores

- [ ] **Revisión de presupuesto**
  - Comparar gasto real vs presupuestado Fase 1
  - Ajustar presupuesto Fase 2 si necesario
  - Aprobar continuación a Fase 2

**Entregables Semana 12:**
- ✅ ERU-001 completo y aprobado
- ✅ Documentación técnica actualizada
- ✅ Sistema en ambiente QA funcionando
- ✅ Demo exitoso para stakeholders
- ✅ Plan detallado Fase 2 aprobado

**Recursos:**
- Validation Specialist: 100% tiempo (ERU)
- Process Owner: 30% tiempo (review ERU)
- QA: 50% tiempo (testing)
- Dev Senior: 30% tiempo (fixes de bugs)
- System Owner: 20% tiempo (aprobaciones)

---

### Criterios de Salida Fase 1

**Requisitos para pasar a Fase 2:**

✅ **Funcionalidades Implementadas:**
- [ ] Audit Trail capturando todos los cambios GxP con motivo
- [ ] Firma electrónica funcional en liberación de lotes
- [ ] Políticas de contraseña robustas activas
- [ ] Segundo factor (PIN) implementado

✅ **Documentación:**
- [ ] ERU-001 completo y aprobado por QA y Process Owner
- [ ] SOPs creados: SOP-FIRMA-001, SOP-SEC-001
- [ ] Documentación técnica actualizada

✅ **Testing:**
- [ ] Tests unitarios pasando (cobertura >80%)
- [ ] Tests de integración pasando
- [ ] UAT inicial completado sin issues críticos

✅ **Deployment:**
- [ ] Sistema desplegado en ambiente QA
- [ ] Usuarios de prueba creados
- [ ] Datos de prueba cargados

✅ **Aprobaciones:**
- [ ] System Owner aprueba funcionalidades
- [ ] Process Owner aprueba ERU
- [ ] QA aprueba calidad de código
- [ ] Presupuesto Fase 2 aprobado

**Decisión GO/NO-GO:** Reunión formal con todos los stakeholders para decidir avance a Fase 2.

---

## Fase 2: Validación y Seguridad (Meses 4-6)

### Objetivo
Validar formalmente el sistema y fortalecer controles de seguridad.

### Duración
**3 meses (12 semanas)**

### Entregables Clave
1. ✅ Plan Maestro de Validación (PMV) aprobado
2. ✅ Protocolos IQ/OQ/PQ ejecutados
3. ✅ Multi-Factor Authentication (MFA) implementado
4. ✅ Sistema de gestión de cambios formal
5. ✅ Backup automático con verificación

### Presupuesto Fase 2
**USD $70,000 - $90,000**
- Recursos humanos: 1,200 horas
- Herramientas/licencias: $10,000

---

### Semana 13-14: Plan Maestro de Validación

#### Actividades

**Semana 13:**
- [ ] **Workshop de Validación**
  - Reunir: Validation Specialist, QA, System Owner, Process Owner
  - Definir alcance de validación
  - Identificar sistemas/módulos a validar
  - Clasificar criticidad (GAMP categories)
  - Definir enfoque de validación por módulo

- [ ] **Inicio de PMV**
  - Crear documento PMV-001-Plan-Maestro-Validacion.pdf
  - Sección 1: Introducción y alcance
  - Sección 2: Organización y responsabilidades
  - Sección 3: Estrategia de validación
  - Sección 4: Documentación requerida
  - Sección 5: Cronograma de validación

- [ ] **Risk Assessment formal**
  - Completar documento CONITRACK-RA-001-Risk-Assessment.pdf
  - Matriz de riesgo por funcionalidad
  - Evaluación de impacto en paciente
  - Justificación de controles de mitigación

**Semana 14:**
- [ ] **Completar PMV**
  - Sección 6: Criterios de aceptación
  - Sección 7: Gestión de desvíos
  - Sección 8: Control de cambios durante validación
  - Sección 9: Reportes de validación
  - Apéndices: Glosario, referencias normativas

- [ ] **Revisión y aprobación PMV**
  - Revisión por QA
  - Revisión por System Owner
  - Revisión por Process Owner
  - Aprobación formal (firmas)
  - Distribución a equipo

- [ ] **Vendor Qualification**
  - Iniciar calificación de proveedores críticos:
    - PostgreSQL (COTS software)
    - Spring Framework
    - Docker
  - Crear carpeta `docs/proveedores/`
  - Templates de vendor assessment
  - Iniciar vendor documentation review

**Entregables Semana 13-14:**
- ✅ PMV-001 completo y aprobado
- ✅ Risk Assessment formal documentado
- ✅ Vendor qualification iniciada
- ✅ Cronograma detallado de validación

---

### Semana 15-18: Desarrollo de Protocolos y EDS

#### Actividades

**Semana 15: EDS (Especificaciones de Diseño)**
- [ ] **Crear EDS-001**
  - Documento EDS-001-Especificaciones-Diseño-Sistema.pdf
  - Arquitectura del sistema
  - Diseño de base de datos
  - Diseño de interfaces
  - Diseño de seguridad
  - Diseño de audit trail
  - Diseño de firma electrónica
  - Trazabilidad EDS → ERU

**Semana 16: Protocolo IQ**
- [ ] **Crear IQ-001**
  - Documento IQ-001-Installation-Qualification.pdf
  - Test cases de instalación:
    - Verificar versiones de software
    - Verificar configuración de servidor
    - Verificar conectividad de BD
    - Verificar variables de entorno
    - Verificar configuración de seguridad
    - Verificar logs del sistema
  - Criterios de aceptación por test
  - Formatos de registro de resultados

**Semana 17: Protocolo OQ**
- [ ] **Crear OQ-001**
  - Documento OQ-001-Operational-Qualification.pdf
  - Test cases operacionales (50+ tests):
    - Funcionalidades de audit trail
    - Funcionalidades de firma electrónica
    - Políticas de contraseña
    - Control de acceso por rol
    - Backup y restore
    - Cada caso de uso CU (25+)
  - Para cada test:
    - Prerequisitos
    - Procedimiento paso a paso
    - Datos de entrada
    - Resultados esperados
    - Criterios de aceptación

**Semana 18: Protocolo PQ**
- [ ] **Crear PQ-001**
  - Documento PQ-001-Performance-Qualification.pdf
  - Test cases de performance (20+ tests):
    - Flujos de negocio end-to-end
    - Escenarios de usuario real
    - Testing con datos reales (o realistas)
    - Tests de carga (múltiples usuarios)
    - Tests de integridad de datos
  - Ejemplos:
    - PQ-TC-001: Flujo completo ingreso compra → análisis → liberación
    - PQ-TC-002: Revisión de audit trail de lote liberado
    - PQ-TC-003: Firma de liberación por Persona Cualificada
    - PQ-TC-004: Reverso de movimiento con auditoría
    - PQ-TC-005: Backup y restore sin pérdida de datos

- [ ] **Revisión de protocolos**
  - Peer review por otro Validation Specialist
  - Revisión por QA
  - Aprobación formal de todos los protocolos

**Entregables Semana 15-18:**
- ✅ EDS-001 completo y aprobado
- ✅ IQ-001 completo y aprobado
- ✅ OQ-001 completo y aprobado
- ✅ PQ-001 completo y aprobado
- ✅ Protocolos listos para ejecución

---

### Semana 19-21: Ejecución de Validación

#### Actividades

**Semana 19: Ejecución IQ**
- [ ] **Preparación**
  - Ambiente de validación dedicado (aislado de desarrollo)
  - Snapshot de sistema "as validated"
  - Verificar que sistema está congelado (no se permiten cambios)

- [ ] **Ejecución IQ-001**
  - Ejecutar todos los test cases del protocolo IQ
  - Registrar resultados en protocolo
  - Capturar screenshots como evidencia
  - Firmar cada test case completado
  - Registrar cualquier desviación

- [ ] **Reporte IQ**
  - Crear IQ-001-REPORTE.pdf
  - Resumen de ejecución
  - Resultados por test case (Pass/Fail)
  - Desviaciones registradas (si las hay)
  - Conclusión: Sistema instalado correctamente

**Semana 20: Ejecución OQ**
- [ ] **Ejecución OQ-001 (Parte 1)**
  - Ejecutar test cases de funcionalidades críticas:
    - Audit trail (10 tests)
    - Firma electrónica (8 tests)
    - Control de acceso (12 tests)
    - Políticas de contraseña (6 tests)
  - Registrar resultados
  - Capturar evidencia (screenshots, logs)
  - Registrar desviaciones

- [ ] **Ejecución OQ-001 (Parte 2)**
  - Ejecutar test cases de casos de uso CU:
    - Alta Ingreso Compra
    - Resultado Análisis
    - Liberación Ventas
    - Trazado Lote
    - Reverso Movimiento
    - (20+ casos de uso)
  - Registrar resultados
  - Capturar evidencia

- [ ] **Reporte OQ**
  - Crear OQ-001-REPORTE.pdf
  - Resumen ejecutivo
  - Resultados consolidados
  - Análisis de desviaciones
  - Conclusión: Sistema opera conforme a especificaciones

**Semana 21: Ejecución PQ**
- [ ] **Ejecución PQ-001**
  - Ejecutar escenarios end-to-end con datos realistas
  - Simular operación normal por 5 días seguidos
  - Involucrar usuarios finales (UAT formal)
  - Test de carga: 10 usuarios concurrentes
  - Test de backup/restore
  - Test de disaster recovery

- [ ] **Reporte PQ**
  - Crear PQ-001-REPORTE.pdf
  - Evidencia de performance adecuado
  - Resultados de UAT
  - Evidencia de integridad de datos
  - Conclusión: Sistema cumple propósito GxP

- [ ] **Gestión de Desviaciones**
  - Para cada desviación encontrada:
    - Clasificar criticidad (Crítica/Mayor/Menor)
    - Documentar causa raíz
    - Definir CAPA (Corrective and Preventive Action)
    - Re-ejecutar tests afectados después de corrección

**Entregables Semana 19-21:**
- ✅ IQ ejecutado y reportado
- ✅ OQ ejecutado y reportado
- ✅ PQ ejecutado y reportado
- ✅ Todas las desviaciones cerradas
- ✅ Sistema en "estado validado"

---

### Semana 22-24: Implementaciones de Seguridad Adicionales

#### Actividades

**Semana 22: Multi-Factor Authentication (MFA)**
- [ ] **Decisión de método MFA**
  - Opción A: TOTP (Time-based One-Time Password) - Google Authenticator, Authy
  - Opción B: SMS OTP
  - Opción C: Email OTP
  - **Recomendado: Opción A (TOTP)** por seguridad

- [ ] **Implementación TOTP**
  - Agregar dependencia: Google Authenticator library
  - Crear `entity/UserMfaSecret.java`
  - Crear `service/MfaService.java`
    - Método `generateSecret(user)` → QR code
    - Método `verifyCode(user, code)` → boolean
  - Modificar login flow:
    - Después de username/password correcto
    - Solicitar código MFA
    - Validar código
    - Permitir acceso solo si código es válido

- [ ] **UI de MFA**
  - Pantalla de setup inicial (mostrar QR code)
  - Pantalla de ingreso de código en login
  - UI para backup codes (códigos de recuperación)
  - Opción de "confiar en este dispositivo por 30 días"

- [ ] **Testing MFA**
  - Test de setup correcto
  - Test de login con código correcto
  - Test de rechazo con código incorrecto
  - Test de backup codes

**Semana 23: Sistema de Gestión de Cambios**
- [ ] **Implementación (ver Gap #5 en doc 03)**
  - Crear `entity/ChangeRequest.java`
  - Crear `service/ChangeControlService.java`
  - Workflow de aprobación (System Owner → QA)
  - UI de change requests
  - Link entre CR y Git commits

- [ ] **SOP de Change Control**
  - Crear SOP-IT-001-Gestion-Cambios.md
  - Definir proceso formal
  - Criterios de re-validación
  - Aprobación por QA

**Semana 24: Backup Automatizado**
- [ ] **Implementación**
  - Configurar cron job / scheduled task
  - Backup diario automático a las 2 AM
  - Rotación de backups (retener 30 días, semanales 1 año)
  - Verificación automática de integridad
  - Alertas por email si backup falla

- [ ] **Protocolo de Backup/Restore**
  - Crear OQ-BACKUP-001
  - Test de backup automático
  - Test de restore
  - Test de backup encryption (si aplica)

- [ ] **Disaster Recovery Plan**
  - Documento de DR Plan
  - RTO (Recovery Time Objective): 4 horas
  - RPO (Recovery Point Objective): 24 horas
  - Procedimiento de recuperación paso a paso

**Entregables Semana 22-24:**
- ✅ MFA implementado y funcional
- ✅ Sistema de gestión de cambios operativo
- ✅ Backup automático configurado y validado
- ✅ DR Plan documentado
- ✅ SOPs de seguridad completos

---

### Criterios de Salida Fase 2

✅ **Validación Completa:**
- [ ] PMV aprobado
- [ ] Protocolos IQ/OQ/PQ ejecutados exitosamente
- [ ] Reportes de validación aprobados
- [ ] Todas las desviaciones cerradas
- [ ] Sistema declarado "validado"

✅ **Seguridad:**
- [ ] MFA implementado
- [ ] Gestión de cambios formal operativa
- [ ] Backup automático funcional
- [ ] DR Plan aprobado

✅ **Documentación:**
- [ ] Todos los SOPs completos y aprobados
- [ ] Vendor qualification completada
- [ ] Documentación técnica actualizada

**Decisión GO/NO-GO:** Sistema listo para Fase 3 (mejora continua).

---

## Fase 3: Mejora Continua y Compliance (Meses 7-9)

### Objetivo
Implementar sistemas de monitoreo, gestión de incidencias y preparación para auditorías.

### Duración
**3 meses (12 semanas)**

### Entregables Clave
1. ✅ Sistema de gestión de incidencias
2. ✅ Evaluación periódica automatizada
3. ✅ Dashboards de compliance
4. ✅ SOPs completos
5. ✅ Training program para usuarios

### Presupuesto Fase 3
**USD $45,000 - $60,000**

---

### Semana 25-27: Sistema de Gestión de Incidencias

*(Implementación completa según Requisito 13 - ver documento 02)*

- [ ] Crear entidad `Incident.java`
- [ ] Workflow de incidentes (NEW → INVESTIGATING → RESOLVED → CLOSED)
- [ ] Root Cause Analysis (RCA) integrado
- [ ] CAPA (Corrective and Preventive Actions)
- [ ] UI de gestión de incidencias
- [ ] Reportes de incidentes
- [ ] SOP-INCIDENT-001

---

### Semana 28-30: Evaluación Periódica y Métricas

*(Implementación según Requisito 11 - ver documento 02)*

- [ ] Crear entidad `SystemPeriodicReview.java`
- [ ] `SystemMetricsService.java` - generación de métricas
- [ ] Dashboard de compliance (tiempo real)
- [ ] Reportes automáticos mensuales
- [ ] SOP-QA-002-Evaluacion-Periodica.md
- [ ] Programar primera evaluación trimestral

---

### Semana 31-33: Training y Documentación Final

- [ ] Programa de capacitación en BPF
- [ ] Training en uso de Conitrack
- [ ] Training en firma electrónica
- [ ] Certificación de usuarios
- [ ] Completar todos los SOPs faltantes
- [ ] Manual de usuario completo
- [ ] Manual de administrador

---

### Criterios de Salida Fase 3

✅ **Operacional:**
- [ ] Gestión de incidencias funcional
- [ ] Evaluaciones periódicas programadas
- [ ] Dashboards de métricas activos

✅ **Training:**
- [ ] 100% de usuarios capacitados
- [ ] Certificados de training emitidos
- [ ] Material de training aprobado

✅ **Documentación:**
- [ ] Todos los SOPs completos
- [ ] Manuales de usuario/admin
- [ ] Procedimientos de emergencia

---

## Fase 4: Auditoría y Certificación (Meses 10-12)

### Objetivo
Preparar y aprobar auditorías internas y externas previo a inspección ANMAT.

### Duración
**3 meses (12 semanas)**

### Entregables Clave
1. ✅ Mock audit interna exitosa
2. ✅ Remediación de findings
3. ✅ Auditoría externa pre-ANMAT
4. ✅ Sistema certificado y listo para inspección

### Presupuesto Fase 4
**USD $40,000 - $50,000**

---

### Semana 34-36: Mock Audit Interna

- [ ] Contratar auditor interno certificado
- [ ] Ejecutar mock audit completa (5 días)
- [ ] Documentar findings (esperados: 10-15)
- [ ] Clasificar findings por criticidad
- [ ] Plan de remediación

---

### Semana 37-42: Remediación y Re-audit

- [ ] Implementar correcciones de findings críticos
- [ ] Implementar correcciones de findings mayores
- [ ] Re-ejecutar tests afectados
- [ ] Re-audit de findings corregidos
- [ ] Cerrar todos los findings

---

### Semana 43-48: Auditoría Externa y Preparación ANMAT

- [ ] Contratar auditor externo (experiencia ANMAT)
- [ ] Ejecutar auditoría externa pre-ANMAT
- [ ] Remediación de findings finales
- [ ] Preparar war room para inspección ANMAT
- [ ] Simulacros de inspección
- [ ] Sistema APROBADO y listo

---

### Criterios de Éxito Final

✅ **Auditoría:**
- [ ] Cero findings críticos en auditoría externa
- [ ] Todos los findings cerrados
- [ ] Auditor externo recomienda aprobación

✅ **Sistema:**
- [ ] 100% funcional en producción
- [ ] Estado "validado" mantenido
- [ ] Documentación completa

✅ **Organización:**
- [ ] Personal capacitado
- [ ] Procesos establecidos
- [ ] Listo para inspección ANMAT

---

## Resumen de Recursos por Fase

| Fase | Duración | Horas | Costo | Personal Clave |
|------|----------|-------|-------|----------------|
| **Fase 1** | 3 meses | 960 hrs | $50-70K | 2 Devs, 1 QA, 1 Validation |
| **Fase 2** | 3 meses | 1,200 hrs | $70-90K | 1 Dev, 1 QA, 1 Validation |
| **Fase 3** | 3 meses | 800 hrs | $45-60K | 1 Dev, 1 QA, 1 Trainer |
| **Fase 4** | 3 meses | 600 hrs | $40-50K | Auditores, QA, System Owner |
| **TOTAL** | **12 meses** | **3,560 hrs** | **$205-270K** | **Variable por fase** |

---

[← Gap Analysis](./ANMAT-03-GAP-ANALYSIS.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md) | [Siguiente: Especificaciones Técnicas →](./ANMAT-05-ESPECIFICACIONES-TECNICAS.md)
