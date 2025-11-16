# PROMPTS PENDIENTES - CONITRACK

Este archivo contiene prompts simples y específicos para ejecutar las mejoras, correcciones y tareas identificadas en el análisis completo del proyecto. Ejecutar en orden de prioridad.

---

## 🔴 CRÍTICO - SEGURIDAD (Semana 1)

### S-001: Password Policy
```
Implementar validación de contraseñas en PasswordChangeService y CustomUserDetailsService:
- Longitud mínima: 12 caracteres
- Al menos 1 mayúscula, 1 minúscula, 1 número, 1 carácter especial
- Crear clase PasswordValidator con método validar()
- Actualizar formulario password-change.html con mensajes de validación
```

### S-002: Default Users
```
Eliminar usuarios por defecto con password=username en DataInitializer.java:
- Comentar o eliminar creación de usuarios test
- Implementar flag "requierecambioPassword" en entidad User
- Crear filtro que redirija a cambio de password obligatorio en primer login
- Actualizar SecurityConfig para permitir acceso a /password-change sin autenticación completa
```

### S-003: Database Credentials
```
Rotar credenciales de base de datos expuestas:
1. Acceder a Heroku dashboard y cambiar DATABASE_URL
2. Actualizar application-DEV.yml con nuevas credenciales locales
3. Crear .env.example con plantilla de variables (sin valores reales)
4. Verificar que .env esté en .gitignore
5. Documentar en README.md el proceso de configuración de credenciales
```

### S-004: Authorization Bypass
```
Descomentar TODAS las anotaciones @PreAuthorize en los siguientes archivos:
- AltaIngresoCompraController.java
- AltaIngresoProduccionController.java
- AltaDevolucionVentaController.java
- AltaRetiroMercadoController.java
- BajaVentaProductoController.java
- BajaConsumoProduccionController.java
- BajaMuestreoBultoController.java
- BajaDevolucionCompraController.java
- ModifResultadoAnalisisController.java
- ModifAnulacionAnalisisController.java
- ModifTrazadoLoteController.java
- ModifLiberacionVentasController.java
- ModifReanalisisLoteController.java
- ModifDictamenCuarentenaController.java
- ModifReversoMovimientoController.java

Verificar que cada usuario tenga roles correctos en la base de datos.
```

### S-005: Rate Limiting
```
Implementar rate limiting en login:
1. Agregar dependencia bucket4j en build.gradle
2. Crear LoginAttemptService con mapa de intentos por IP
3. Configurar límite: 5 intentos fallidos por IP en 15 minutos
4. Actualizar CustomAuthenticationFailureHandler para invocar LoginAttemptService
5. Crear tabla auditoria_intentos_login para persistir intentos
6. Agregar mensaje en login.html cuando se bloquea IP
```

---

## 🟠 ALTA PRIORIDAD (Semanas 2-3)

### L-001: Logback Configuration
```
Crear src/main/resources/logback-spring.xml con la siguiente configuración:
- Logs de aplicación: logs/conitrack-app.log (rotación diaria, retención 90 días)
- Logs de auditoría: logs/conitrack-audit.log (rotación diaria, retención 365 días)
- Logs de errores: logs/conitrack-error.log (solo nivel ERROR)
- Pattern: %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
- MaxFileSize: 50MB por archivo
- Comprimir archivos antiguos (.gz)
```

### L-002: Logging en CU Services - Parte 1
```
Agregar logging a los siguientes 5 servicios CU (grupos de alta prioridad):
1. AltaIngresoCompraService.java
2. AltaIngresoProduccionService.java
3. BajaVentaProductoService.java
4. BajaConsumoProduccionService.java
5. ModifResultadoAnalisisService.java

En cada servicio:
- Agregar: private static final Logger log = LoggerFactory.getLogger(NombreClase.class);
- Log INFO al inicio de cada método público con parámetros
- Log INFO al finalizar operación exitosa con resultado
- Log ERROR en cada catch con stacktrace completo
- Log WARN para validaciones fallidas
```

### L-003: Logging en CU Services - Parte 2
```
Agregar logging a los siguientes 5 servicios CU:
1. AltaDevolucionVentaService.java
2. AltaRetiroMercadoService.java
3. BajaMuestreoBultoService.java
4. BajaDevolucionCompraService.java
5. ModifAnulacionAnalisisService.java

Seguir el mismo patrón que L-002.
```

### L-004: Logging en CU Services - Parte 3
```
Agregar logging a los siguientes 6 servicios CU:
1. ModifTrazadoLoteService.java
2. ModifLiberacionVentasService.java
3. ModifReanalisisLoteService.java
4. ModifDictamenCuarentenaService.java
5. ModifReversoMovimientoService.java
6. AbstractCuService.java (métodos de validación)

Seguir el mismo patrón que L-002.
```

### C-001: Production Configuration
```
Crear src/main/resources/application-PROD.yml con configuración segura:
- spring.jpa.show-sql: false
- spring.jpa.properties.hibernate.format_sql: false
- logging.level.root: WARN
- logging.level.com.mb.conitrack: INFO
- server.error.include-message: never
- server.error.include-stacktrace: never
- spring.session.timeout: 30m
- spring.thymeleaf.cache: true

Documentar en README.md cómo activar perfil PROD.
```

### D-001: PostgreSQL JDBC Update
```
Actualizar PostgreSQL JDBC driver en build.gradle:
- Cambiar de runtimeOnly 'org.postgresql:postgresql:42.6.0'
- A: runtimeOnly 'org.postgresql:postgresql:42.7.4'
- Ejecutar ./gradlew clean build
- Ejecutar tests: ./gradlew test
- Verificar que no haya breaking changes
```

### M-001: Migración a Render - Parte 1
```
Crear cuenta en Render.com y configurar PostgreSQL:
1. Crear cuenta en https://render.com
2. Crear PostgreSQL database (instancia Starter $7/mo)
3. Anotar DATABASE_URL generada
4. Configurar backups diarios automáticos
5. Verificar retención de backups (7 días en plan Starter)
6. Crear variables de entorno en panel de Render
```

### M-002: Migración a Render - Parte 2
```
Configurar Web Service en Render:
1. Conectar repositorio GitHub con Render
2. Crear Web Service (instancia Starter $7/mo)
3. Build Command: ./gradlew clean bootJar
4. Start Command: java -jar build/libs/conitrack-0.0.1-SNAPSHOT.jar --spring.profiles.active=PROD
5. Configurar variables de entorno desde PostgreSQL
6. Health Check Path: /actuator/health
7. Auto-Deploy: activar desde main branch
```

### M-003: Migración a Render - Parte 3
```
Testing y validación en Render:
1. Hacer deploy inicial desde Render dashboard
2. Verificar logs de inicio exitoso
3. Acceder a la URL pública generada
4. Probar login con usuario admin
5. Realizar operación de prueba (crear lote, movimiento)
6. Verificar logs en Render dashboard
7. Verificar conectividad con PostgreSQL
8. Realizar backup manual desde Render
```

---

## 🟡 MEDIA PRIORIDAD - TESTING (Semanas 4-6)

### T-001: ModifReversoMovimientoService Tests
```
Crear tests para ModifReversoMovimientoService (0% coverage, 1784 instrucciones):
- Crear ModifReversoMovimientoServiceTest.java
- Setup con @ExtendWith(MockitoExtension.class)
- Mock: movimientoRepository, loteRepository, analisisRepository, bultoRepository
- Test reversarMovimiento_cuandoMovimientoValido_debeCrearReverso
- Test reversarMovimiento_cuandoMovimientoNoExiste_debeLanzarExcepcion
- Test reversarMovimientoAltaCompra_debeRestarCantidadesLote
- Test reversarMovimientoAltaProduccion_debeRevertirTrazas
- Test reversarMovimientoBajaVenta_debeSumarCantidadesLote
- Test reversarMovimientoResultadoAnalisis_debeAnularAnalisis
- Ejecutar: ./gradlew test --tests ModifReversoMovimientoServiceTest
- Meta: lograr 80%+ coverage en primer iteración
```

### T-002: BajaVentaProductoService Tests
```
Crear tests para BajaVentaProductoService (0% coverage):
- Crear BajaVentaProductoServiceTest.java
- Test bajaVenta_cuandoLoteDisponible_debeCrearMovimientoBaja
- Test bajaVenta_cuandoLoteInsuficiente_debeLanzarExcepcion
- Test bajaVenta_cuandoLoteRechazado_debeLanzarExcepcion
- Test validarVentaInput_cuandoFechaInvalida_debeRetornarFalse
- Test validarVentaInput_cuandoBultosInvalidos_debeRetornarFalse
- Test persistirMovimientoVenta_debeActualizarCantidadActualLote
- Ejecutar: ./gradlew test --tests BajaVentaProductoServiceTest
- Meta: 80%+ coverage
```

### T-003: AltaDevolucionVentaService Tests
```
Crear tests para AltaDevolucionVentaService (0% coverage):
- Crear AltaDevolucionVentaServiceTest.java
- Test devolucionVenta_cuandoMovimientoVentaValido_debeRevertir
- Test devolucionVenta_cuandoMovimientoNoExiste_debeLanzarExcepcion
- Test devolucionVenta_cuandoMovimientoNoEsVenta_debeLanzarExcepcion
- Test devolucionVenta_debeSumarCantidadesAlLote
- Test validarDevolucionVentaInput_conDatosValidos_debeRetornarTrue
- Ejecutar: ./gradlew test --tests AltaDevolucionVentaServiceTest
- Meta: 80%+ coverage
```

### T-004: AltaRetiroMercadoService Tests
```
Crear tests para AltaRetiroMercadoService (0% coverage):
- Crear AltaRetiroMercadoServiceTest.java
- Test retiroMercado_cuandoLoteVigente_debeCrearMovimientoAlta
- Test retiroMercado_debeCambiarEstadoLoteSuspendido
- Test retiroMercado_debeReferenciarMovimientoOriginal
- Test validarRetiroMercadoInput_conDatosValidos_debeRetornarTrue
- Ejecutar: ./gradlew test --tests AltaRetiroMercadoServiceTest
- Meta: 80%+ coverage
```

### T-005: BajaDevolucionCompraService Tests
```
Crear tests para BajaDevolucionCompraService (0% coverage):
- Crear BajaDevolucionCompraServiceTest.java
- Test devolucionCompra_cuandoMovimientoCompraValido_debeRevertir
- Test devolucionCompra_debeRestarCantidadesDelLote
- Test devolucionCompra_cuandoCantidadSuperaDisponible_debeLanzarExcepcion
- Test validarDevolucionCompraInput_conDatosValidos_debeRetornarTrue
- Ejecutar: ./gradlew test --tests BajaDevolucionCompraServiceTest
- Meta: 80%+ coverage
```

### T-006: ModifAnulacionAnalisisService Tests
```
Crear tests para ModifAnulacionAnalisisService (0% coverage):
- Crear ModifAnulacionAnalisisServiceTest.java
- Test anularAnalisis_cuandoAnalisisExiste_debeDesactivarlo
- Test anularAnalisis_debeCrearMovimientoModificacion
- Test anularAnalisis_debeCambiarDictamenLote
- Test validarAnulacionAnalisisInput_conDatosValidos_debeRetornarTrue
- Ejecutar: ./gradlew test --tests ModifAnulacionAnalisisServiceTest
- Meta: 80%+ coverage
```

### T-007: ModifTrazadoLoteService Tests
```
Crear tests para ModifTrazadoLoteService (0% coverage):
- Crear ModifTrazadoLoteServiceTest.java
- Test trazarLote_cuandoLotesValidos_debeCrearTrazas
- Test trazarLote_debeValidarCantidadesUtilizadas
- Test trazarLote_cuandoCantidadSuperaDisponible_debeLanzarExcepcion
- Test validarTrazadoInput_conDatosValidos_debeRetornarTrue
- Ejecutar: ./gradlew test --tests ModifTrazadoLoteServiceTest
- Meta: 80%+ coverage
```

### T-008: ModifLiberacionVentasService Tests
```
Crear tests para ModifLiberacionVentasService (0% coverage):
- Crear ModifLiberacionVentasServiceTest.java
- Test liberarVentas_cuandoLoteAprobado_debeCambiarEstadoVigente
- Test liberarVentas_cuandoLoteRechazado_debePermanecer
- Test liberarVentas_debeCrearMovimientoModificacion
- Test validarLiberacionInput_conDatosValidos_debeRetornarTrue
- Ejecutar: ./gradlew test --tests ModifLiberacionVentasServiceTest
- Meta: 80%+ coverage
```

### T-009: ModifReanalisisLoteService Tests
```
Crear tests para ModifReanalisisLoteService (0% coverage):
- Crear ModifReanalisisLoteServiceTest.java
- Test reanalisis_cuandoLoteAprobado_debeProgramarNuevoAnalisis
- Test reanalisis_debeActualizarFechaReanalisis
- Test reanalisis_debeCrearMovimientoModificacion
- Test validarReanalisisInput_conDatosValidos_debeRetornarTrue
- Ejecutar: ./gradlew test --tests ModifReanalisisLoteServiceTest
- Meta: 80%+ coverage
```

### T-010: ModifDictamenCuarentenaService Tests
```
Crear tests para ModifDictamenCuarentenaService (0% coverage):
- Crear ModifDictamenCuarentenaServiceTest.java
- Test dictamen_cuandoLoteEnCuarentena_debeCambiarDictamen
- Test dictamen_debeCambiarEstadoSegunResultado
- Test dictamen_debeCrearMovimientoModificacion
- Test validarDictamenInput_conDatosValidos_debeRetornarTrue
- Ejecutar: ./gradlew test --tests ModifDictamenCuarentenaServiceTest
- Meta: 80%+ coverage
```

---

## 🟢 BAJA PRIORIDAD - REFACTORING (Semanas 7-10)

### R-001: Refactorizar ModifReversoMovimientoService
```
Dividir ModifReversoMovimientoService (654 líneas) en clases más pequeñas:
1. Crear ReversoStrategyFactory con patrón Strategy
2. Crear ReversoAltaStrategy para reversiones de ALTA
3. Crear ReversoBajaStrategy para reversiones de BAJA
4. Crear ReversoModificacionStrategy para reversiones de MODIFICACION
5. Actualizar ModifReversoMovimientoService para delegar en strategies
6. Ejecutar tests para verificar no regresiones
```

### R-002: Refactorizar AbstractCuService - Parte 1
```
Dividir AbstractCuService (633 líneas, 29 métodos de validación):
1. Crear ValidationService con validaciones genéricas:
   - validarFechas()
   - validarCantidades()
   - validarBultos()
2. Mover métodos de líneas 50-250 a ValidationService
3. Actualizar AbstractCuService para inyectar ValidationService
4. Ejecutar tests de todos los servicios CU para verificar no regresiones
```

### R-003: Refactorizar AbstractCuService - Parte 2
```
Continuar división de AbstractCuService:
1. Crear AnalisisValidationService con validaciones de análisis:
   - validarNroAnalisis()
   - validarFechasReanalisis()
   - validarDatosResultadoAnalisisAprobadoInput()
2. Mover métodos de líneas 250-450 a AnalisisValidationService
3. Ejecutar tests para verificar no regresiones
```

### R-004: Refactorizar AbstractCuService - Parte 3
```
Finalizar división de AbstractCuService:
1. Crear LoteValidationService con validaciones de lotes:
   - validarFechaMovimientoPosteriorIngresoLote()
   - validarFechaEgresoLoteDtoPosteriorLote()
2. Dejar en AbstractCuService solo métodos comunes a todos los CU
3. Ejecutar suite completa de tests
4. Objetivo final: AbstractCuService con <200 líneas
```

### R-005: Protección Recursión Lote.getRootLote()
```
Agregar protección explícita a recursión en Lote.getRootLote():
1. Abrir src/main/java/com/mb/conitrack/entity/Lote.java
2. En método getRootLote(), agregar parámetro int depth con default 0
3. Agregar validación: if (depth > MAX_GENEALOGY_DEPTH) throw new IllegalStateException()
4. En llamada recursiva: loteOrigen.getRootLote(depth + 1)
5. Crear test: testGetRootLote_cuandoRecursionExcesiva_debeLanzarExcepcion
```

### R-006: Centralizar Conversión Unidades
```
Centralizar lógica de conversión de unidades:
1. Revisar todas las clases que hacen conversiones manuales
2. Identificar duplicación en 8 ubicaciones
3. Crear UnidadMedidaConverter con métodos:
   - convertir(BigDecimal cantidad, UnidadMedidaEnum from, UnidadMedidaEnum to)
   - convertirAUnidadBase(BigDecimal cantidad, UnidadMedidaEnum from)
4. Reemplazar código duplicado con llamadas a UnidadMedidaConverter
5. Ejecutar tests completos
```

### R-007: Reemplazar Excepciones Genéricas
```
Reemplazar 8 instancias de catch(Exception e) con excepciones específicas:
1. Buscar todas las ocurrencias de "catch (Exception e)" en services/
2. Analizar contexto de cada catch
3. Reemplazar con excepciones específicas:
   - DataAccessException para errores de DB
   - IllegalArgumentException para validaciones
   - EntityNotFoundException para entidades no encontradas
4. Crear excepciones custom si es necesario
5. Actualizar tests para verificar excepciones específicas
```

### R-008: Implementar JPA Auditing
```
Implementar JPA Auditing para entidades maestro:
1. Crear @Configuration JpaAuditingConfig con @EnableJpaAuditing
2. Crear AuditorAwareImpl implements AuditorAware<String> que retorna usuario actual
3. Agregar a entidades maestro (Producto, Proveedor, Fabricante):
   - @CreatedBy String creadoPor
   - @CreatedDate OffsetDateTime creadoEn
   - @LastModifiedBy String modificadoPor
   - @LastModifiedDate OffsetDateTime modificadoEn
4. Ejecutar: ./gradlew clean build
5. Verificar migración automática con ddl-auto=update
```

---

## 📋 TESTING E2E (Opcional - Semana 11+)

### E2E-001: Flujo Completo Compra → Análisis → Venta
```
Crear test E2E para flujo completo:
1. Crear clase E2EFlujoPrincipalTest con @SpringBootTest
2. Test debe simular:
   - Alta Ingreso Compra (crear lote)
   - Muestreo de bulto
   - Resultado análisis APROBADO
   - Liberación para ventas (estado VIGENTE)
   - Baja venta producto
3. Verificar estado final del lote
4. Verificar todos los movimientos creados
5. Verificar trazabilidad completa
```

### E2E-002: Flujo Completo Producción con Trazabilidad
```
Crear test E2E para flujo de producción:
1. Crear clase E2EFlujoProduccionTest con @SpringBootTest
2. Test debe simular:
   - Alta Ingreso Compra de 3 materias primas
   - Análisis y aprobación de las 3
   - Alta Ingreso Producción con trazado de las 3 materias primas
   - Análisis del producto final
   - Venta del producto final
3. Verificar genealogía completa (getRootLote())
4. Verificar trazas en tabla Traza
5. Verificar cantidades consumidas de materias primas
```

### E2E-003: Flujo Reverso Completo
```
Crear test E2E para flujo de reverso:
1. Crear clase E2EFlujoReversoTest con @SpringBootTest
2. Test debe simular:
   - Alta Ingreso Compra
   - Análisis APROBADO
   - Venta producto
   - Devolución de venta (reverso)
   - Verificar lote vuelve a tener cantidad original
3. Verificar movimientos de reverso creados
4. Verificar referencias movimientoOrigen correctas
```

---

## 📊 VERIFICACIÓN FINAL

### V-001: Verificar Coverage Global
```
Ejecutar verificación final de cobertura:
1. ./gradlew clean test jacocoTestReport
2. Abrir build/reports/jacoco/test/html/index.html
3. Verificar métricas globales:
   - Meta: >80% instruction coverage en com.mb.conitrack.service.cu
   - Meta: >70% branch coverage en com.mb.conitrack.service.cu
   - Meta: >60% instruction coverage global
4. Documentar resultados en TESTING.md
```

### V-002: Verificar Seguridad
```
Ejecutar verificación de seguridad:
1. ./gradlew dependencyCheckAnalyze
2. Revisar build/reports/dependency-check-report.html
3. Verificar que no haya vulnerabilidades CRITICAL o HIGH
4. Actualizar dependencias si es necesario
5. Ejecutar tests completos después de actualizar
```

### V-003: Verificar Logs en Producción
```
Verificar logging en ambiente productivo:
1. Deploy a Render
2. Realizar 5 operaciones diferentes (crear lote, análisis, venta, etc.)
3. Descargar logs desde Render dashboard
4. Verificar que todos los eventos estén loggeados
5. Verificar formato de logs correcto
6. Verificar que logs de auditoría estén separados
```

### V-004: Backup y Restore
```
Verificar proceso de backup y restore:
1. Crear datos de prueba en Render (10 lotes, 20 movimientos)
2. Esperar backup automático (se ejecutan cada noche)
3. Descargar backup desde Render dashboard
4. Crear base de datos local de prueba
5. Restaurar backup en base local
6. Verificar integridad de datos restaurados
7. Documentar procedimiento en BACKUP_RESTORE.md
```

---

## ✅ CHECKLIST PRE-PRODUCCIÓN

Ejecutar este checklist antes de considerar el sistema listo para producción:

```
Seguridad:
□ S-001: Password policy implementada (12+ caracteres)
□ S-002: Usuarios por defecto eliminados
□ S-003: Credenciales DB rotadas
□ S-004: Todos los @PreAuthorize descomentados
□ S-005: Rate limiting en login implementado

Logging:
□ L-001: logback-spring.xml configurado
□ L-002: Logging en CU Services Parte 1 (5 servicios)
□ L-003: Logging en CU Services Parte 2 (5 servicios)
□ L-004: Logging en CU Services Parte 3 (6 servicios)

Configuración:
□ C-001: application-PROD.yml creado
□ D-001: PostgreSQL JDBC actualizado a 42.7.4

Migración:
□ M-001: Cuenta Render creada, PostgreSQL configurado
□ M-002: Web Service configurado
□ M-003: Testing y validación completados

Testing:
□ T-001 a T-010: Tests para 10 servicios CU sin coverage
□ V-001: Coverage global >80% en service.cu
□ V-002: Sin vulnerabilidades CRITICAL/HIGH

Producción:
□ V-003: Logs verificados en producción
□ V-004: Backup y restore probados
□ README.md actualizado con instrucciones de deploy
□ BACKUP_RESTORE.md creado con procedimientos

Regulatorio:
□ Logs de auditoría con retención 365 días
□ Tabla auditoria_accesos funcionando
□ Movimientos trackeados en DB
□ Documentación lista para auditoría
```

---

## 📝 NOTAS

- **Orden de Ejecución**: Seguir el orden de prioridades (Crítico → Alta → Media → Baja)
- **Testing Continuo**: Ejecutar `./gradlew test` después de cada cambio significativo
- **Git Commits**: Hacer commit después de completar cada prompt exitosamente
- **Tiempo Estimado Total**: ~10 semanas (400 horas) para completar todos los prompts
- **Costo Render**: $14/mo ($7 PostgreSQL + $7 Web Service) con backups incluidos

**Último Update**: 2025-11-09
**Basado en**: ANALISIS_COMPLETO_PROYECTO.md
