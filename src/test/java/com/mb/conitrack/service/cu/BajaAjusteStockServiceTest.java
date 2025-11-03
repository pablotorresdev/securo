package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.*;
import com.mb.conitrack.repository.*;
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
import static org.mockito.Mockito.when;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class BajaAjusteStockServiceTest {

    @Autowired private BajaAjusteStockService service;
    @Autowired private LoteRepository loteRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private ProveedorRepository proveedorRepository;
    @Autowired private BultoRepository bultoRepository;
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

        Bulto bulto = new Bulto();
        bulto.setLote(loteTest);
        bulto.setNroBulto(1);
        bulto.setCantidadInicial(new BigDecimal("100"));
        bulto.setCantidadActual(new BigDecimal("100"));
        bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto.setEstado(EstadoEnum.DISPONIBLE);
        bulto.setActivo(true);
        bultoRepository.save(bulto);
        loteTest.getBultos().add(bulto);
    }

    @Test
    void test_bajaAjusteStock_debe_descontarCantidadYActualizarEstado() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroBulto("1");
        dto.setCantidad(new BigDecimal("10"));
        dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setObservaciones("Ajuste por inventario");

        // When
        LoteDTO resultado = service.bajaAjusteStock(dto);

        // Then
        assertThat(resultado).isNotNull();
        Lote loteBD = loteRepository.findById(loteTest.getId()).orElse(null);
        assertThat(loteBD.getCantidadActual()).isEqualByComparingTo(new BigDecimal("90"));
        assertThat(loteBD.getEstado()).isEqualTo(EstadoEnum.EN_USO);
    }

    @Test
    void test_bajaAjusteTotalConAnalisisPendiente_debe_cancelarAnalisis() {
        // Given
        Analisis analisis = new Analisis();
        analisis.setNroAnalisis("A-001");
        analisis.setLote(loteTest);
        analisis.setFechaRealizado(LocalDate.now());
        analisis.setDictamen(null);
        analisis.setActivo(true);
        analisisRepository.save(analisis);

        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroBulto("1");
        dto.setCantidad(new BigDecimal("100"));
        dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());

        // When
        service.bajaAjusteStock(dto);

        // Then
        Analisis analisisBD = analisisRepository.findByNroAnalisisAndActivoTrue("A-001");
        assertThat(analisisBD.getDictamen()).isEqualTo(DictamenEnum.CANCELADO);
    }

    @Test
    void test_validarAjusteStockInputValido_debe_retornarTrue() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroBulto("1");
        dto.setCantidad(new BigDecimal("10"));
        dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        dto.setFechaMovimiento(LocalDate.now());
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "dto");

        // When
        boolean resultado = service.validarAjusteStockInput(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
    }
}
