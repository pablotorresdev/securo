package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.*;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.TrazaRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class ModifTrazadoLoteServiceTest {

    @Autowired private ModifTrazadoLoteService service;
    @Autowired private LoteRepository loteRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private ProveedorRepository proveedorRepository;
    @Autowired private TrazaRepository trazaRepository;
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
        producto.setCodigoProducto("UV-001");
        producto.setNombreGenerico("Unidad Venta");
        producto.setTipoProducto(TipoProductoEnum.UNIDAD_VENTA);
        producto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Proveedor");
        proveedor.setPais("Argentina");
        proveedor.setCuit("20-12345678-9");
        proveedor.setActivo(true);
        proveedor = proveedorRepository.save(proveedor);

        loteTest = new Lote();
        loteTest.setCodigoLote("L-UV-001");
        loteTest.setProducto(producto);
        loteTest.setProveedor(proveedor);
        loteTest.setFechaYHoraCreacion(OffsetDateTime.now());
        loteTest.setFechaIngreso(LocalDate.now());
        loteTest.setLoteProveedor("LP-001");
        loteTest.setCantidadInicial(new BigDecimal("100"));
        loteTest.setCantidadActual(new BigDecimal("100"));
        loteTest.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        loteTest.setEstado(EstadoEnum.NUEVO);
        loteTest.setDictamen(DictamenEnum.RECIBIDO);
        loteTest.setBultosTotales(1);
        loteTest.setTrazado(false);
        loteTest.setActivo(true);
        loteTest = loteRepository.save(loteTest);
    }

    @Test
    void test_persistirTrazadoLote_debe_crearTrazasYMarcarLoteComoTrazado() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setTrazaInicial(1L);
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setObservaciones("Trazado inicial");

        // When
        LoteDTO resultado = service.persistirTrazadoLote(dto);

        // Then
        assertThat(resultado).isNotNull();
        Lote loteBD = loteRepository.findById(loteTest.getId()).orElse(null);
        assertThat(loteBD.getTrazado()).isTrue();

        List<Traza> trazas = trazaRepository.findAll();
        assertThat(trazas).hasSize(100);
        assertThat(trazas.get(0).getNroTraza()).isEqualTo(1L);
        assertThat(trazas.get(99).getNroTraza()).isEqualTo(100L);
    }

    @Test
    void test_persistirTrazadoLoteConProductoNoUnidadVenta_debe_lanzarExcepcion() {
        // Given - Cambiar tipo producto
        Producto productoAPI = new Producto();
        productoAPI.setCodigoProducto("API-001");
        productoAPI.setNombreGenerico("API");
        productoAPI.setTipoProducto(TipoProductoEnum.API);
        productoAPI.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        productoAPI.setActivo(true);
        productoAPI = productoRepository.save(productoAPI);

        loteTest.setProducto(productoAPI);
        loteRepository.save(loteTest);

        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setTrazaInicial(1L);
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());

        // When & Then
        assertThatThrownBy(() -> service.persistirTrazadoLote(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("debe ser UNIDAD_VENTA para poder trazarse");
    }

    @Test
    void test_validarTrazadoLoteInputValido_debe_retornarTrue() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setTrazaInicial(1L);
        dto.setFechaMovimiento(LocalDate.now());
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "dto");

        // When
        boolean resultado = service.validarTrazadoLoteInput(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
    }

    @Test
    void test_validarTrazadoLoteSinTrazaInicial_debe_rechazar() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setTrazaInicial(null);
        dto.setFechaMovimiento(LocalDate.now());
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "dto");

        // When
        boolean resultado = service.validarTrazadoLoteInput(dto, bindingResult);

        // Then
        assertThat(resultado).isFalse();
        assertThat(bindingResult.hasErrors()).isTrue();
    }
}
