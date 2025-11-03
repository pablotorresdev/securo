package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.*;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.BultoRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.maestro.ProductoRepository;
import com.mb.conitrack.repository.maestro.ProveedorRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=false"
})
@Transactional
@DisplayName("Tests de Integracion - BajaDevolucionCompraService (CU4)")
class BajaDevolucionCompraServiceTest {

    @Autowired
    private BajaDevolucionCompraService service;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private BultoRepository bultoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private AnalisisRepository analisisRepository;

    @MockBean
    private com.mb.conitrack.service.SecurityContextService securityContextService;

    private User testUser;
    private Lote loteTest;

    @BeforeEach
    void setUp() {
        reset(securityContextService);

        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        testUser = new User("testuser", "password", adminRole);
        testUser.setId(1L);

        when(securityContextService.getCurrentUser()).thenReturn(testUser);

        Producto producto = new Producto();
        producto.setCodigoProducto("API-001");
        producto.setNombreGenerico("Paracetamol");
        producto.setTipoProducto(TipoProductoEnum.API);
        producto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Proveedor Test");
        proveedor.setPais("Argentina");
        proveedor.setCuit("20-12345678-9");
        proveedor.setDireccion("Calle Test 123");
        proveedor.setCiudad("Buenos Aires");
        proveedor.setActivo(true);
        proveedor = proveedorRepository.save(proveedor);

        loteTest = new Lote();
        loteTest.setCodigoLote("L-TEST-001");
        loteTest.setProducto(producto);
        loteTest.setProveedor(proveedor);
        loteTest.setFechaYHoraCreacion(OffsetDateTime.now());
        loteTest.setFechaIngreso(LocalDate.now());
        loteTest.setLoteProveedor("LP-001");
        loteTest.setCantidadInicial(new BigDecimal("100"));
        loteTest.setCantidadActual(new BigDecimal("100"));
        loteTest.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        loteTest.setEstado(EstadoEnum.NUEVO);
        loteTest.setDictamen(DictamenEnum.RECIBIDO);
        loteTest.setBultosTotales(2);
        loteTest.setActivo(true);
        loteTest = loteRepository.save(loteTest);

        Bulto bulto1 = new Bulto();
        bulto1.setLote(loteTest);
        bulto1.setNroBulto(1);
        bulto1.setCantidadInicial(new BigDecimal("60"));
        bulto1.setCantidadActual(new BigDecimal("60"));
        bulto1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto1.setEstado(EstadoEnum.NUEVO);
        bulto1.setActivo(true);
        bultoRepository.save(bulto1);

        Bulto bulto2 = new Bulto();
        bulto2.setLote(loteTest);
        bulto2.setNroBulto(2);
        bulto2.setCantidadInicial(new BigDecimal("40"));
        bulto2.setCantidadActual(new BigDecimal("40"));
        bulto2.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto2.setEstado(EstadoEnum.NUEVO);
        bulto2.setActivo(true);
        bultoRepository.save(bulto2);

        loteTest.getBultos().add(bulto1);
        loteTest.getBultos().add(bulto2);
    }

    @Nested
    @DisplayName("bajaBultosDevolucionCompra() - Flujo completo")
    class BajaBultosDevolucionCompra {

        @Test
        @DisplayName("test_devolucionCompraCompleta_debe_marcarTodoCeroYDevuelto")
        void test_devolucionCompraCompleta_debe_marcarTodoCeroYDevuelto() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote(loteTest.getCodigoLote());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setObservaciones("Devolucion por defecto");

            // When
            LoteDTO resultado = service.bajaBultosDevolucionCompra(dto);

            // Then
            assertThat(resultado).isNotNull();

            Lote loteBD = loteRepository.findById(loteTest.getId()).orElse(null);
            assertThat(loteBD).isNotNull();
            assertThat(loteBD.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(loteBD.getEstado()).isEqualTo(EstadoEnum.DEVUELTO);

            for (Bulto bulto : loteBD.getBultos()) {
                assertThat(bulto.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
                assertThat(bulto.getEstado()).isEqualTo(EstadoEnum.DEVUELTO);
            }

            assertThat(loteBD.getMovimientos()).hasSize(1);
            Movimiento movimiento = loteBD.getMovimientos().get(0);
            assertThat(movimiento.getTipoMovimiento()).isEqualTo(TipoMovimientoEnum.BAJA);
            assertThat(movimiento.getMotivo()).isEqualTo(MotivoEnum.DEVOLUCION_COMPRA);
            assertThat(movimiento.getCantidad()).isEqualByComparingTo(new BigDecimal("100"));
        }

        @Test
        @DisplayName("test_devolucionCompraConLoteInexistente_debe_lanzarExcepcion")
        void test_devolucionCompraConLoteInexistente_debe_lanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-INEXISTENTE");
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When & Then
            assertThatThrownBy(() -> service.bajaBultosDevolucionCompra(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("lote no existe");
        }

        @Test
        @DisplayName("test_devolucionCompraConAnalisisPendiente_debe_cancelarAnalisis")
        void test_devolucionCompraConAnalisisPendiente_debe_cancelarAnalisis() {
            // Given - Crear análisis en curso (sin dictamen)
            Analisis analisis = new Analisis();
            analisis.setNroAnalisis("A-001");
            analisis.setLote(loteTest);
            analisis.setFechaRealizado(LocalDate.now());
            analisis.setDictamen(null); // Sin dictamen = en curso
            analisis.setActivo(true);
            analisis = analisisRepository.save(analisis);
            loteTest.getAnalisisList().add(analisis);

            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote(loteTest.getCodigoLote());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            service.bajaBultosDevolucionCompra(dto);

            // Then
            Analisis analisisBD = analisisRepository.findById(analisis.getId()).orElse(null);
            assertThat(analisisBD).isNotNull();
            assertThat(analisisBD.getDictamen()).isEqualTo(DictamenEnum.CANCELADO);
        }

        @Test
        @DisplayName("test_devolucionCompraGuardaDictamenInicial")
        void test_devolucionCompraGuardaDictamenInicial() {
            // Given
            loteTest.setDictamen(DictamenEnum.CUARENTENA);
            loteRepository.save(loteTest);

            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote(loteTest.getCodigoLote());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            service.bajaBultosDevolucionCompra(dto);

            // Then
            Lote loteBD = loteRepository.findById(loteTest.getId()).orElse(null);
            Movimiento movimiento = loteBD.getMovimientos().get(0);
            assertThat(movimiento.getDictamenInicial()).isEqualTo(DictamenEnum.CUARENTENA);
        }
    }

    @Nested
    @DisplayName("validarDevolucionCompraInput() - Validaciones")
    class ValidarDevolucionCompraInput {

        @Test
        @DisplayName("test_validarInputValido_debe_retornarTrue")
        void test_validarInputValido_debe_retornarTrue() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote(loteTest.getCodigoLote());
            dto.setFechaMovimiento(LocalDate.now());
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // When
            boolean resultado = service.validarDevolucionCompraInput(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue();
            assertThat(bindingResult.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("test_validarConErroresPrevios_debe_retornarFalse")
        void test_validarConErroresPrevios_debe_retornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");
            bindingResult.reject("error.global", "Error previo");

            // When
            boolean resultado = service.validarDevolucionCompraInput(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("test_validarConLoteInexistente_debe_rechazar")
        void test_validarConLoteInexistente_debe_rechazar() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-INEXISTENTE");
            dto.setFechaMovimiento(LocalDate.now());
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // When
            boolean resultado = service.validarDevolucionCompraInput(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("codigoLote")).isNotNull();
        }

        @Test
        @DisplayName("test_validarConFechaAnteriorAIngreso_debe_rechazar")
        void test_validarConFechaAnteriorAIngreso_debe_rechazar() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote(loteTest.getCodigoLote());
            dto.setFechaMovimiento(LocalDate.now().minusDays(10));
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // When
            boolean resultado = service.validarDevolucionCompraInput(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("fechaMovimiento")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Casos edge y limites")
    class CasosEdge {

        @Test
        @DisplayName("test_devolucionCompraSinBultos_debe_procesarCorrectamente")
        void test_devolucionCompraSinBultos_debe_procesarCorrectamente() {
            // Given - Lote sin bultos
            Lote loteSinBultos = new Lote();
            loteSinBultos.setCodigoLote("L-SIN-BULTOS");
            loteSinBultos.setProducto(loteTest.getProducto());
            loteSinBultos.setProveedor(loteTest.getProveedor());
            loteSinBultos.setFechaYHoraCreacion(OffsetDateTime.now());
            loteSinBultos.setFechaIngreso(LocalDate.now());
            loteSinBultos.setLoteProveedor("LP-002");
            loteSinBultos.setCantidadInicial(BigDecimal.ZERO);
            loteSinBultos.setCantidadActual(BigDecimal.ZERO);
            loteSinBultos.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            loteSinBultos.setEstado(EstadoEnum.NUEVO);
            loteSinBultos.setDictamen(DictamenEnum.RECIBIDO);
            loteSinBultos.setBultosTotales(0);
            loteSinBultos.setActivo(true);
            loteSinBultos = loteRepository.save(loteSinBultos);

            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote(loteSinBultos.getCodigoLote());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            LoteDTO resultado = service.bajaBultosDevolucionCompra(dto);

            // Then
            assertThat(resultado).isNotNull();
            Lote loteBD = loteRepository.findById(loteSinBultos.getId()).orElse(null);
            assertThat(loteBD).isNotNull();
            assertThat(loteBD.getEstado()).isEqualTo(EstadoEnum.DEVUELTO);
        }

        @Test
        @DisplayName("test_devolucionCompraConAnalisisAprobado_no_debe_cancelarlo")
        void test_devolucionCompraConAnalisisAprobado_no_debe_cancelarlo() {
            // Given - Crear análisis aprobado
            Analisis analisis = new Analisis();
            analisis.setNroAnalisis("A-002");
            analisis.setLote(loteTest);
            analisis.setFechaRealizado(LocalDate.now());
            analisis.setDictamen(DictamenEnum.APROBADO);
            analisis.setActivo(true);
            analisis = analisisRepository.save(analisis);
            loteTest.getAnalisisList().add(analisis);

            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote(loteTest.getCodigoLote());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            service.bajaBultosDevolucionCompra(dto);

            // Then
            Analisis analisisBD = analisisRepository.findById(analisis.getId()).orElse(null);
            assertThat(analisisBD).isNotNull();
            assertThat(analisisBD.getDictamen()).isEqualTo(DictamenEnum.APROBADO);
        }
    }
}
