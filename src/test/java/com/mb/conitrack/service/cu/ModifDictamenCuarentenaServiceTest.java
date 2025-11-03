package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.*;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.LoteRepository;
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
@DisplayName("Tests - ModifDictamenCuarentenaService (CU2)")
class ModifDictamenCuarentenaServiceTest {

    @Autowired
    private ModifDictamenCuarentenaService service;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

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
        loteTest.setEstado(EstadoEnum.NUEVO);
        loteTest.setDictamen(DictamenEnum.RECIBIDO);
        loteTest.setBultosTotales(1);
        loteTest.setActivo(true);
        loteTest = loteRepository.save(loteTest);
    }

    @Test
    @DisplayName("test_persistirDictamenCuarentenaConNuevoAnalisis_debe_crearAnalisisYCambiarDictamen")
    void test_persistirDictamenCuarentenaConNuevoAnalisis_debe_crearAnalisisYCambiarDictamen() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-001");
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setObservaciones("Test cuarentena");

        // When
        LoteDTO resultado = service.persistirDictamenCuarentena(dto);

        // Then
        assertThat(resultado).isNotNull();
        Lote loteBD = loteRepository.findById(loteTest.getId()).orElse(null);
        assertThat(loteBD.getDictamen()).isEqualTo(DictamenEnum.CUARENTENA);

        assertThat(loteBD.getAnalisisList()).hasSize(1);
        Analisis analisis = loteBD.getAnalisisList().get(0);
        assertThat(analisis.getNroAnalisis()).isEqualTo("A-001");

        assertThat(loteBD.getMovimientos()).hasSize(1);
        Movimiento mov = loteBD.getMovimientos().get(0);
        assertThat(mov.getTipoMovimiento()).isEqualTo(TipoMovimientoEnum.MODIFICACION);
        assertThat(mov.getMotivo()).isEqualTo(MotivoEnum.ANALISIS);
        assertThat(mov.getDictamenInicial()).isEqualTo(DictamenEnum.RECIBIDO);
        assertThat(mov.getDictamenFinal()).isEqualTo(DictamenEnum.CUARENTENA);
    }

    @Test
    @DisplayName("test_persistirDictamenCuarentenaConAnalisisExistente_debe_usarAnalisisExistente")
    void test_persistirDictamenCuarentenaConAnalisisExistente_debe_usarAnalisisExistente() {
        // Given - Crear análisis existente
        Analisis analisisExistente = new Analisis();
        analisisExistente.setNroAnalisis("A-002");
        analisisExistente.setLote(loteTest);
        analisisExistente.setFechaRealizado(LocalDate.now());
        analisisExistente.setActivo(true);
        analisisRepository.save(analisisExistente);

        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-002");
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());

        // When
        service.persistirDictamenCuarentena(dto);

        // Then
        Lote loteBD = loteRepository.findById(loteTest.getId()).orElse(null);
        assertThat(loteBD.getDictamen()).isEqualTo(DictamenEnum.CUARENTENA);
        // No debe crear nuevo análisis
        long count = analisisRepository.findAll().stream().filter(a -> a.getNroAnalisis().equals("A-002")).count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("test_persistirDictamenCuarentenaConLoteInexistente_debe_lanzarExcepcion")
    void test_persistirDictamenCuarentenaConLoteInexistente_debe_lanzarExcepcion() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("L-INEXISTENTE");
        dto.setNroAnalisis("A-003");

        // When & Then
        assertThatThrownBy(() -> service.persistirDictamenCuarentena(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lote no existe");
    }

    @Test
    @DisplayName("test_validarDictamenCuarentenaInputValido_debe_retornarTrue")
    void test_validarDictamenCuarentenaInputValido_debe_retornarTrue() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-004");
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");

        // When
        boolean resultado = service.validarDictamenCuarentenaInput(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
        assertThat(bindingResult.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("test_validarDictamenCuarentenaSinNroAnalisis_debe_rechazar")
    void test_validarDictamenCuarentenaSinNroAnalisis_debe_rechazar() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("");
        dto.setFechaMovimiento(LocalDate.now());
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");

        // When
        boolean resultado = service.validarDictamenCuarentenaInput(dto, bindingResult);

        // Then
        assertThat(resultado).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }

    @Test
    @DisplayName("test_validarDictamenCuarentenaConNroAnalisisDuplicado_debe_rechazar")
    void test_validarDictamenCuarentenaConNroAnalisisDuplicado_debe_rechazar() {
        // Given - Crear análisis con dictamen
        Analisis analisisExistente = new Analisis();
        analisisExistente.setNroAnalisis("A-005");
        analisisExistente.setLote(loteTest);
        analisisExistente.setFechaRealizado(LocalDate.now());
        analisisExistente.setDictamen(DictamenEnum.APROBADO);
        analisisExistente.setActivo(true);
        analisisRepository.save(analisisExistente);

        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-005");
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");

        // When
        boolean resultado = service.validarDictamenCuarentenaInput(dto, bindingResult);

        // Then
        assertThat(resultado).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }
}
