# 07 - Anexos Técnicos
## Información Técnica del Proyecto Conitrack

[← Documentación Requerida](./ANMAT-06-DOCUMENTACION-REQUERIDA.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md)

---

## Introducción

Este documento contiene información técnica detallada del sistema Conitrack actual, útil para implementadores y auditores.

---

## 1. Estructura del Proyecto

### 1.1 Árbol de Directorios

```
C:\opt\securo\
├── src/
│   ├── main/
│   │   ├── java/com/mb/conitrack/
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── WebMvcConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── cu/                    ← Casos de Uso GxP
│   │   │   │   │   ├── AltaIngresoCompraController.java
│   │   │   │   │   ├── ModifResultadoAnalisisController.java
│   │   │   │   │   ├── ModifLiberacionVentasController.java
│   │   │   │   │   └── (20+ controllers)
│   │   │   │   ├── maestro/               ← Datos maestros
│   │   │   │   └── AbstractCuController.java
│   │   │   ├── dto/
│   │   │   │   ├── validation/            ← Grupos de validación
│   │   │   │   └── (50+ DTOs)
│   │   │   ├── entity/
│   │   │   │   ├── maestro/
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Role.java
│   │   │   │   │   ├── Producto.java
│   │   │   │   │   └── Proveedor.java
│   │   │   │   ├── Lote.java              ← Entidad central
│   │   │   │   ├── Movimiento.java
│   │   │   │   ├── Analisis.java
│   │   │   │   ├── Bulto.java
│   │   │   │   ├── Traza.java
│   │   │   │   ├── AuditoriaAcceso.java   ← Audit actual (limitado)
│   │   │   │   └── DetalleMovimiento.java
│   │   │   ├── enums/
│   │   │   │   ├── RoleEnum.java
│   │   │   │   ├── EstadoEnum.java
│   │   │   │   ├── DictamenEnum.java
│   │   │   │   ├── TipoMovimientoEnum.java
│   │   │   │   ├── MotivoEnum.java
│   │   │   │   ├── UnidadMedidaEnum.java
│   │   │   │   └── PermisosCasoUsoEnum.java
│   │   │   ├── exception/
│   │   │   ├── interceptor/
│   │   │   │   └── AuditorAccessInterceptor.java
│   │   │   ├── repository/
│   │   │   │   ├── maestro/
│   │   │   │   └── (20+ repositories)
│   │   │   ├── service/
│   │   │   │   ├── cu/                    ← Servicios de Casos de Uso
│   │   │   │   │   ├── validator/
│   │   │   │   │   │   ├── FechaValidator.java
│   │   │   │   │   │   ├── CantidadValidator.java
│   │   │   │   │   │   ├── AnalisisValidator.java
│   │   │   │   │   │   └── TrazaValidator.java
│   │   │   │   │   ├── AbstractCuService.java
│   │   │   │   │   └── (25+ services)
│   │   │   │   ├── maestro/
│   │   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   │   └── (otros servicios maestros)
│   │   │   │   ├── AuditorAccessLogger.java
│   │   │   │   ├── ReversoAuthorizationService.java
│   │   │   │   ├── SecurityContextService.java
│   │   │   │   └── LoteService.java
│   │   │   └── utils/
│   │   │       ├── LoteEntityUtils.java
│   │   │       ├── MovimientoEntityUtils.java
│   │   │       ├── UnidadMedidaUtils.java
│   │   │       └── DTOUtils.java
│   │   └── resources/
│   │       ├── db/migration/               ← Flyway migrations
│   │       │   ├── V1__initial_schema.sql
│   │       │   └── V2__add_user_hierarchy_and_tracking.sql
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── images/
│   │       ├── templates/                  ← Thymeleaf templates
│   │       │   ├── cu/                     ← Vistas de Casos de Uso
│   │       │   ├── lote/
│   │       │   ├── layout/
│   │       │   └── maestro/
│   │       ├── application.yml
│   │       ├── application-DEV.yml
│   │       └── schema.sql                  ← Schema completo
│   └── test/
│       └── java/com/mb/conitrack/
│           ├── controller/cu/
│           ├── service/cu/
│           └── (tests unitarios existentes)
├── data-base/
│   ├── Dockerfile.db
│   └── custom_backup.sh                    ← Script de backup manual
├── docs/                                    ← NUEVOS DOCS ANMAT
│   ├── ANMAT-COMPLIANCE-INDEX.md
│   ├── ANMAT-01-RESUMEN-EJECUTIVO.md
│   ├── ANMAT-02-ANALISIS-REQUISITOS.md
│   ├── ANMAT-02-ANALISIS-REQUISITOS-PARTE2.md
│   ├── ANMAT-03-GAP-ANALYSIS.md
│   ├── ANMAT-04-PLAN-IMPLEMENTACION.md
│   ├── ANMAT-05-ESPECIFICACIONES-TECNICAS.md
│   ├── ANMAT-06-DOCUMENTACION-REQUERIDA.md
│   └── ANMAT-07-ANEXOS-TECNICOS.md
├── backups/                                 ← Backups de BD
├── docker-compose.yml
├── Dockerfile
├── build.gradle  (o pom.xml)
├── CLAUDE.md                                ← Guía del proyecto
└── README.md
```

### 1.2 Estadísticas del Código

| Métrica | Valor |
|---------|-------|
| **Total de líneas de código** | ~15,000 (estimado) |
| **Clases Java** | ~150 |
| **Entidades JPA** | 12 principales |
| **Controllers** | 25+ (CU) + maestros |
| **Services** | 25+ (CU) + maestros |
| **Repositories** | 20+ |
| **Templates Thymeleaf** | 50+ |
| **Enums** | 8 principales |
| **Tests unitarios** | 50+ (cobertura ~60%) |

---

## 2. Modelo de Datos Actual

### 2.1 Entidades Principales

#### **Lote** (Entidad Central)
```java
@Entity
@Table(name = "lote")
@SQLDelete(sql = "UPDATE lote SET activo = false WHERE id = ?")
public class Lote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identificación
    @Column(unique = true, nullable = false)
    private String numeroLote;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    // Cantidades
    @Column(precision = 12, scale = 4, nullable = false)
    private BigDecimal cantidadInicial;

    @Column(precision = 12, scale = 4, nullable = false)
    private BigDecimal cantidadActual;

    @Enumerated(EnumType.STRING)
    private UnidadMedidaEnum unidadMedida;

    // Fechas
    private LocalDate fechaIngreso;
    private LocalDate fechaExpiracion;
    private LocalDate fechaElaboracion;
    private LocalDate fechaReanalisis;

    // Estados
    @Enumerated(EnumType.STRING)
    private EstadoEnum estadoLote;

    @Enumerated(EnumType.STRING)
    private DictamenEnum dictamen;

    // Genealogía
    @ManyToOne
    @JoinColumn(name = "lote_origen_id")
    private Lote loteOrigen;  // Para trazabilidad de producción

    // Tracking
    @ManyToOne
    @JoinColumn(name = "creado_por")
    private User creadoPor;

    private OffsetDateTime fechaCreacion;

    // Soft delete
    @Column(nullable = false)
    private Boolean activo = true;

    // Relaciones
    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL)
    private List<Bulto> bultos;

    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL)
    private List<Analisis> analisis;

    @OneToMany(mappedBy = "lote")
    private List<Movimiento> movimientos;

    // Métodos de negocio
    public Integer duplicateNumber() { /* ... */ }
    public Lote getRootLote() { /* ... con protección circular */ }
}
```

#### **Movimiento** (Transacciones)
```java
@Entity
@Table(name = "movimiento")
@SQLDelete(sql = "UPDATE movimiento SET activo = false WHERE id = ?")
public class Movimiento {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Enumerated(EnumType.STRING)
    private TipoMovimientoEnum tipoMovimiento;  // ALTA, BAJA, MODIFICACION

    @Enumerated(EnumType.STRING)
    private MotivoEnum motivoMovimiento;

    @Column(precision = 12, scale = 4)
    private BigDecimal cantidad;

    @Enumerated(EnumType.STRING)
    private DictamenEnum dictamenInicial;

    @Enumerated(EnumType.STRING)
    private DictamenEnum dictamenFinal;

    @ManyToOne
    @JoinColumn(name = "movimiento_origen_id")
    private Movimiento movimientoOrigen;  // Para reversiones

    @ManyToOne
    @JoinColumn(name = "creado_por")
    private User creadoPor;

    private OffsetDateTime fechaYHoraCreacion;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "movimiento", cascade = CascadeType.ALL)
    private List<DetalleMovimiento> detalles;
}
```

#### **Analisis** (Control de Calidad)
```java
@Entity
@Table(name = "analisis")
public class Analisis {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(unique = true)
    private String numeroAnalisis;

    private LocalDate fechaAnalisis;

    private String resultado;  // Texto libre o estructurado

    @Enumerated(EnumType.STRING)
    private DictamenEnum dictamen;

    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "creado_por")
    private User creadoPor;

    @Column(nullable = false)
    private Boolean activo = true;
}
```

### 2.2 Diagrama ER Simplificado

```
┌─────────────┐
│   Producto  │
│ (maestro)   │
└──────┬──────┘
       │
       │ N:1
       ↓
┌─────────────┐         ┌─────────────┐
│    Lote     │←──1:N──→│  Movimiento │
│  (central)  │         └─────────────┘
└──────┬──────┘
       │
       │ 1:N
       ↓
┌─────────────┐         ┌─────────────┐
│   Analisis  │         │    Bulto    │
└─────────────┘         └─────────────┘

┌─────────────┐         ┌─────────────┐
│    User     │────1:N─→│AuditoriaAcceso│
│  (maestro)  │         └─────────────┘
└──────┬──────┘
       │
       │ N:1
       ↓
┌─────────────┐
│    Role     │
└─────────────┘
```

### 2.3 Índices de Base de Datos

```sql
-- Índices principales existentes
CREATE INDEX idx_lote_numero ON lote(numero_lote);
CREATE INDEX idx_lote_producto ON lote(producto_id);
CREATE INDEX idx_lote_estado ON lote(estado_lote, dictamen);
CREATE INDEX idx_lote_activo ON lote(activo) WHERE activo = TRUE;

CREATE INDEX idx_movimiento_lote ON movimiento(lote_id);
CREATE INDEX idx_movimiento_tipo ON movimiento(tipo_movimiento, motivo_movimiento);
CREATE INDEX idx_movimiento_fecha ON movimiento(fecha_y_hora_creacion DESC);

CREATE INDEX idx_analisis_lote ON analisis(lote_id);
CREATE INDEX idx_analisis_numero ON analisis(numero_analisis);

CREATE INDEX idx_audit_acceso_user ON auditoria_acceso(user_id);
CREATE INDEX idx_audit_acceso_timestamp ON auditoria_acceso(timestamp DESC);
```

---

## 3. Stack Tecnológico Detallado

### 3.1 Backend

| Componente | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 17 LTS | Lenguaje base |
| **Spring Boot** | 3.4.1 | Framework aplicación |
| **Spring Security** | 6.x | Autenticación/autorización |
| **Spring Data JPA** | 3.x | ORM y repositorios |
| **Hibernate** | 6.x | Implementación JPA |
| **Thymeleaf** | 3.x | Template engine (SSR) |
| **Flyway** | 9.x | Migraciones de BD |
| **Lombok** | 1.18.x | Reducción de boilerplate |
| **Jakarta Validation** | 3.x | Validación de beans |
| **Jackson** | 2.x | JSON serialization |

### 3.2 Base de Datos

| Componente | Versión | Detalles |
|------------|---------|----------|
| **PostgreSQL** | 17 | BD principal |
| **pg_dump / pg_dumpall** | 17 | Backup utilities |
| **Encoding** | UTF-8 | Soporte internacional |
| **Timezone** | UTC | Timestamps consistentes |

**Configuración Hibernate:**
```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 50
        jdbc:
          batch_size: 50
          time_zone: UTC
        order_inserts: true
        order_updates: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### 3.3 Frontend

| Tecnología | Versión | Uso |
|------------|---------|-----|
| **HTML5** | - | Estructura |
| **Thymeleaf** | 3.x | Templating server-side |
| **Bootstrap** | 4.x | CSS framework |
| **jQuery** | 3.x | JavaScript utilities |
| **Font Awesome** | 5.x | Iconos |

**Nota:** No hay SPA (Single Page Application). Todo es server-side rendering.

### 3.4 DevOps e Infraestructura

| Componente | Versión | Propósito |
|------------|---------|-----------|
| **Docker** | 24.x | Containerización |
| **Docker Compose** | 2.x | Orquestación local |
| **Git** | 2.x | Control de versiones |
| **Gradle** (o Maven) | 8.x | Build tool |

**Docker Compose Setup:**
```yaml
services:
  postgres-db:
    image: postgres:17
    ports: ["5432:5432"]
    volumes:
      - db_data:/var/lib/postgresql/data
      - ./backups:/backups
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: root  # ⚠️ Cambiar en producción
      POSTGRES_DB: conitrack

  spring-app:
    build: .
    ports: ["8080:8080"]
    environment:
      SPRING_PROFILES_ACTIVE: DEV
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-db:5432/conitrack
    depends_on:
      - postgres-db
```

---

## 4. Flujos de Datos Críticos

### 4.1 Flujo de Ingreso de Compra

```
┌──────────────┐
│   Usuario    │
│  (Analista)  │
└──────┬───────┘
       │ 1. Accede a form
       ↓
┌─────────────────────────┐
│ AltaIngresoCompraController │
└──────────┬──────────────┘
           │ 2. Valida DTO
           ↓
┌─────────────────────────┐
│ AltaIngresoCompraService│
└──────────┬──────────────┘
           │ 3. Validaciones de negocio
           │    (fechas, cantidades, bultos)
           ↓
┌─────────────────────────┐
│   LoteRepository.save() │
│   MovimientoRepository  │
└──────────┬──────────────┘
           │ 4. Persiste
           ↓
┌─────────────────────────┐
│   PostgreSQL            │
│   - lote                │
│   - movimiento          │
│   - bultos (si aplica)  │
└──────────┬──────────────┘
           │ 5. Commit
           ↓
┌─────────────────────────┐
│ AuditorAccessLogger     │
│ (registra acceso)       │
└─────────────────────────┘
```

### 4.2 Flujo de Modificación de Análisis (Con Audit Trail Futuro)

```
┌──────────────┐
│   Usuario    │
│  (Analista)  │
└──────┬───────┘
       │ 1. Modifica análisis
       │ 2. Ingresa MOTIVO (obligatorio)
       ↓
┌─────────────────────────────┐
│ ModifResultadoAnalisisController │
└──────────┬──────────────────┘
           │ 3. Valida motivo (min 20 chars)
           ↓
┌─────────────────────────────┐
│ @Auditable Aspect (AOP)     │
│ - Captura estado ANTES      │
└──────────┬──────────────────┘
           │
           ↓
┌─────────────────────────────┐
│ ModifResultadoAnalisisService│
│ - Modifica análisis         │
│ - Actualiza lote            │
└──────────┬──────────────────┘
           │
           ↓
┌─────────────────────────────┐
│ @Auditable Aspect (AOP)     │
│ - Captura estado DESPUÉS    │
│ - Compara old/new values    │
└──────────┬──────────────────┘
           │ 4. Para cada cambio
           ↓
┌─────────────────────────────┐
│ AuditTrailService           │
│ - Registra en audit_cambios │
│ - Incluye motivo            │
└──────────┬──────────────────┘
           │ 5. Persiste
           ↓
┌─────────────────────────────┐
│   PostgreSQL                │
│   - analisis (updated)      │
│   - lote (updated)          │
│   - auditoria_cambios (NEW) │
└─────────────────────────────┘
```

### 4.3 Flujo de Liberación de Lote (Con Firma Electrónica Futura)

```
┌──────────────┐
│   Usuario    │
│ (Pers. Calif)│
└──────┬───────┘
       │ 1. Selecciona lote APROBADO
       │ 2. Clic "Liberar"
       ↓
┌─────────────────────────────┐
│ ModifLiberacionVentasController│
└──────────┬──────────────────┘
           │ 3. Verifica requisitos:
           │    - Lote APROBADO
           │    - Análisis completos
           │    - Usuario autorizado
           ↓
┌─────────────────────────────┐
│ Modal de Firma Electrónica  │
│ - Re-ingreso password       │
│ - Ingreso PIN (2FA)         │
│ - Comentarios opcionales    │
└──────────┬──────────────────┘
           │ 4. Submit firma
           ↓
┌─────────────────────────────┐
│ FirmaElectronicaController  │
└──────────┬──────────────────┘
           │ 5. Valida credenciales
           ↓
┌─────────────────────────────┐
│ ElectronicSignatureService  │
│ - Verifica password + PIN   │
│ - Captura snapshot de lote  │
│ - Genera hash SHA-256       │
│ - Crea FirmaElectronica     │
└──────────┬──────────────────┘
           │ 6. Persiste
           ↓
┌─────────────────────────────┐
│ ModifLiberacionVentasService│
│ - Marca lote como LIBERADO  │
│ - Vincula firma             │
└──────────┬──────────────────┘
           │ 7. Commit
           ↓
┌─────────────────────────────┐
│   PostgreSQL                │
│   - lote (estado=LIBERADO)  │
│   - firmas_electronicas     │
│   - auditoria_cambios       │
└─────────────────────────────┘
```

---

## 5. Configuraciones Importantes

### 5.1 application.yml (Producción)

```yaml
spring:
  application:
    name: conitrack
    version: 1.0  # Actualizar en releases

  datasource:
    url: jdbc:postgresql://${PROD_DB_HOST}:${PROD_DB_PORT}/${PROD_DB_NAME}
    username: ${PROD_DB_USERNAME}
    password: ${PROD_DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: none  # NUNCA "update" en producción
    show-sql: false
    open-in-view: false  # Evita lazy loading issues

server:
  port: ${PORT:8080}
  servlet:
    session:
      timeout: 1500s  # 25 minutos
  error:
    include-message: never
    include-binding-errors: never

logging:
  level:
    org.springframework.security: WARN
    com.mb.conitrack: INFO
  file:
    name: /var/log/conitrack/application.log
```

### 5.2 application-DEV.yml (Desarrollo)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/conitrack
    username: postgres
    password: root

  jpa:
    hibernate:
      ddl-auto: update  # OK en DEV
    show-sql: true

  devtools:
    livereload:
      enabled: true
    restart:
      enabled: true

server:
  servlet:
    session:
      timeout: 300s  # 5 minutos en DEV

logging:
  level:
    org.springframework.security: DEBUG
    com.mb.conitrack: DEBUG
```

### 5.3 SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login", "/logout").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## 6. Tests Existentes

### 6.1 Cobertura Actual

| Módulo | Tests | Cobertura |
|--------|-------|-----------|
| **Services CU** | 40+ tests | ~60% |
| **Validators** | 15+ tests | ~70% |
| **Controllers** | 20+ tests | ~50% |
| **Entities** | 10+ tests | ~40% |
| **Utils** | 5+ tests | ~80% |

### 6.2 Ejemplo de Test Existente

```java
// AltaIngresoCompraServiceTest.java
@SpringBootTest
@Transactional
class AltaIngresoCompraServiceTest {

    @Autowired
    private AltaIngresoCompraService service;

    @Test
    void debe_crearLoteEIngresoCompra_cuandoDatosValidos() {
        // Arrange
        AltaIngresoCompraDTO dto = new AltaIngresoCompraDTO();
        dto.setNumeroLote("LOT-2024-001");
        dto.setProductoId(1L);
        dto.setProveedorId(1L);
        dto.setCantidad(new BigDecimal("1000.00"));
        dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        dto.setFechaIngreso(LocalDate.now());

        // Act
        Lote resultado = service.altaIngresoCompra(dto);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getNumeroLote()).isEqualTo("LOT-2024-001");
        assertThat(resultado.getDictamen()).isEqualTo(DictamenEnum.CUARENTENA);
        assertThat(resultado.getCreadoPor()).isNotNull();
    }

    @Test
    void debe_lanzarExcepcion_cuandoNumeroLoteDuplicado() {
        // Arrange
        AltaIngresoCompraDTO dto = new AltaIngresoCompraDTO();
        dto.setNumeroLote("LOT-DUPLICADO");
        // ... otros campos

        service.altaIngresoCompra(dto);  // Primera creación

        // Act & Assert
        assertThatThrownBy(() -> service.altaIngresoCompra(dto))
            .isInstanceOf(DuplicateKeyException.class)
            .hasMessageContaining("Número de lote ya existe");
    }
}
```

### 6.3 Gaps en Testing Actual

- ❌ No hay tests de integración end-to-end
- ❌ No hay tests de performance
- ❌ No hay tests de seguridad (penetration testing)
- ❌ No hay tests de carga (múltiples usuarios concurrentes)
- ❌ **Tests existentes NO son tests de validación GxP**

---

## 7. Dependencias de Maven/Gradle

### 7.1 build.gradle (Ejemplo)

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.1'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.mb'
version = '1.0.0'
sourceCompatibility = '17'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity6'

    // Database
    runtimeOnly 'org.postgresql:postgresql'
    implementation 'org.flywaydb:flyway-core'

    // Utilities
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'

    // Development
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
}

tasks.named('test') {
    useJUnitPlatform()
}

// JaCoCo para cobertura de tests
apply plugin: 'jacoco'

jacoco {
    toolVersion = "0.8.11"
}

jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }

    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/config/**',
                '**/entity/**',
                '**/dto/**',
                '**/exception/**',
                '**/Application.class'
            ])
        }))
    }
}
```

---

## 8. Comandos Útiles

### 8.1 Gradle

```bash
# Compilar
./gradlew clean build -x test

# Ejecutar tests
./gradlew test

# Tests + coverage report
./gradlew clean test jacocoTestReport

# Ver reporte de cobertura (Windows)
start build/reports/jacoco/test/html/index.html

# Ejecutar solo un test
./gradlew test --tests AltaIngresoCompraServiceTest

# Ejecutar tests con log detallado
./gradlew test --info
```

### 8.2 Docker

```bash
# Build y levantar servicios
docker-compose up -d

# Ver logs
docker logs spring-app -f
docker logs postgres-db -f

# Acceder a PostgreSQL
docker exec -it postgres-db psql -U postgres -d conitrack

# Backup manual
docker exec postgres-db pg_dumpall -U postgres > backups/backup_manual_$(date +%Y%m%d).sql

# Detener servicios
docker-compose down

# Rebuild imagen
docker-compose build --no-cache
```

### 8.3 PostgreSQL

```sql
-- Verificar tablas
\dt

-- Contar registros
SELECT COUNT(*) FROM lote;
SELECT COUNT(*) FROM movimiento;
SELECT COUNT(*) FROM auditoria_acceso;

-- Ver últimos cambios
SELECT * FROM auditoria_acceso ORDER BY timestamp DESC LIMIT 10;

-- Verificar integridad de lotes
SELECT
    l.numero_lote,
    l.dictamen,
    COUNT(a.id) as total_analisis,
    l.activo
FROM lote l
LEFT JOIN analisis a ON a.lote_id = l.id
WHERE l.activo = TRUE
GROUP BY l.id;

-- Identificar lotes huérfanos (sin producto)
SELECT * FROM lote WHERE producto_id IS NULL;

-- Verificar usuarios y roles
SELECT u.username, r.name, r.hierarchy_level
FROM users u
JOIN roles r ON u.role_id = r.id
ORDER BY r.hierarchy_level DESC;
```

---

## 9. Endpoints API Principales

### 9.1 Casos de Uso (CU)

| Endpoint | Método | Descripción | Roles Autorizados |
|----------|--------|-------------|-------------------|
| `/cu/ingreso-compra` | GET | Form alta ingreso | ANALISTA_CC, GERENTE_CC, ADMIN |
| `/cu/ingreso-compra/guardar` | POST | Guardar ingreso | ANALISTA_CC, GERENTE_CC, ADMIN |
| `/cu/resultado-analisis` | GET | Form resultado | ANALISTA_CC, GERENTE_CC, ADMIN |
| `/cu/resultado-analisis/guardar` | POST | Guardar resultado | ANALISTA_CC, GERENTE_CC, ADMIN |
| `/cu/liberacion-ventas` | GET | Form liberación | DT, GERENTE_GC, ADMIN |
| `/cu/liberacion-ventas/guardar` | POST | Liberar lote | DT, GERENTE_GC, ADMIN |
| `/cu/reverso-movimiento` | GET | Form reverso | GERENTE+, ADMIN |
| `/cu/reverso-movimiento/ejecutar` | POST | Ejecutar reverso | GERENTE+, ADMIN |

### 9.2 Consultas

| Endpoint | Método | Descripción | Roles |
|----------|--------|-------------|-------|
| `/lote/lista` | GET | Listar lotes | Todos |
| `/lote/detalle/{id}` | GET | Detalle de lote | Todos |
| `/movimiento/lista` | GET | Listar movimientos | Todos |
| `/analisis/lista` | GET | Listar análisis | Todos |

### 9.3 Administración

| Endpoint | Método | Descripción | Roles |
|----------|--------|-------------|-------|
| `/admin/usuarios` | GET | Gestión usuarios | ADMIN |
| `/admin/productos` | GET | Gestión productos | ADMIN, GERENTE_GC |
| `/admin/proveedores` | GET | Gestión proveedores | ADMIN, GERENTE_GC |

---

## 10. Variables de Entorno

### 10.1 Producción

```bash
# Base de datos
PROD_DB_HOST=postgres-server.company.com
PROD_DB_PORT=5432
PROD_DB_NAME=conitrack_prod
PROD_DB_USERNAME=conitrack_user
PROD_DB_PASSWORD=<strong_password>  # ⚠️ Usar secrets manager

# Aplicación
SPRING_PROFILES_ACTIVE=prod
PORT=8080

# Logging
LOG_LEVEL=INFO
LOG_FILE=/var/log/conitrack/app.log

# Security
SESSION_TIMEOUT=1500  # 25 minutos en segundos
```

### 10.2 Desarrollo

```bash
SPRING_PROFILES_ACTIVE=DEV
LOG_LEVEL=DEBUG
```

---

## 11. Roadmap Técnico de Implementación ANMAT

### 11.1 Fase 1: Funcionalidades Críticas (Semanas 1-12)

**Nuevas Entidades:**
- `AuditoriaCambios.java`
- `GxpDataClassification.java`
- `FirmaElectronica.java`
- `UserPin.java`
- `PasswordHistory.java`
- `FailedLoginAttempt.java`

**Nuevos Servicios:**
- `AuditTrailService.java`
- `ElectronicSignatureService.java`
- `UserPinService.java`
- `PasswordPolicyService.java`
- `AccountLockoutService.java`

**Nuevos Aspects:**
- `AuditTrailAspect.java`

**Nuevos Controllers:**
- `FirmaElectronicaController.java`
- `AuditoriaController.java`

**Migraciones BD:**
- `V3__create_auditoria_cambios.sql`
- `V4__create_firma_electronica.sql`
- `V5__password_policies.sql`

### 11.2 Fase 2: Validación (Semanas 13-24)

**Documentación:**
- PMV-001, RA-001, ERU-001, EDS-001, MTR-001
- IQ-001, OQ-001, PQ-001
- VR-001

**Nuevos Tests:**
- Test suites de validación (100+ test cases)
- Tests de firma electrónica
- Tests de audit trail
- Tests de seguridad

---

## 12. Checklist de Preparación para Auditoría ANMAT

### 12.1 Sistema

- [ ] Audit trail completo implementado
- [ ] Firma electrónica funcional
- [ ] Políticas de contraseña robustas
- [ ] Backup automático configurado
- [ ] Gestión de cambios formal operativa
- [ ] Sistema validado (IQ/OQ/PQ aprobados)

### 12.2 Documentación

- [ ] PMV aprobado
- [ ] ERU aprobado
- [ ] Protocolos IQ/OQ/PQ ejecutados
- [ ] Reportes de validación firmados
- [ ] Todos los SOPs completos
- [ ] Manuales de usuario/admin
- [ ] Vendor qualifications completadas

### 12.3 Datos

- [ ] Datos de prueba en ambiente de validación
- [ ] Datos reales en producción con trazabilidad completa
- [ ] Audit trail sin gaps
- [ ] Backups verificados (restore exitoso probado)

### 12.4 Personal

- [ ] Todos los usuarios capacitados en BPF
- [ ] Certificados de training emitidos
- [ ] Roles y responsabilidades documentados
- [ ] Personas Cualificadas designadas formalmente

---

## Conclusión

Este documento técnico proporciona una visión completa del estado actual de Conitrack y el roadmap para cumplimiento ANMAT.

**Próximos pasos:**
1. Aprobar presupuesto y recursos
2. Iniciar Fase 1 de implementación
3. Contratar Validation Specialist
4. Ejecutar plan de 12 meses

---

[← Documentación Requerida](./ANMAT-06-DOCUMENTACION-REQUERIDA.md) | [Índice](./ANMAT-COMPLIANCE-INDEX.md)

**FIN DE DOCUMENTACIÓN ANMAT**
