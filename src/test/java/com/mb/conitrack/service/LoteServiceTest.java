package com.mb.conitrack.service;

import com.mb.conitrack.dto.BultoDTO;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.LoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para LoteService.
 * Verifica todos los métodos de consulta de lotes por caso de uso.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - LoteService")
class LoteServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @InjectMocks
    private LoteService service;

    private Lote loteTest;
    private Producto productoTest;
    private Proveedor proveedorTest;

    @BeforeEach
    void setUp() {
        productoTest = new Producto();
        productoTest.setId(1L);
        productoTest.setCodigoProducto("API-TEST-001");
        productoTest.setNombreGenerico("Paracetamol Test");
        productoTest.setTipoProducto(TipoProductoEnum.API);
        productoTest.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        productoTest.setActivo(true);

        proveedorTest = new Proveedor();
        proveedorTest.setId(1L);
        proveedorTest.setRazonSocial("Proveedor Test");
        proveedorTest.setPais("Argentina");
        proveedorTest.setCuit("20-12345678-9");
        proveedorTest.setActivo(true);

        loteTest = crearLoteTest();
    }

    private Lote crearLoteTest() {
        Lote lote = new Lote();
        lote.setId(1L);
        lote.setCodigoLote("L-TEST-001");
        lote.setLoteProveedor("LP-2025-001");
        lote.setProducto(productoTest);
        lote.setProveedor(proveedorTest);
        lote.setFechaIngreso(LocalDate.now().minusDays(10));
        lote.setCantidadInicial(new BigDecimal("100.00"));
        lote.setCantidadActual(new BigDecimal("100.00"));
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.setBultosTotales(2);
        lote.setEstado(EstadoEnum.NUEVO);
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setActivo(true);
        lote.setMovimientos(new ArrayList<>());
        lote.setAnalisisList(new ArrayList<>());
        return lote;
    }

    private Bulto crearBultoTest(Lote lote, int nroBulto) {
        Bulto bulto = new Bulto();
        bulto.setId((long) nroBulto);
        bulto.setNroBulto(nroBulto);
        bulto.setCantidadInicial(new BigDecimal("50.00"));
        bulto.setCantidadActual(new BigDecimal("50.00"));
        bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto.setEstado(EstadoEnum.NUEVO);
        bulto.setLote(lote);
        bulto.setActivo(true);
        return bulto;
    }

    @Nested
    @DisplayName("Métodos de consulta por caso de uso")
    class MetodosConsultaPorCU {

        @Test
        @DisplayName("test_findAllForCuarentenaDTOs_debe_retornarLista")
        void test_findAllForCuarentenaDTOs() {
            when(loteRepository.findAllForCuarentena()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForCuarentenaDTOs();
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getCodigoLote()).isEqualTo("L-TEST-001");
            verify(loteRepository).findAllForCuarentena();
        }

        @Test
        @DisplayName("test_findAllForMuestreoTrazableDTOs_debe_retornarLista")
        void test_findAllForMuestreoTrazableDTOs() {
            when(loteRepository.findAllForMuestreoTrazable()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForMuestreoTrazableDTOs();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllForMuestreoTrazable();
        }

        @Test
        @DisplayName("test_findAllForMuestreoMultiBultoDTOs_debe_retornarLista")
        void test_findAllForMuestreoMultiBultoDTOs() {
            when(loteRepository.findAllForMuestreoMultiBulto()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForMuestreoMultiBultoDTOs();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllForMuestreoMultiBulto();
        }

        @Test
        @DisplayName("test_findAllForDevolucionCompraDTOs_debe_retornarLista")
        void test_findAllForDevolucionCompraDTOs() {
            when(loteRepository.findAllForDevolucionCompra()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForDevolucionCompraDTOs();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllForDevolucionCompra();
        }

        @Test
        @DisplayName("test_findAllForReanalisisLoteDTOs_debe_retornarLista")
        void test_findAllForReanalisisLoteDTOs() {
            when(loteRepository.findAllForReanalisisLote()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForReanalisisLoteDTOs();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllForReanalisisLote();
        }

        @Test
        @DisplayName("test_findAllForResultadoAnalisisDTOs_debe_retornarLista")
        void test_findAllForResultadoAnalisisDTOs() {
            when(loteRepository.findAllForResultadoAnalisis()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForResultadoAnalisisDTOs();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllForResultadoAnalisis();
        }

        @Test
        @DisplayName("test_findAllForConsumoProduccionDTOs_debe_retornarLista")
        void test_findAllForConsumoProduccionDTOs() {
            when(loteRepository.findAllForConsumoProduccion()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForConsumoProduccionDTOs();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllForConsumoProduccion();
        }

        @Test
        @DisplayName("test_findAllForLiberacionProductoDTOs_debe_retornarLista")
        void test_findAllForLiberacionProductoDTOs() {
            when(loteRepository.findAllForLiberacionProducto()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForLiberacionProductoDTOs();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllForLiberacionProducto();
        }

        @Test
        @DisplayName("test_findAllForTrazadoLoteDTOs_debe_retornarLista")
        void test_findAllForTrazadoLoteDTOs() {
            when(loteRepository.findAllForTrazadoLote()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForTrazadoLoteDTOs();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllForTrazadoLote();
        }

        @Test
        @DisplayName("test_findAllForVentaProductoDTOs_debe_retornarLista")
        void test_findAllForVentaProductoDTOs() {
            when(loteRepository.findAllForVentaProducto()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForVentaProductoDTOs();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllForVentaProducto();
        }

        @Test
        @DisplayName("test_findAllForDevolucionDTOs_debe_retornarLista")
        void test_findAllForDevolucionDTOs() {
            when(loteRepository.findAllForDevolucion()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForDevolucionDTOs();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllForDevolucion();
        }

        @Test
        @DisplayName("test_findAllForRecallDTOs_debe_retornarLista")
        void test_findAllForRecallDTOs() {
            when(loteRepository.findAllForRecall()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForRecallDTOs();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllForRecall();
        }

        @Test
        @DisplayName("test_findAllForAjusteDTOs_debe_retornarLista")
        void test_findAllForAjusteDTOs() {
            when(loteRepository.findAllForAjuste()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForAjusteDTOs();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllForAjuste();
        }

        @Test
        @DisplayName("test_findAllForReversoMovimientoDTOs_debe_retornarLista")
        void test_findAllForReversoMovimientoDTOs() {
            when(loteRepository.findAllForReversoMovimiento()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllForReversoMovimientoDTOs();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllForReversoMovimiento();
        }

        @Test
        @DisplayName("test_findAllLotesAudit_debe_retornarListaOrdenada")
        void test_findAllLotesAudit() {
            when(loteRepository.findAllByOrderByFechaIngresoAscCodigoLoteAsc()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllLotesAudit();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllByOrderByFechaIngresoAscCodigoLoteAsc();
        }

        @Test
        @DisplayName("test_findAllLotes_debe_retornarLotesActivos")
        void test_findAllLotes() {
            when(loteRepository.findAllByActivoTrue()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findAllLotes();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findAllByActivoTrue();
        }

        @Test
        @DisplayName("test_findLotesDictaminadosConStock_debe_retornarLista")
        void test_findLotesDictaminadosConStock() {
            when(loteRepository.findLotesDictaminadosConStock()).thenReturn(Arrays.asList(loteTest));
            List<LoteDTO> resultado = service.findLotesDictaminadosConStock();
            assertThat(resultado).hasSize(1);
            verify(loteRepository).findLotesDictaminadosConStock();
        }
    }

    @Nested
    @DisplayName("Métodos de consulta de bultos")
    class MetodosConsultaBultos {

        @Test
        @DisplayName("test_findBultosForMuestreoByCodigoLote_debe_retornarBultos")
        void test_findBultosForMuestreoByCodigoLote() {
            Bulto bulto1 = crearBultoTest(loteTest, 1);
            when(loteRepository.findBultosForMuestreoByCodigoLote("L-TEST-001"))
                    .thenReturn(Arrays.asList(bulto1));
            List<BultoDTO> resultado = service.findBultosForMuestreoByCodigoLote("L-TEST-001");
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNroBulto()).isEqualTo(1);
            verify(loteRepository).findBultosForMuestreoByCodigoLote("L-TEST-001");
        }

        @Test
        @DisplayName("test_findBultosForAjusteByCodigoLote_debe_retornarBultos")
        void test_findBultosForAjusteByCodigoLote() {
            Bulto bulto1 = crearBultoTest(loteTest, 1);
            Bulto bulto2 = crearBultoTest(loteTest, 2);
            when(loteRepository.findBultosForAjusteByCodigoLote("L-TEST-001"))
                    .thenReturn(Arrays.asList(bulto1, bulto2));
            List<BultoDTO> resultado = service.findBultosForAjusteByCodigoLote("L-TEST-001");
            assertThat(resultado).hasSize(2);
            verify(loteRepository).findBultosForAjusteByCodigoLote("L-TEST-001");
        }
    }

    @Nested
    @DisplayName("Métodos de búsqueda por código")
    class MetodosBusquedaPorCodigo {

        @Test
        @DisplayName("test_findByCodigoLote_loteExiste_debe_retornarOptionalConLote")
        void test_findByCodigoLote_existe() {
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001"))
                    .thenReturn(Optional.of(loteTest));
            Optional<Lote> resultado = service.findByCodigoLote("L-TEST-001");
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getCodigoLote()).isEqualTo("L-TEST-001");
            verify(loteRepository).findByCodigoLoteAndActivoTrue("L-TEST-001");
        }

        @Test
        @DisplayName("test_findByCodigoLote_loteNoExiste_debe_retornarOptionalVacio")
        void test_findByCodigoLote_noExiste() {
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-INEXISTENTE"))
                    .thenReturn(Optional.empty());
            Optional<Lote> resultado = service.findByCodigoLote("L-INEXISTENTE");
            assertThat(resultado).isEmpty();
            verify(loteRepository).findByCodigoLoteAndActivoTrue("L-INEXISTENTE");
        }

        @Test
        @DisplayName("test_findDTOByCodigoLote_loteExiste_debe_retornarOptionalConDTO")
        void test_findDTOByCodigoLote_existe() {
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001"))
                    .thenReturn(Optional.of(loteTest));
            Optional<LoteDTO> resultado = service.findDTOByCodigoLote("L-TEST-001");
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getCodigoLote()).isEqualTo("L-TEST-001");
            verify(loteRepository).findByCodigoLoteAndActivoTrue("L-TEST-001");
        }

        @Test
        @DisplayName("test_findDTOByCodigoLote_loteNoExiste_debe_retornarOptionalVacio")
        void test_findDTOByCodigoLote_noExiste() {
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-INEXISTENTE"))
                    .thenReturn(Optional.empty());
            Optional<LoteDTO> resultado = service.findDTOByCodigoLote("L-INEXISTENTE");
            assertThat(resultado).isEmpty();
            verify(loteRepository).findByCodigoLoteAndActivoTrue("L-INEXISTENTE");
        }
    }

    @Nested
    @DisplayName("Casos con listas vacías")
    class CasosListasVacias {

        @Test
        @DisplayName("test_findAllForCuarentenaDTOs_sinLotes_debe_retornarListaVacia")
        void test_findAllForCuarentenaDTOs_vacia() {
            when(loteRepository.findAllForCuarentena()).thenReturn(Collections.emptyList());
            List<LoteDTO> resultado = service.findAllForCuarentenaDTOs();
            assertThat(resultado).isEmpty();
            verify(loteRepository).findAllForCuarentena();
        }

        @Test
        @DisplayName("test_findBultosForMuestreoByCodigoLote_sinBultos_debe_retornarListaVacia")
        void test_findBultosForMuestreoByCodigoLote_vacia() {
            when(loteRepository.findBultosForMuestreoByCodigoLote("L-TEST-001"))
                    .thenReturn(Collections.emptyList());
            List<BultoDTO> resultado = service.findBultosForMuestreoByCodigoLote("L-TEST-001");
            assertThat(resultado).isEmpty();
            verify(loteRepository).findBultosForMuestreoByCodigoLote("L-TEST-001");
        }
    }
}