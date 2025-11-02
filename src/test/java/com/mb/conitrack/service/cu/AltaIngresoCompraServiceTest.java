package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.BultoRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.maestro.ProductoRepository;
import com.mb.conitrack.repository.maestro.ProveedorRepository;
import com.mb.conitrack.repository.maestro.RoleRepository;
import com.mb.conitrack.repository.maestro.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static com.mb.conitrack.testdata.TestDataBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integraci�n para AltaIngresoCompraService (CU1).
 * Usa @SpringBootTest con @Transactional para rollback autom�tico.
 * Verifica persistencia real en BD y relaciones bidireccionales.
 *
 * Cobertura completa del flujo CU1:
 * - Alta de stock por compra
 * - Validaci�n de entrada completa
 * - Creaci�n de lote con bultos
 * - Creaci�n de movimiento ALTA/COMPRA
 * - Persistencia en BD
 * - Manejo de errores
 * - Casos edge
 */
@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=false"
})
@Transactional
@DisplayName("Tests de Integraci�n - AltaIngresoCompraService (CU1)")
class AltaIngresoCompraServiceTest {

    @Autowired
    private AltaIngresoCompraService service;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private BultoRepository bultoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @MockBean
    private com.mb.conitrack.service.SecurityContextService securityContextService;

    private Producto productoTest;
    private Proveedor proveedorTest;
    private Proveedor fabricanteTest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Reset the mock before each test
        reset(securityContextService);

        // Crear usuario de test para el mock
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        testUser = new User("testuser", "password", adminRole);
        testUser.setId(1L);

        // Mock the SecurityContextService to return the test user
        when(securityContextService.getCurrentUser()).thenReturn(testUser);

        // Crear datos de test en BD
        productoTest = new Producto();
        productoTest.setCodigoProducto("API-TEST-001");
        productoTest.setNombreGenerico("Paracetamol Test");
        productoTest.setTipoProducto(TipoProductoEnum.API);
        productoTest.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        productoTest.setActivo(true);
        productoTest = productoRepository.save(productoTest);

        proveedorTest = new Proveedor();
        proveedorTest.setRazonSocial("Proveedor Test");
        proveedorTest.setPais("Argentina");
        proveedorTest.setCuit("20-12345678-9");
        proveedorTest.setDireccion("Calle Test 123");
        proveedorTest.setCiudad("Buenos Aires");
        proveedorTest.setActivo(true);
        proveedorTest = proveedorRepository.save(proveedorTest);

        fabricanteTest = new Proveedor();
        fabricanteTest.setRazonSocial("Fabricante Test");
        fabricanteTest.setPais("Alemania");
        fabricanteTest.setCuit("30-98765432-1");
        fabricanteTest.setDireccion("Strasse Test 456");
        fabricanteTest.setCiudad("Berlin");
        fabricanteTest.setActivo(true);
        fabricanteTest = proveedorRepository.save(fabricanteTest);
    }

    @Nested
    @DisplayName("altaStockPorCompra() - Flujo completo")
    class AltaStockPorCompra {

        @Test
        @DisplayName("test_altaCompraConUnBulto_debe_crearLoteYMovimiento")
        void test_altaCompraConUnBulto_debe_crearLoteYMovimiento() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoTest.getId())
                    .withProveedorId(proveedorTest.getId())
                    .withFabricanteId(fabricanteTest.getId())
                    .withCantidadInicial(new BigDecimal("25.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(1)
                    .withLoteProveedor("LP-2025-001")
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            LoteDTO resultado = service.altaStockPorCompra(dto);

            // Then - Verificar DTO resultado
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigoLote()).isNotNull();
            assertThat(resultado.getCodigoLote()).startsWith("L-API-TEST-001-");

            // Verificar persistencia en BD
            List<Lote> lotes = loteRepository.findAll();
            Lote loteBD = lotes.stream()
                    .filter(l -> l.getCodigoLote().equals(resultado.getCodigoLote()))
                    .findFirst()
                    .orElse(null);
            assertThat(loteBD).isNotNull();
            assertThat(loteBD.getEstado()).isEqualTo(EstadoEnum.NUEVO);
            assertThat(loteBD.getDictamen()).isEqualTo(DictamenEnum.RECIBIDO);
            assertThat(loteBD.getActivo()).isTrue();
            assertThat(loteBD.getCantidadInicial()).isEqualByComparingTo(new BigDecimal("25.0"));
            assertThat(loteBD.getCantidadActual()).isEqualByComparingTo(new BigDecimal("25.0"));
            assertThat(loteBD.getUnidadMedida()).isEqualTo(UnidadMedidaEnum.KILOGRAMO);
            assertThat(loteBD.getProducto().getId()).isEqualTo(productoTest.getId());
            assertThat(loteBD.getProveedor().getId()).isEqualTo(proveedorTest.getId());
            assertThat(loteBD.getFabricante().getId()).isEqualTo(fabricanteTest.getId());
            assertThat(loteBD.getPaisOrigen()).isEqualTo("Alemania");

            // Verificar bultos
            assertThat(loteBD.getBultos()).hasSize(1);
            Bulto bulto = loteBD.getBultos().get(0);
            assertThat(bulto.getNroBulto()).isEqualTo(1);
            assertThat(bulto.getCantidadInicial()).isEqualByComparingTo(new BigDecimal("25.0"));
            assertThat(bulto.getCantidadActual()).isEqualByComparingTo(new BigDecimal("25.0"));
            assertThat(bulto.getUnidadMedida()).isEqualTo(UnidadMedidaEnum.KILOGRAMO);
            assertThat(bulto.getEstado()).isEqualTo(EstadoEnum.NUEVO);
            assertThat(bulto.getLote()).isEqualTo(loteBD);

            // Verificar movimiento
            assertThat(loteBD.getMovimientos()).hasSize(1);
            Movimiento movimiento = loteBD.getMovimientos().get(0);
            assertThat(movimiento.getTipoMovimiento()).isEqualTo(TipoMovimientoEnum.ALTA);
            assertThat(movimiento.getMotivo()).isEqualTo(MotivoEnum.COMPRA);
            assertThat(movimiento.getCantidad()).isEqualByComparingTo(new BigDecimal("25.0"));
            assertThat(movimiento.getUnidadMedida()).isEqualTo(UnidadMedidaEnum.KILOGRAMO);
            assertThat(movimiento.getCodigoMovimiento()).contains(resultado.getCodigoLote());
            assertThat(movimiento.getActivo()).isTrue();

            // Verificar DetalleMovimiento
            assertThat(movimiento.getDetalles()).hasSize(1);
            DetalleMovimiento detalle = movimiento.getDetalles().iterator().next();
            assertThat(detalle.getBulto()).isEqualTo(bulto);
            assertThat(detalle.getMovimiento()).isEqualTo(movimiento);
        }

        @Test
        @DisplayName("test_altaCompraConMultiplesBultos_debe_distribuirCantidades")
        void test_altaCompraConMultiplesBultos_debe_distribuirCantidades() {
            // Given - 3 bultos con cantidades diferentes
            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoTest.getId())
                    .withProveedorId(proveedorTest.getId())
                    .withCantidadInicial(new BigDecimal("30.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(3)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("10.0"),
                            new BigDecimal("12.0"),
                            new BigDecimal("8.0")
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO
                    ))
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            LoteDTO resultado = service.altaStockPorCompra(dto);

            // Then
            List<Lote> lotes = loteRepository.findAll();
            Lote loteBD = lotes.stream()
                    .filter(l -> l.getCodigoLote().equals(resultado.getCodigoLote()))
                    .findFirst()
                    .orElse(null);
            assertThat(loteBD).isNotNull();
            assertThat(loteBD.getBultos()).hasSize(3);

            // Verificar cantidades individuales
            assertThat(loteBD.getBultos().get(0).getCantidadInicial()).isEqualByComparingTo(new BigDecimal("10.0"));
            assertThat(loteBD.getBultos().get(1).getCantidadInicial()).isEqualByComparingTo(new BigDecimal("12.0"));
            assertThat(loteBD.getBultos().get(2).getCantidadInicial()).isEqualByComparingTo(new BigDecimal("8.0"));

            // Verificar numeraci�n
            assertThat(loteBD.getBultos().get(0).getNroBulto()).isEqualTo(1);
            assertThat(loteBD.getBultos().get(1).getNroBulto()).isEqualTo(2);
            assertThat(loteBD.getBultos().get(2).getNroBulto()).isEqualTo(3);

            // Verificar DetalleMovimiento para cada bulto
            Movimiento movimiento = loteBD.getMovimientos().get(0);
            assertThat(movimiento.getDetalles()).hasSize(3);
        }

        @Test
        @DisplayName("test_altaCompraSinFabricante_debe_usarProveedorComoPaisOrigen")
        void test_altaCompraSinFabricante_debe_usarProveedorComoPaisOrigen() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoTest.getId())
                    .withProveedorId(proveedorTest.getId())
                    .withFabricanteId(null)  // Sin fabricante
                    .withPaisOrigen("")
                    .withCantidadInicial(new BigDecimal("10.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(1)
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            LoteDTO resultado = service.altaStockPorCompra(dto);

            // Then
            List<Lote> lotes = loteRepository.findAll();
            Lote loteBD = lotes.stream()
                    .filter(l -> l.getCodigoLote().equals(resultado.getCodigoLote()))
                    .findFirst()
                    .orElse(null);
            assertThat(loteBD).isNotNull();
            assertThat(loteBD.getFabricante()).isNull();
            assertThat(loteBD.getPaisOrigen()).isEqualTo("Argentina");  // Del proveedor
        }

        @Test
        @DisplayName("test_altaCompraConProductoInexistente_debe_lanzarExcepcion")
        void test_altaCompraConProductoInexistente_debe_lanzarExcepcion() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withProductoId(99999L)  // ID inexistente
                    .withProveedorId(proveedorTest.getId())
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When & Then
            assertThatThrownBy(() -> service.altaStockPorCompra(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("producto no existe");
        }

        @Test
        @DisplayName("test_altaCompraConProveedorInexistente_debe_lanzarExcepcion")
        void test_altaCompraConProveedorInexistente_debe_lanzarExcepcion() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoTest.getId())
                    .withProveedorId(99999L)  // ID inexistente
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When & Then
            assertThatThrownBy(() -> service.altaStockPorCompra(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("proveedor no existe");
        }

        @Test
        @DisplayName("test_altaCompraConUnidadMedidaDiferente_debe_convertir")
        void test_altaCompraConUnidadMedidaDiferente_debe_convertir() {
            // Given - Total en kg pero bultos en g
            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoTest.getId())
                    .withProveedorId(proveedorTest.getId())
                    .withCantidadInicial(new BigDecimal("1.5"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(2)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("1.0"),
                            new BigDecimal("500")
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.GRAMO
                    ))
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            LoteDTO resultado = service.altaStockPorCompra(dto);

            // Then
            List<Lote> lotes = loteRepository.findAll();
            Lote loteBD = lotes.stream()
                    .filter(l -> l.getCodigoLote().equals(resultado.getCodigoLote()))
                    .findFirst()
                    .orElse(null);
            assertThat(loteBD).isNotNull();
            assertThat(loteBD.getBultos()).hasSize(2);
            assertThat(loteBD.getBultos().get(0).getUnidadMedida()).isEqualTo(UnidadMedidaEnum.KILOGRAMO);
            assertThat(loteBD.getBultos().get(1).getUnidadMedida()).isEqualTo(UnidadMedidaEnum.GRAMO);
            assertThat(loteBD.getBultos().get(1).getCantidadInicial()).isEqualByComparingTo(new BigDecimal("500"));
        }
    }

    @Nested
    @DisplayName("validarIngresoCompraInput() - Validaciones")
    class ValidarIngresoCompraInput {

        @Test
        @DisplayName("test_validarInputValido_debe_retornarTrue")
        void test_validarInputValido_debe_retornarTrue() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("25.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(1)
                    .build();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.validarIngresoCompraInput(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue();
            assertThat(bindingResult.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("test_validarConErroresPrevios_debe_retornarFalse")
        void test_validarConErroresPrevios_debe_retornarFalse() {
            // Given
            LoteDTO dto = unLoteDTO().build();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");
            bindingResult.reject("error.global", "Error previo");

            // When
            boolean resultado = service.validarIngresoCompraInput(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("test_validarCantidadNula_debe_rechazar")
        void test_validarCantidadNula_debe_rechazar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(null)
                    .build();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.validarIngresoCompraInput(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("cantidadInicial")).isNotNull();
        }

        @Test
        @DisplayName("test_validarFechasInvalidas_debe_rechazar")
        void test_validarFechasInvalidas_debe_rechazar() {
            // Given - Reanalisis posterior a vencimiento
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("10.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(1)
                    .withFechaReanalisisProveedor(LocalDate.now().plusYears(2))
                    .withFechaVencimientoProveedor(LocalDate.now().plusMonths(6))
                    .build();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.validarIngresoCompraInput(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("fechaReanalisisProveedor")).isNotNull();
        }

        @Test
        @DisplayName("test_validarBultosConSumaIncorrecta_debe_rechazar")
        void test_validarBultosConSumaIncorrecta_debe_rechazar() {
            // Given - Suma de bultos no coincide con total
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("30.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(2)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("10.0"),
                            new BigDecimal("15.0")  // Suma 25, no 30
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO
                    ))
                    .build();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.validarIngresoCompraInput(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("cantidadesBultos")).isNotNull();
        }

        @Test
        @DisplayName("test_validarUnidadConDecimales_debe_rechazar")
        void test_validarUnidadConDecimales_debe_rechazar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("10.5"))  // Decimal en UNIDAD
                    .withUnidadMedida(UnidadMedidaEnum.UNIDAD)
                    .withBultosTotales(5)
                    .build();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.validarIngresoCompraInput(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
        }
    }

    @Nested
    @DisplayName("Casos edge y l�mites")
    class CasosEdge {

        @Test
        @DisplayName("test_altaCompraCantidadMuyGrande_debe_procesar")
        void test_altaCompraCantidadMuyGrande_debe_procesar() {
            // Given - 1 tonelada
            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoTest.getId())
                    .withProveedorId(proveedorTest.getId())
                    .withCantidadInicial(new BigDecimal("1000.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(1)
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            LoteDTO resultado = service.altaStockPorCompra(dto);

            // Then
            assertThat(resultado).isNotNull();
            List<Lote> lotes = loteRepository.findAll();
            Lote loteBD = lotes.stream()
                    .filter(l -> l.getCodigoLote().equals(resultado.getCodigoLote()))
                    .findFirst()
                    .orElse(null);
            assertThat(loteBD).isNotNull();
            assertThat(loteBD.getCantidadActual()).isEqualByComparingTo(new BigDecimal("1000.0"));
        }

        @Test
        @DisplayName("test_altaCompraCantidadMuyPequena_debe_procesar")
        void test_altaCompraCantidadMuyPequena_debe_procesar() {
            // Given - 1 gramo
            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoTest.getId())
                    .withProveedorId(proveedorTest.getId())
                    .withCantidadInicial(new BigDecimal("1"))
                    .withUnidadMedida(UnidadMedidaEnum.GRAMO)
                    .withBultosTotales(1)
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            LoteDTO resultado = service.altaStockPorCompra(dto);

            // Then
            assertThat(resultado).isNotNull();
            List<Lote> lotes = loteRepository.findAll();
            Lote loteBD = lotes.stream()
                    .filter(l -> l.getCodigoLote().equals(resultado.getCodigoLote()))
                    .findFirst()
                    .orElse(null);
            assertThat(loteBD).isNotNull();
            assertThat(loteBD.getCantidadActual()).isEqualByComparingTo(new BigDecimal("1"));
        }
    }
}
