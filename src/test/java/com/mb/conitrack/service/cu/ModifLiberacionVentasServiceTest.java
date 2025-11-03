package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
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
import static org.mockito.Mockito.when;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class ModifLiberacionVentasServiceTest {

    @Autowired private ModifLiberacionVentasService service;
    @Autowired private LoteRepository loteRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private ProveedorRepository proveedorRepository;
    @Autowired private AnalisisRepository analisisRepository;
    @MockBean private com.mb.conitrack.service.SecurityContextService securityContextService;

    private Lote loteTest;

    @BeforeEach
    void setUp() {
        Role role = Role.fromEnum(RoleEnum.ADMIN);
        role.setId(1L);
        User user = new User("test", "pass", role);
        user.setId(1L);
        when(securityContextService.getCurrentUser()).thenReturn(user);

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
        loteTest.setCodigoLote("L-001");
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
    }

    @Test
    void test_persistirLiberacionProductoConUnAnalisisConFechaVencimiento_debe_liberarYActualizarFechaVencimiento() {
        // Given
        Analisis analisis = new Analisis();
        analisis.setNroAnalisis("A-001");
        analisis.setLote(loteTest);
        analisis.setFechaRealizado(LocalDate.now());
        analisis.setDictamen(DictamenEnum.APROBADO);
        analisis.setFechaVencimiento(LocalDate.now().plusYears(2));
        analisis.setActivo(true);
        analisisRepository.save(analisis);
        loteTest.getAnalisisList().add(analisis);

        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setObservaciones("Liberacion para venta");

        // When
        LoteDTO resultado = service.persistirLiberacionProducto(dto);

        // Then
        assertThat(resultado).isNotNull();
        Lote loteBD = loteRepository.findById(loteTest.getId()).orElse(null);
        assertThat(loteBD.getDictamen()).isEqualTo(DictamenEnum.LIBERADO);
        assertThat(loteBD.getFechaVencimientoProveedor()).isEqualTo(LocalDate.now().plusYears(2));
    }

    @Test
    void test_persistirLiberacionProductoSinAnalisisConFechaVencimiento_debe_lanzarExcepcion() {
        // Given - Sin analisis con fecha vencimiento
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());

        // When & Then
        assertThatThrownBy(() -> service.persistirLiberacionProducto(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("NO hay Analisis con fecha de vencimiento");
    }

    @Test
    void test_persistirLiberacionProductoConMultiplesAnalisisConFechaVencimiento_debe_lanzarExcepcion() {
        // Given - Dos analisis con fecha vencimiento
        Analisis analisis1 = new Analisis();
        analisis1.setNroAnalisis("A-001");
        analisis1.setLote(loteTest);
        analisis1.setFechaRealizado(LocalDate.now());
        analisis1.setDictamen(DictamenEnum.APROBADO);
        analisis1.setFechaVencimiento(LocalDate.now().plusYears(2));
        analisis1.setActivo(true);
        analisisRepository.save(analisis1);

        Analisis analisis2 = new Analisis();
        analisis2.setNroAnalisis("A-002");
        analisis2.setLote(loteTest);
        analisis2.setFechaRealizado(LocalDate.now());
        analisis2.setDictamen(DictamenEnum.APROBADO);
        analisis2.setFechaVencimiento(LocalDate.now().plusYears(3));
        analisis2.setActivo(true);
        analisisRepository.save(analisis2);

        loteTest.getAnalisisList().add(analisis1);
        loteTest.getAnalisisList().add(analisis2);

        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());

        // When & Then
        assertThatThrownBy(() -> service.persistirLiberacionProducto(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Hay más de un análisis activo con fecha de vencimiento");
    }

    @Test
    void test_validarLiberacionProductoInputValido_debe_retornarTrue() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setFechaMovimiento(LocalDate.now());
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "dto");

        // When
        boolean resultado = service.validarLiberacionProductoInput(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
    }
}
