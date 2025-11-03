# Tests de Services CU - Conitrack

## Resumen

Este directorio contiene tests de integracion para todos los services del paquete `com.mb.conitrack.service.cu`.

**Estado actual:** 11 de 15 services tienen tests completos (73% completado)

## Archivos de Test Generados

### Tests Completos (11)

1. **AltaIngresoCompraServiceTest.java** (existente) - CU1
2. **AltaIngresoProduccionServiceTest.java** - CU20
3. **BajaDevolucionCompraServiceTest.java** - CU4
4. **BajaConsumoProduccionServiceTest.java** - CU7
5. **BajaVentaProductoServiceTest.java** - CU22
6. **BajaAjusteStockServiceTest.java** - CU25
7. **ModifDictamenCuarentenaServiceTest.java** - CU2
8. **ModifResultadoAnalisisServiceTest.java** - CU5/6
9. **ModifAnulacionAnalisisServiceTest.java** - CU11
10. **ModifReanalisisLoteServiceTest.java** - CU8
11. **ModifLiberacionVentasServiceTest.java** - CU21
12. **ModifTrazadoLoteServiceTest.java** - CU28
13. **FechaValidatorServiceTest.java** (existente)

### Tests Pendientes (4)

- **AltaDevolucionVentaServiceTest** - CU23 (COMPLEJO - Maneja trazas y lotes derivados)
- **BajaMuestreoBultoServiceTest** - CU3 (COMPLEJO - Muestreo trazable)
- **ModifRetiroMercadoServiceTest** - CU24 (MUY COMPLEJO - RECALL combinado)
- **ModifReversoMovimientoServiceTest** - CU26 (EXTREMADAMENTE COMPLEJO - Reversa todos los CU)

## Ejecutar Tests

### Ejecutar todos los tests de CU

```bash
mvn test -Dtest=com.mb.conitrack.service.cu.*Test
```

### Ejecutar un test especifico

```bash
mvn test -Dtest=AltaIngresoProduccionServiceTest
```

### Ejecutar con reporte de coverage

```bash
mvn clean test jacoco:report
```

Luego abrir: `target/site/jacoco/index.html`

### Ejecutar solo tests rapidos (sin integracion)

```bash
mvn test -Dtest=*ServiceTest -DexcludedGroups=integration
```

## Estructura de Tests

Todos los tests siguen el mismo patron:

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never"
})
@Transactional
@DisplayName("Tests - NombreService (CU##)")
class NombreServiceTest {

    @Autowired private NombreService service;
    @Autowired private LoteRepository loteRepository;
    // ... mas repositories

    @MockBean
    private SecurityContextService securityContextService;

    @BeforeEach
    void setUp() {
        // Setup de datos de prueba
    }

    @Test
    @DisplayName("test_metodo_cuandoCondicion_deberiaResultado")
    void test_metodo_cuandoCondicion_deberiaResultado() {
        // Given - Preparar datos
        // When - Ejecutar
        // Then - Verificar
    }
}
```

## Caracteristicas de los Tests

### Tecnologias Utilizadas

- **JUnit 5** - Framework de testing
- **Mockito** - Mocking de dependencias
- **AssertJ** - Assertions fluidas y legibles
- **H2 Database** - Base de datos en memoria para tests
- **Spring Boot Test** - Contexto de Spring para tests de integracion

### Patrones Aplicados

1. **Given-When-Then** - Estructura clara de cada test
2. **@Transactional** - Rollback automatico despues de cada test
3. **@MockBean** - MockBean para SecurityContextService
4. **@DisplayName** - Nombres descriptivos para documentacion
5. **Nested Tests** - Agrupacion logica de tests relacionados (en algunos)

### Cobertura por Test

Cada test cubre:

- ✅ **Happy path** - Flujo exitoso principal
- ✅ **Edge cases** - Casos limite (cantidades grandes, pequeñas, cero)
- ✅ **Error handling** - Excepciones esperadas
- ✅ **Validaciones** - Todos los metodos de validacion
- ✅ **Integraciones** - Verificacion de persistencia en BD
- ✅ **Estados** - Transiciones de estado (NUEVO → EN_USO → CONSUMIDO)
- ✅ **Dictamenes** - Cambios de dictamen
- ✅ **Trazas** - Manejo de trazabilidad (cuando aplica)
- ✅ **Analisis** - Cancelacion automatica cuando corresponde

## Dependencias

Los tests requieren las siguientes dependencias en `pom.xml`:

```xml
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- AssertJ -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- H2 Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Notas Importantes

### Base de Datos H2

- Todos los tests usan H2 en memoria (`jdbc:h2:mem:testdb`)
- Cada ejecucion crea una BD limpia desde cero
- No afecta la base de datos de desarrollo o produccion
- El esquema se crea automaticamente desde las entidades JPA

### Transaccionalidad

- Cada test se ejecuta en una transaccion
- Al finalizar el test, se hace rollback automatico
- Los tests son completamente independientes entre si
- No hay efectos colaterales entre tests

### MockBean vs Mock

- **@MockBean**: Se usa para SecurityContextService (bean de Spring)
- **@Mock**: Se usaria para tests unitarios puros (sin Spring)
- Los tests actuales son de **integracion** (usan SpringBootTest)

### Performance

- Los tests de integracion son mas lentos que tests unitarios
- Tiempo estimado: ~30-60 segundos para todos los tests de CU
- Para feedback rapido, ejecutar tests individuales

## Troubleshooting

### Error: "No tests found"

```bash
# Verificar que Maven este compilando los tests
mvn clean test-compile

# Verificar estructura de directorios
ls src/test/java/com/mb/conitrack/service/cu/
```

### Error: "H2 database not found"

Verificar que H2 este en las dependencias de test en `pom.xml`

### Error: "Bean not found"

Los tests de integracion necesitan que Spring Boot arranque correctamente.
Verificar que `@SpringBootTest` este presente y que la aplicacion compile.

### Tests muy lentos

Para tests mas rapidos, considerar:
1. Usar tests unitarios con Mockito (sin SpringBootTest)
2. Usar `@DirtiesContext` solo cuando sea necesario
3. Reutilizar contexto de Spring entre tests

## Proximos Pasos

Para completar el 100% de coverage:

1. **Implementar AltaDevolucionVentaServiceTest**
   - Devolucion con trazas
   - Creacion de lotes derivados (_D_N)
   - Validacion de movimiento origen

2. **Implementar BajaMuestreoBultoServiceTest**
   - Muestreo trazable
   - Muestreo multi-bulto
   - Conversion de unidades

3. **Implementar ModifRetiroMercadoServiceTest**
   - Procesamiento de RECALL (Alta + Modificacion)
   - Manejo de trazas en RECALL
   - Creacion de lotes recall (_R_N)

4. **Implementar ModifReversoMovimientoServiceTest** (El mas complejo)
   - Tests unitarios con Mockito (sin SpringBootTest)
   - Un metodo de reverso por cada CU
   - Verificacion de marcado activo=false
   - Restauracion de estados previos

## Recursos

- **Documento de resumen completo:** `TEST_GENERATION_SUMMARY.md` (en raiz del proyecto)
- **Test de referencia completo:** `AltaIngresoCompraServiceTest.java`
- **Patron de tests:** Ver cualquiera de los tests generados
- **Documentacion JUnit 5:** https://junit.org/junit5/docs/current/user-guide/
- **Documentacion AssertJ:** https://assertj.github.io/doc/

## Contacto

Para preguntas sobre los tests, consultar:
- Documento `TEST_GENERATION_SUMMARY.md`
- Codigo existente en los tests generados
- Patrones establecidos en `AltaIngresoCompraServiceTest.java`

---

**Generado automaticamente** - Los tests fueron generados siguiendo patrones establecidos y best practices de testing en Spring Boot.
