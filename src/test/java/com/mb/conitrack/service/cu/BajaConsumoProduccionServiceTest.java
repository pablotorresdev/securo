package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
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
import java.util.Arrays;

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
@DisplayName("Tests - BajaConsumoProduccionService (CU7)")
class BajaConsumoProduccionServiceTest {

    @Autowired
    private BajaConsumoProduccionService service;

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

    private Lote loteTest;
    private User testUser;

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
        loteTest.setEstado(EstadoEnum.DISPONIBLE);
        loteTest.setDictamen(DictamenEnum.APROBADO);
        loteTest.setBultosTotales(2);
        loteTest.setActivo(true);
        loteTest = loteRepository.save(loteTest);

        Bulto bulto1 = new Bulto();
        bulto1.setLote(loteTest);
        bulto1.setNroBulto(1);
        bulto1.setCantidadInicial(new BigDecimal("60"));
        bulto1.setCantidadActual(new BigDecimal("60"));
        bulto1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto1.setEstado(EstadoEnum.DISPONIBLE);
        bulto1.setActivo(true);
        bultoRepository.save(bulto1);

        Bulto bulto2 = new Bulto();
        bulto2.setLote(loteTest);
        bulto2.setNroBulto(2);
        bulto2.setCantidadInicial(new BigDecimal("40"));
        bulto2.setCantidadActual(new BigDecimal("40"));
        bulto2.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto2.setEstado(EstadoEnum.DISPONIBLE);
        bulto2.setActivo(true);
        bultoRepository.save(bulto2);

        loteTest.getBultos().add(bulto1);
        loteTest.getBultos().add(bulto2);
    }

    @Test
    @DisplayName("test_bajaConsumoProduccionParcial_debe_actualizarStockYEstado")
    void test_bajaConsumoProduccionParcial_debe_actualizarStockYEstado() {
        // Given
        LoteDTO dto = new LoteDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setFechaEgreso(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setNroBultoList(Arrays.asList(1, 2));
        dto.setCantidadesBultos(Arrays.asList(new BigDecimal("20"), new BigDecimal("10")));
        dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO));

        // When
        LoteDTO resultado = service.bajaConsumoProduccion(dto);

        // Then
        assertThat(resultado).isNotNull();
        Lote loteBD = loteRepository.findById(loteTest.getId()).orElse(null);
        assertThat(loteBD.getCantidadActual()).isEqualByComparingTo(new BigDecimal("70"));
        assertThat(loteBD.getEstado()).isEqualTo(EstadoEnum.EN_USO);

        Bulto bulto1 = loteBD.getBultos().stream().filter(b -> b.getNroBulto() == 1).findFirst().orElse(null);
        assertThat(bulto1.getCantidadActual()).isEqualByComparingTo(new BigDecimal("40"));
        assertThat(bulto1.getEstado()).isEqualTo(EstadoEnum.EN_USO);

        assertThat(loteBD.getMovimientos()).hasSize(1);
        Movimiento mov = loteBD.getMovimientos().get(0);
        assertThat(mov.getTipoMovimiento()).isEqualTo(TipoMovimientoEnum.BAJA);
        assertThat(mov.getMotivo()).isEqualTo(MotivoEnum.CONSUMO_PRODUCCION);
    }

    @Test
    @DisplayName("test_bajaConsumoProduccionTotal_debe_marcarComoConsumido")
    void test_bajaConsumoProduccionTotal_debe_marcarComoConsumido() {
        // Given
        LoteDTO dto = new LoteDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setFechaEgreso(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setNroBultoList(Arrays.asList(1, 2));
        dto.setCantidadesBultos(Arrays.asList(new BigDecimal("60"), new BigDecimal("40")));
        dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO));

        // When
        service.bajaConsumoProduccion(dto);

        // Then
        Lote loteBD = loteRepository.findById(loteTest.getId()).orElse(null);
        assertThat(loteBD.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(loteBD.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);

        for (Bulto bulto : loteBD.getBultos()) {
            assertThat(bulto.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(bulto.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
        }
    }

    @Test
    @DisplayName("test_bajaConsumoProduccionTotalConAnalisisPendiente_debe_cancelarAnalisis")
    void test_bajaConsumoProduccionTotalConAnalisisPendiente_debe_cancelarAnalisis() {
        // Given
        Analisis analisis = new Analisis();
        analisis.setNroAnalisis("A-001");
        analisis.setLote(loteTest);
        analisis.setFechaRealizado(LocalDate.now());
        analisis.setDictamen(null);
        analisis.setActivo(true);
        analisis = analisisRepository.save(analisis);
        loteTest.getAnalisisList().add(analisis);

        LoteDTO dto = new LoteDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setFechaEgreso(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setNroBultoList(Arrays.asList(1, 2));
        dto.setCantidadesBultos(Arrays.asList(new BigDecimal("60"), new BigDecimal("40")));
        dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO));

        // When
        service.bajaConsumoProduccion(dto);

        // Then
        Analisis analisisBD = analisisRepository.findById(analisis.getId()).orElse(null);
        assertThat(analisisBD.getDictamen()).isEqualTo(DictamenEnum.CANCELADO);
    }

    @Test
    @DisplayName("test_bajaConsumoProduccionConLoteInexistente_debe_lanzarExcepcion")
    void test_bajaConsumoProduccionConLoteInexistente_debe_lanzarExcepcion() {
        // Given
        LoteDTO dto = new LoteDTO();
        dto.setCodigoLote("L-INEXISTENTE");

        // When & Then
        assertThatThrownBy(() -> service.bajaConsumoProduccion(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lote no existe");
    }

    @Test
    @DisplayName("test_validarConsumoProduccionInputValido_debe_retornarTrue")
    void test_validarConsumoProduccionInputValido_debe_retornarTrue() {
        // Given
        LoteDTO dto = new LoteDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setFechaEgreso(LocalDate.now());
        dto.setNroBultoList(Arrays.asList(1));
        dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10")));
        dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.KILOGRAMO));
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

        // When
        boolean resultado = service.validarConsumoProduccionInput(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
        assertThat(bindingResult.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("test_validarConsumoProduccionConLoteInexistente_debe_rechazar")
    void test_validarConsumoProduccionConLoteInexistente_debe_rechazar() {
        // Given
        LoteDTO dto = new LoteDTO();
        dto.setCodigoLote("L-INEXISTENTE");
        dto.setFechaEgreso(LocalDate.now());
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

        // When
        boolean resultado = service.validarConsumoProduccionInput(dto, bindingResult);

        // Then
        assertThat(resultado).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }

    @Test
    @DisplayName("test_validarConsumoProduccionConFechaAnterior_debe_rechazar")
    void test_validarConsumoProduccionConFechaAnterior_debe_rechazar() {
        // Given
        LoteDTO dto = new LoteDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setFechaEgreso(LocalDate.now().minusDays(10));
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

        // When
        boolean resultado = service.validarConsumoProduccionInput(dto, bindingResult);

        // Then
        assertThat(resultado).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }
}
