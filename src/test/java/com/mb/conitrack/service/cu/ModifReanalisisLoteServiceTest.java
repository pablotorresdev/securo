package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
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
import static org.mockito.Mockito.when;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never"
})
@Transactional
class ModifReanalisisLoteServiceTest {

    @Autowired private ModifReanalisisLoteService service;
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
    void test_persistirReanalisisLote_debe_crearNuevoAnalisis() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-REANALISIS-001");
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        dto.setFechaYHoraCreacion(OffsetDateTime.now());

        // When
        LoteDTO resultado = service.persistirReanalisisLote(dto);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(analisisRepository.findByNroAnalisisAndActivoTrue("A-REANALISIS-001")).isNotNull();
    }

    @Test
    void test_validarReanalisisLoteInputValido_debe_retornarTrue() {
        // Given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote(loteTest.getCodigoLote());
        dto.setNroAnalisis("A-002");
        dto.setFechaMovimiento(LocalDate.now());
        dto.setFechaRealizadoAnalisis(LocalDate.now());
        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "dto");

        // When
        boolean resultado = service.validarReanalisisLoteInput(dto, bindingResult);

        // Then
        assertThat(resultado).isTrue();
    }
}
