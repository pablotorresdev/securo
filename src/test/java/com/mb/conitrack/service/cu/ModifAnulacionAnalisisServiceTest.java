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
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.maestro.ProductoRepository;
import com.mb.conitrack.repository.maestro.ProveedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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
    "spring.sql.init.mode=never"
})
@Transactional
@DisplayName("Tests - ModifAnulacionAnalisisService (CU11)")
class ModifAnulacionAnalisisServiceTest {

    @Autowired private ModifAnulacionAnalisisService service;
    @Autowired private LoteRepository loteRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private ProveedorRepository proveedorRepository;
    @Autowired private AnalisisRepository analisisRepository;
    @Autowired private MovimientoRepository movimientoRepository;
    @MockBean private com.mb.conitrack.service.SecurityContextService securityContextService;

    private Lote loteTest;
    private Analisis analisisTest;

    @BeforeEach
    void setUp() {
        reset(securityContextService);
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        User testUser = new User("testuser", "password", adminRole);
        testUser.setId(1L);
        when(securityContextService.getCurrentUser()).thenReturn(testUser);

        Producto producto = new Producto();
        producto.setCodigoProducto("API-001");
        producto.setNombreGenerico("Test");
        producto.setTipoProducto(TipoProductoEnum.API);
        producto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Proveedor");
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
        loteTest.setBultosTotales(1);
        loteTest.setActivo(true);
        loteTest = loteRepository.save(loteTest);

        analisisTest = new Analisis();
        analisisTest.setNroAnalisis("A-001");
        analisisTest.setLote(loteTest);
        analisisTest.setFechaRealizado(LocalDate.now());
        analisisTest.setDictamen(DictamenEnum.APROBADO);
        analisisTest.setActivo(true);
        analisisTest = analisisRepository.save(analisisTest);
        loteTest.getAnalisisList().add(analisisTest);

        // Crear movimiento de analisis anterior
        Movimiento movAnalisis = new Movimiento();
        movAnalisis.setCodigoMovimiento("M-ANALISIS-001");
        movAnalisis.setLote(loteTest);
        movAnalisis.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
        movAnalisis.setMotivo(MotivoEnum.RESULTADO_ANALISIS);
        movAnalisis.setNroAnalisis("A-001");
        movAnalisis.setDictamenInicial(DictamenEnum.CUARENTENA);
        movAnalisis.setDictamenFinal(DictamenEnum.APROBADO);
        movAnalisis.setFecha(LocalDate.now());
        movAnalisis.setActivo(true);
        movimientoRepository.save(movAnalisis);
    }

    @Test
    @DisplayName("test_persistirAnulacionAnalisis_debe_anularYRevertirDictamen")
    void test_persistirAnulacionAnalisis_debe_anularYRevertirDictamen() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-001");
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setObservaciones("Anulacion por error");

        // When
        LoteDTO resultado = service.persistirAnulacionAnalisis(dto);

        // Then
        assertThat(resultado).isNotNull();
        Lote loteBD = loteRepository.findById(loteTest.getId()).orElse(null);
        assertThat(loteBD.getDictamen()).isEqualTo(DictamenEnum.CUARENTENA);

        Analisis analisisBD = analisisRepository.findByNroAnalisisAndActivoTrue("A-001");
        assertThat(analisisBD.getDictamen()).isEqualTo(DictamenEnum.ANULADO);
    }

    @Test
    @DisplayName("test_validarAnulacionAnalisisInputValido_debe_retornarTrue")
    void test_validarAnulacionAnalisisInputValido_debe_retornarTrue() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-001");
        dto.setFechaMovimiento(LocalDate.now());
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "movimientoDTO");

        // When
        boolean resultado = service.validarAnulacionAnalisisInput(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
        assertThat(bindingResult.hasErrors()).isFalse();
    }
}
