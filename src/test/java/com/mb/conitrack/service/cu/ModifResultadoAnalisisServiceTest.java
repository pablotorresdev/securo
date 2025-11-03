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
@DisplayName("Tests - ModifResultadoAnalisisService (CU5/6)")
class ModifResultadoAnalisisServiceTest {

    @Autowired
    private ModifResultadoAnalisisService service;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private AnalisisRepository analisisRepository;

    @Autowired
    private BultoRepository bultoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @MockBean
    private com.mb.conitrack.service.SecurityContextService securityContextService;

    private Lote loteTest;
    private Analisis analisisTest;
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
        loteTest.setEstado(EstadoEnum.NUEVO);
        loteTest.setDictamen(DictamenEnum.CUARENTENA);
        loteTest.setBultosTotales(1);
        loteTest.setActivo(true);
        loteTest = loteRepository.save(loteTest);

        Bulto bulto = new Bulto();
        bulto.setLote(loteTest);
        bulto.setNroBulto(1);
        bulto.setCantidadInicial(new BigDecimal("100"));
        bulto.setCantidadActual(new BigDecimal("100"));
        bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto.setEstado(EstadoEnum.NUEVO);
        bulto.setActivo(true);
        bultoRepository.save(bulto);

        analisisTest = new Analisis();
        analisisTest.setNroAnalisis("A-001");
        analisisTest.setLote(loteTest);
        analisisTest.setFechaRealizado(LocalDate.now());
        analisisTest.setDictamen(null);
        analisisTest.setActivo(true);
        analisisTest = analisisRepository.save(analisisTest);
        loteTest.getAnalisisList().add(analisisTest);

        // Crear movimiento de muestreo (prerequisito para CU5/6)
        Movimiento movMuestreo = new Movimiento();
        movMuestreo.setCodigoMovimiento("M-TEST-001");
        movMuestreo.setLote(loteTest);
        movMuestreo.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movMuestreo.setMotivo(MotivoEnum.MUESTREO);
        movMuestreo.setNroAnalisis("A-001");
        movMuestreo.setCantidad(new BigDecimal("1"));
        movMuestreo.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        movMuestreo.setFecha(LocalDate.now());
        movMuestreo.setActivo(true);
        movimientoRepository.save(movMuestreo);
    }

    @Test
    @DisplayName("test_persistirResultadoAnalisisAprobado_debe_actualizarAnalisisYDictamen")
    void test_persistirResultadoAnalisisAprobado_debe_actualizarAnalisisYDictamen() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-001");
        dto.setDictamenFinal(DictamenEnum.APROBADO);
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaReanalisis(LocalDate.now().plusMonths(6));
        dto.setFechaVencimiento(LocalDate.now().plusYears(2));
        dto.setTitulo(new BigDecimal("99.5"));
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setObservaciones("Aprobado");

        // When
        LoteDTO resultado = service.persistirResultadoAnalisis(dto);

        // Then
        assertThat(resultado).isNotNull();
        Lote loteBD = loteRepository.findById(loteTest.getId()).orElse(null);
        assertThat(loteBD.getDictamen()).isEqualTo(DictamenEnum.APROBADO);

        Analisis analisisBD = analisisRepository.findByNroAnalisisAndActivoTrue("A-001");
        assertThat(analisisBD).isNotNull();
        assertThat(analisisBD.getDictamen()).isEqualTo(DictamenEnum.APROBADO);
        assertThat(analisisBD.getFechaReanalisis()).isEqualTo(LocalDate.now().plusMonths(6));
        assertThat(analisisBD.getFechaVencimiento()).isEqualTo(LocalDate.now().plusYears(2));
        assertThat(analisisBD.getTitulo()).isEqualByComparingTo(new BigDecimal("99.5"));

        assertThat(loteBD.getMovimientos()).hasSize(1);
        Movimiento mov = loteBD.getMovimientos().get(0);
        assertThat(mov.getTipoMovimiento()).isEqualTo(TipoMovimientoEnum.MODIFICACION);
        assertThat(mov.getMotivo()).isEqualTo(MotivoEnum.RESULTADO_ANALISIS);
        assertThat(mov.getDictamenInicial()).isEqualTo(DictamenEnum.CUARENTENA);
        assertThat(mov.getDictamenFinal()).isEqualTo(DictamenEnum.APROBADO);
    }

    @Test
    @DisplayName("test_persistirResultadoAnalisisRechazado_debe_actualizarDictamen")
    void test_persistirResultadoAnalisisRechazado_debe_actualizarDictamen() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-001");
        dto.setDictamenFinal(DictamenEnum.RECHAZADO);
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setObservaciones("Rechazado por fuera de especificacion");

        // When
        LoteDTO resultado = service.persistirResultadoAnalisis(dto);

        // Then
        assertThat(resultado).isNotNull();
        Lote loteBD = loteRepository.findById(loteTest.getId()).orElse(null);
        assertThat(loteBD.getDictamen()).isEqualTo(DictamenEnum.RECHAZADO);

        Analisis analisisBD = analisisRepository.findByNroAnalisisAndActivoTrue("A-001");
        assertThat(analisisBD.getDictamen()).isEqualTo(DictamenEnum.RECHAZADO);
        assertThat(analisisBD.getFechaReanalisis()).isNull();
        assertThat(analisisBD.getFechaVencimiento()).isNull();
        assertThat(analisisBD.getTitulo()).isNull();
    }

    @Test
    @DisplayName("test_validarResultadoAnalisisInputValido_debe_retornarTrue")
    void test_validarResultadoAnalisisInputValido_debe_retornarTrue() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-001");
        dto.setDictamenFinal(DictamenEnum.APROBADO);
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaReanalisis(LocalDate.now().plusMonths(6));
        dto.setFechaVencimiento(LocalDate.now().plusYears(2));
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");

        // When
        boolean resultado = service.validarResultadoAnalisisInput(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
        assertThat(bindingResult.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("test_validarResultadoAnalisisSinNroAnalisis_debe_rechazar")
    void test_validarResultadoAnalisisSinNroAnalisis_debe_rechazar() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("");
        dto.setDictamenFinal(DictamenEnum.APROBADO);
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");

        // When
        boolean resultado = service.validarResultadoAnalisisInput(dto, bindingResult);

        // Then
        assertThat(resultado).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }

    @Test
    @DisplayName("test_validarResultadoAnalisisSinDictamenFinal_debe_rechazar")
    void test_validarResultadoAnalisisSinDictamenFinal_debe_rechazar() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-001");
        dto.setDictamenFinal(null);
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");

        // When
        boolean resultado = service.validarResultadoAnalisisInput(dto, bindingResult);

        // Then
        assertThat(resultado).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }

    @Test
    @DisplayName("test_validarResultadoAnalisisAprobadoSinFechas_debe_rechazar")
    void test_validarResultadoAnalisisAprobadoSinFechas_debe_rechazar() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-001");
        dto.setDictamenFinal(DictamenEnum.APROBADO);
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        dto.setFechaMovimiento(LocalDate.now());
        // Sin fechaReanalisis ni fechaVencimiento
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");

        // When
        boolean resultado = service.validarResultadoAnalisisInput(dto, bindingResult);

        // Then
        assertThat(resultado).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }

    @Test
    @DisplayName("test_validarResultadoAnalisisConReanalisisPosteriorAVencimiento_debe_rechazar")
    void test_validarResultadoAnalisisConReanalisisPosteriorAVencimiento_debe_rechazar() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-001");
        dto.setDictamenFinal(DictamenEnum.APROBADO);
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaReanalisis(LocalDate.now().plusYears(2));
        dto.setFechaVencimiento(LocalDate.now().plusMonths(6));
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");

        // When
        boolean resultado = service.validarResultadoAnalisisInput(dto, bindingResult);

        // Then
        assertThat(resultado).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }

    @Test
    @DisplayName("test_validarResultadoAnalisisSinMuestreo_debe_rechazar")
    void test_validarResultadoAnalisisSinMuestreo_debe_rechazar() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-999"); // Sin muestreo asociado
        dto.setDictamenFinal(DictamenEnum.APROBADO);
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaReanalisis(LocalDate.now().plusMonths(6));
        dto.setFechaVencimiento(LocalDate.now().plusYears(2));
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");

        // When
        boolean resultado = service.validarResultadoAnalisisInput(dto, bindingResult);

        // Then
        assertThat(resultado).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }
}
