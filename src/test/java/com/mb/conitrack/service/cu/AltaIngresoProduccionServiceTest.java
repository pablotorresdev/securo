package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.*;
import com.mb.conitrack.repository.BultoRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.TrazaRepository;
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
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.mb.conitrack.testdata.TestDataBuilder.unLoteDTO;
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
@DisplayName("Tests de Integracion - AltaIngresoProduccionService (CU20)")
class AltaIngresoProduccionServiceTest {

    @Autowired
    private AltaIngresoProduccionService service;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private BultoRepository bultoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private TrazaRepository trazaRepository;

    @MockBean
    private com.mb.conitrack.service.SecurityContextService securityContextService;

    private Producto productoTest;
    private Proveedor conifarmaProveedor;
    private User testUser;

    @BeforeEach
    void setUp() {
        reset(securityContextService);

        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        testUser = new User("testuser", "password", adminRole);
        testUser.setId(1L);

        when(securityContextService.getCurrentUser()).thenReturn(testUser);

        productoTest = new Producto();
        productoTest.setCodigoProducto("API-PROD-001");
        productoTest.setNombreGenerico("Paracetamol Produccion");
        productoTest.setTipoProducto(TipoProductoEnum.API);
        productoTest.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        productoTest.setActivo(true);
        productoTest = productoRepository.save(productoTest);

        conifarmaProveedor = new Proveedor();
        conifarmaProveedor.setRazonSocial("CONIFARMA");
        conifarmaProveedor.setPais("Argentina");
        conifarmaProveedor.setCuit("30-99999999-9");
        conifarmaProveedor.setDireccion("Calle Conifarma 123");
        conifarmaProveedor.setCiudad("Buenos Aires");
        conifarmaProveedor.setActivo(true);
        conifarmaProveedor = proveedorRepository.save(conifarmaProveedor);
    }

    @Nested
    @DisplayName("altaStockPorProduccion() - Flujo completo")
    class AltaStockPorProduccion {

        @Test
        @DisplayName("test_altaProduccionConUnBulto_debe_crearLoteYMovimiento")
        void test_altaProduccionConUnBulto_debe_crearLoteYMovimiento() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoTest.getId())
                    .withCantidadInicial(new BigDecimal("50.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(1)
                    .withLoteProveedor("LP-PROD-001")
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            LoteDTO resultado = service.altaStockPorProduccion(dto);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigoLote()).isNotNull();
            assertThat(resultado.getCodigoLote()).startsWith("L-API-PROD-001-");

            List<Lote> lotes = loteRepository.findAll();
            Lote loteBD = lotes.stream()
                    .filter(l -> l.getCodigoLote().equals(resultado.getCodigoLote()))
                    .findFirst()
                    .orElse(null);
            assertThat(loteBD).isNotNull();
            assertThat(loteBD.getEstado()).isEqualTo(EstadoEnum.NUEVO);
            assertThat(loteBD.getDictamen()).isEqualTo(DictamenEnum.RECIBIDO);
            assertThat(loteBD.getProveedor().getRazonSocial()).isEqualTo("CONIFARMA");
            assertThat(loteBD.getCantidadInicial()).isEqualByComparingTo(new BigDecimal("50.0"));
            assertThat(loteBD.getCantidadActual()).isEqualByComparingTo(new BigDecimal("50.0"));

            assertThat(loteBD.getBultos()).hasSize(1);
            Bulto bulto = loteBD.getBultos().get(0);
            assertThat(bulto.getNroBulto()).isEqualTo(1);
            assertThat(bulto.getCantidadInicial()).isEqualByComparingTo(new BigDecimal("50.0"));

            assertThat(loteBD.getMovimientos()).hasSize(1);
            Movimiento movimiento = loteBD.getMovimientos().get(0);
            assertThat(movimiento.getTipoMovimiento()).isEqualTo(TipoMovimientoEnum.ALTA);
            assertThat(movimiento.getMotivo()).isEqualTo(MotivoEnum.PRODUCCION_PROPIA);
        }

        @Test
        @DisplayName("test_altaProduccionConMultiplesBultos_debe_distribuirCantidades")
        void test_altaProduccionConMultiplesBultos_debe_distribuirCantidades() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoTest.getId())
                    .withCantidadInicial(new BigDecimal("100.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(3)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("40.0"),
                            new BigDecimal("35.0"),
                            new BigDecimal("25.0")
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO
                    ))
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            LoteDTO resultado = service.altaStockPorProduccion(dto);

            // Then
            List<Lote> lotes = loteRepository.findAll();
            Lote loteBD = lotes.stream()
                    .filter(l -> l.getCodigoLote().equals(resultado.getCodigoLote()))
                    .findFirst()
                    .orElse(null);
            assertThat(loteBD).isNotNull();
            assertThat(loteBD.getBultos()).hasSize(3);
            assertThat(loteBD.getBultos().get(0).getCantidadInicial()).isEqualByComparingTo(new BigDecimal("40.0"));
            assertThat(loteBD.getBultos().get(1).getCantidadInicial()).isEqualByComparingTo(new BigDecimal("35.0"));
            assertThat(loteBD.getBultos().get(2).getCantidadInicial()).isEqualByComparingTo(new BigDecimal("25.0"));
        }

        @Test
        @DisplayName("test_altaProduccionConProductoInexistente_debe_lanzarExcepcion")
        void test_altaProduccionConProductoInexistente_debe_lanzarExcepcion() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withProductoId(99999L)
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When & Then
            assertThatThrownBy(() -> service.altaStockPorProduccion(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("producto no existe");
        }

        @Test
        @DisplayName("test_altaProduccionConProveedorConifarmaNulo_debe_lanzarExcepcion")
        void test_altaProduccionConProveedorConifarmaNulo_debe_lanzarExcepcion() {
            // Given - Eliminar Conifarma
            proveedorRepository.delete(conifarmaProveedor);

            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoTest.getId())
                    .withCantidadInicial(new BigDecimal("50.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(1)
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When & Then
            assertThatThrownBy(() -> service.altaStockPorProduccion(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Conifarma no existe");
        }
    }

    @Nested
    @DisplayName("validarIngresoProduccionInput() - Validaciones")
    class ValidarIngresoProduccionInput {

        @Test
        @DisplayName("test_validarInputValido_debe_retornarTrue")
        void test_validarInputValido_debe_retornarTrue() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("25.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(1)
                    .build();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.validarIngresoProduccionInput(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue();
            assertThat(bindingResult.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("test_validarConErroresPrevios_debe_retornarFalse")
        void test_validarConErroresPrevios_debe_retornarFalse() {
            // Given
            LoteDTO dto = unLoteDTO().build();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");
            bindingResult.reject("error.global", "Error previo");

            // When
            boolean resultado = service.validarIngresoProduccionInput(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("test_validarCantidadNula_debe_rechazar")
        void test_validarCantidadNula_debe_rechazar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(null)
                    .build();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.validarIngresoProduccionInput(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("cantidadInicial")).isNotNull();
        }

        @Test
        @DisplayName("test_validarBultosConSumaIncorrecta_debe_rechazar")
        void test_validarBultosConSumaIncorrecta_debe_rechazar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("30.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(2)
                    .withCantidadesBultos(Arrays.asList(
                            new BigDecimal("10.0"),
                            new BigDecimal("15.0")
                    ))
                    .withUnidadMedidaBultos(Arrays.asList(
                            UnidadMedidaEnum.KILOGRAMO,
                            UnidadMedidaEnum.KILOGRAMO
                    ))
                    .build();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.validarIngresoProduccionInput(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("cantidadesBultos")).isNotNull();
        }
    }

    @Nested
    @DisplayName("validarTraza() - Validaciones de traza")
    class ValidarTraza {

        @Test
        @DisplayName("test_validarTrazaParaUnidadVenta_debe_validarTrazaInicial")
        void test_validarTrazaParaUnidadVenta_debe_validarTrazaInicial() {
            // Given - Producto UNIDAD_VENTA
            Producto productoUnidad = new Producto();
            productoUnidad.setCodigoProducto("UV-001");
            productoUnidad.setNombreGenerico("Unidad Venta Test");
            productoUnidad.setTipoProducto(TipoProductoEnum.UNIDAD_VENTA);
            productoUnidad.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            productoUnidad.setActivo(true);
            productoUnidad = productoRepository.save(productoUnidad);

            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoUnidad.getId())
                    .withCantidadInicial(new BigDecimal("100"))
                    .withUnidadMedida(UnidadMedidaEnum.UNIDAD)
                    .withBultosTotales(1)
                    .withTrazaInicial(1L)
                    .build();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.validarTraza(dto, bindingResult);

            // Then
            assertThat(resultado).isTrue();
            assertThat(bindingResult.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("test_validarTrazaParaNoUnidad_debe_rechazar")
        void test_validarTrazaParaNoUnidad_debe_rechazar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withCantidadInicial(new BigDecimal("25.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(1)
                    .withTrazaInicial(1L)
                    .build();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.validarTraza(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("trazaInicial")).isNotNull();
            assertThat(bindingResult.getFieldError("trazaInicial").getDefaultMessage())
                    .contains("solo aplica a unidades de venta");
        }

        @Test
        @DisplayName("test_validarTrazaConNumeroMenorAlMaximo_debe_rechazar")
        void test_validarTrazaConNumeroMenorAlMaximo_debe_rechazar() {
            // Given - Simular que ya existe traza máxima
            Producto productoUnidad = new Producto();
            productoUnidad.setCodigoProducto("UV-002");
            productoUnidad.setNombreGenerico("Unidad Venta Test 2");
            productoUnidad.setTipoProducto(TipoProductoEnum.UNIDAD_VENTA);
            productoUnidad.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            productoUnidad.setActivo(true);
            productoUnidad = productoRepository.save(productoUnidad);

            // Crear lote con traza para simular max
            LoteDTO loteAnterior = unLoteDTO()
                    .withProductoId(productoUnidad.getId())
                    .withCantidadInicial(new BigDecimal("10"))
                    .withUnidadMedida(UnidadMedidaEnum.UNIDAD)
                    .withBultosTotales(1)
                    .withTrazaInicial(100L)
                    .build();
            loteAnterior.setFechaYHoraCreacion(OffsetDateTime.now());
            service.altaStockPorProduccion(loteAnterior);

            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoUnidad.getId())
                    .withCantidadInicial(new BigDecimal("10"))
                    .withUnidadMedida(UnidadMedidaEnum.UNIDAD)
                    .withBultosTotales(1)
                    .withTrazaInicial(50L) // Menor que el máximo
                    .build();
            BindingResult bindingResult = new BeanPropertyBindingResult(dto, "loteDTO");

            // When
            boolean resultado = service.validarTraza(dto, bindingResult);

            // Then
            assertThat(resultado).isFalse();
            assertThat(bindingResult.hasErrors()).isTrue();
            assertThat(bindingResult.getFieldError("trazaInicial")).isNotNull();
            assertThat(bindingResult.getFieldError("trazaInicial").getDefaultMessage())
                    .contains("debe ser mayor al último registrado");
        }
    }

    @Nested
    @DisplayName("Casos edge y limites")
    class CasosEdge {

        @Test
        @DisplayName("test_altaProduccionCantidadMuyGrande_debe_procesar")
        void test_altaProduccionCantidadMuyGrande_debe_procesar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoTest.getId())
                    .withCantidadInicial(new BigDecimal("5000.0"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(1)
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            LoteDTO resultado = service.altaStockPorProduccion(dto);

            // Then
            assertThat(resultado).isNotNull();
            List<Lote> lotes = loteRepository.findAll();
            Lote loteBD = lotes.stream()
                    .filter(l -> l.getCodigoLote().equals(resultado.getCodigoLote()))
                    .findFirst()
                    .orElse(null);
            assertThat(loteBD).isNotNull();
            assertThat(loteBD.getCantidadActual()).isEqualByComparingTo(new BigDecimal("5000.0"));
        }

        @Test
        @DisplayName("test_altaProduccionCantidadMuyPequena_debe_procesar")
        void test_altaProduccionCantidadMuyPequena_debe_procesar() {
            // Given
            LoteDTO dto = unLoteDTO()
                    .withProductoId(productoTest.getId())
                    .withCantidadInicial(new BigDecimal("0.1"))
                    .withUnidadMedida(UnidadMedidaEnum.KILOGRAMO)
                    .withBultosTotales(1)
                    .build();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // When
            LoteDTO resultado = service.altaStockPorProduccion(dto);

            // Then
            assertThat(resultado).isNotNull();
            List<Lote> lotes = loteRepository.findAll();
            Lote loteBD = lotes.stream()
                    .filter(l -> l.getCodigoLote().equals(resultado.getCodigoLote()))
                    .findFirst()
                    .orElse(null);
            assertThat(loteBD).isNotNull();
            assertThat(loteBD.getCantidadActual()).isEqualByComparingTo(new BigDecimal("0.1"));
        }
    }
}
