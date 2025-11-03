package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Traza;
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
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class BajaVentaProductoServiceTest {

    @Autowired private BajaVentaProductoService service;
    @Autowired private LoteRepository loteRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private ProveedorRepository proveedorRepository;
    @Autowired private BultoRepository bultoRepository;
    @Autowired private TrazaRepository trazaRepository;
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
        loteTest.setEstado(EstadoEnum.DISPONIBLE);
        loteTest.setDictamen(DictamenEnum.LIBERADO);
        loteTest.setBultosTotales(1);
        loteTest.setTrazado(true);
        loteTest.setActivo(true);
        loteTest = loteRepository.save(loteTest);

        Bulto bulto = new Bulto();
        bulto.setLote(loteTest);
        bulto.setNroBulto(1);
        bulto.setCantidadInicial(new BigDecimal("100"));
        bulto.setCantidadActual(new BigDecimal("100"));
        bulto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        bulto.setEstado(EstadoEnum.DISPONIBLE);
        bulto.setActivo(true);
        bulto = bultoRepository.save(bulto);
        loteTest.getBultos().add(bulto);

        // Crear trazas
        for (long i = 1; i <= 100; i++) {
            Traza traza = new Traza();
            traza.setNroTraza(i);
            traza.setLote(loteTest);
            traza.setBulto(bulto);
            traza.setEstado(EstadoEnum.DISPONIBLE);
            traza.setActivo(true);
            trazaRepository.save(traza);
        }
    }

    @Test
    void test_bajaVentaProductoTrazado_debe_marcarTrazasComoVendido() {
        // Given
        LoteDTO dto = new LoteDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setFechaEgreso(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setNroBultoList(Arrays.asList(1));
        dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10")));

        // When
        LoteDTO resultado = service.bajaVentaProducto(dto);

        // Then
        assertThat(resultado).isNotNull();
        Lote loteBD = loteRepository.findById(loteTest.getId()).orElse(null);
        assertThat(loteBD.getCantidadActual()).isEqualByComparingTo(new BigDecimal("90"));
        assertThat(loteBD.getEstado()).isEqualTo(EstadoEnum.EN_USO);

        long trazasVendidas = trazaRepository.findAll().stream()
                .filter(t -> t.getEstado() == EstadoEnum.VENDIDO)
                .count();
        assertThat(trazasVendidas).isEqualTo(10);
    }

    @Test
    void test_bajaVentaTotalConAnalisisPendiente_debe_cancelarAnalisis() {
        // Given
        Analisis analisis = new Analisis();
        analisis.setNroAnalisis("A-001");
        analisis.setLote(loteTest);
        analisis.setFechaRealizado(LocalDate.now());
        analisis.setDictamen(null);
        analisis.setActivo(true);
        analisisRepository.save(analisis);

        LoteDTO dto = new LoteDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setFechaEgreso(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setNroBultoList(Arrays.asList(1));
        dto.setCantidadesBultos(Arrays.asList(new BigDecimal("100")));

        // When
        service.bajaVentaProducto(dto);

        // Then
        Analisis analisisBD = analisisRepository.findByNroAnalisisAndActivoTrue("A-001");
        assertThat(analisisBD.getDictamen()).isEqualTo(DictamenEnum.CANCELADO);
    }

    @Test
    void test_validarVentaProductoInputValido_debe_retornarTrue() {
        // Given
        LoteDTO dto = new LoteDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setFechaEgreso(LocalDate.now());
        dto.setNroBultoList(Arrays.asList(1));
        dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10")));
        dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.UNIDAD));
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "dto");

        // When
        boolean resultado = service.validarVentaProductoInput(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
    }
}
