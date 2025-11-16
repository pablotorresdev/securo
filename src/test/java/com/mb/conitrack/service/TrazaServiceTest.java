package com.mb.conitrack.service;

import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.repository.TrazaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TrazaService.
 * Cobertura completa de todos los métodos de consulta de trazas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - TrazaService")
class TrazaServiceTest {

    @Mock
    private TrazaRepository trazaRepository;

    @InjectMocks
    private TrazaService service;

    private Lote loteTest;
    private Bulto bultoTest;
    private Traza traza1;
    private Traza traza2;
    private com.mb.conitrack.entity.maestro.Producto productoTest;

    @BeforeEach
    void setUp() {
        productoTest = new com.mb.conitrack.entity.maestro.Producto();
        productoTest.setId(1L);
        productoTest.setCodigoProducto("PROD-001");

        loteTest = new Lote();
        loteTest.setId(1L);
        loteTest.setCodigoLote("L-TEST-001");

        bultoTest = new Bulto();
        bultoTest.setId(1L);
        bultoTest.setNroBulto(1);
        bultoTest.setLote(loteTest);

        traza1 = crearTrazaTest("T-001", EstadoEnum.DISPONIBLE);
        traza2 = crearTrazaTest("T-002", EstadoEnum.VENDIDO);
    }

    private Traza crearTrazaTest(String nroTrazaStr, EstadoEnum estado) {
        Traza traza = new Traza();
        traza.setId(1L);
        traza.setNroTraza(Long.parseLong(nroTrazaStr.substring(2)));
        traza.setLote(loteTest);
        traza.setBulto(bultoTest);
        traza.setProducto(productoTest);
        traza.setEstado(estado);
        traza.setActivo(true);
        return traza;
    }

    @Test
    @DisplayName("test_findAllByActivoTrue_debe_retornarTodasTrazasActivas")
    void test_findAllByActivoTrue_debe_retornarTodasTrazasActivas() {
        // Given
        when(trazaRepository.findAllByActivoTrue()).thenReturn(Arrays.asList(traza1, traza2));

        // When
        List<TrazaDTO> resultado = service.findAllByActivoTrue();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNroTraza()).isEqualTo(1L);
        assertThat(resultado.get(1).getNroTraza()).isEqualTo(2L);
        verify(trazaRepository).findAllByActivoTrue();
    }

    @Test
    @DisplayName("test_findAllByActivoTrue_sinTrazas_debe_retornarListaVacia")
    void test_findAllByActivoTrue_sinTrazas_debe_retornarListaVacia() {
        // Given
        when(trazaRepository.findAllByActivoTrue()).thenReturn(Collections.emptyList());

        // When
        List<TrazaDTO> resultado = service.findAllByActivoTrue();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(trazaRepository).findAllByActivoTrue();
    }

    @Test
    @DisplayName("test_findByCodigoLoteAndActivo_debe_retornarTrazasDelLoteOrdenadas")
    void test_findByCodigoLoteAndActivo_debe_retornarTrazasDelLoteOrdenadas() {
        // Given
        when(trazaRepository.findByLoteCodigoLoteAndActivoTrueOrderByNroTrazaAsc("L-TEST-001"))
                .thenReturn(Arrays.asList(traza1, traza2));

        // When
        List<TrazaDTO> resultado = service.findByCodigoLoteAndActivo("L-TEST-001");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNroTraza()).isEqualTo(1L);
        assertThat(resultado.get(1).getNroTraza()).isEqualTo(2L);
        verify(trazaRepository).findByLoteCodigoLoteAndActivoTrueOrderByNroTrazaAsc("L-TEST-001");
    }

    @Test
    @DisplayName("test_findByCodigoLoteAndActivo_sinTrazas_debe_retornarListaVacia")
    void test_findByCodigoLoteAndActivo_sinTrazas_debe_retornarListaVacia() {
        // Given
        when(trazaRepository.findByLoteCodigoLoteAndActivoTrueOrderByNroTrazaAsc("L-INEXISTENTE"))
                .thenReturn(Collections.emptyList());

        // When
        List<TrazaDTO> resultado = service.findByCodigoLoteAndActivo("L-INEXISTENTE");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(trazaRepository).findByLoteCodigoLoteAndActivoTrueOrderByNroTrazaAsc("L-INEXISTENTE");
    }

    @Test
    @DisplayName("test_getTrazasVendidasByCodigoMovimiento_debe_retornarTrazasVendidas")
    void test_getTrazasVendidasByCodigoMovimiento_debe_retornarTrazasVendidas() {
        // Given
        when(trazaRepository.findVendidasByCodigoMovimiento("MOV-001"))
                .thenReturn(Arrays.asList(traza2));

        // When
        List<TrazaDTO> resultado = service.getTrazasVendidasByCodigoMovimiento("MOV-001");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNroTraza()).isEqualTo(2L);
        assertThat(resultado.get(0).getEstado()).isEqualTo(EstadoEnum.VENDIDO);
        verify(trazaRepository).findVendidasByCodigoMovimiento("MOV-001");
    }

    @Test
    @DisplayName("test_getTrazasVendidasByCodigoMovimiento_sinTrazasVendidas_debe_retornarListaVacia")
    void test_getTrazasVendidasByCodigoMovimiento_sinTrazasVendidas_debe_retornarListaVacia() {
        // Given
        when(trazaRepository.findVendidasByCodigoMovimiento("MOV-INEXISTENTE"))
                .thenReturn(Collections.emptyList());

        // When
        List<TrazaDTO> resultado = service.getTrazasVendidasByCodigoMovimiento("MOV-INEXISTENTE");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(trazaRepository).findVendidasByCodigoMovimiento("MOV-INEXISTENTE");
    }

    @Test
    @DisplayName("test_getTrazasVendidasByCodigoLote_debe_retornarTrazasVendidas")
    void test_getTrazasVendidasByCodigoLote_debe_retornarTrazasVendidas() {
        // Given
        when(trazaRepository.findVendidasByCodigoLote("L-TEST-001"))
                .thenReturn(Arrays.asList(traza2));

        // When
        List<TrazaDTO> resultado = service.getTrazasVendidasByCodigoLote("L-TEST-001");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNroTraza()).isEqualTo(2L);
        assertThat(resultado.get(0).getEstado()).isEqualTo(EstadoEnum.VENDIDO);
        verify(trazaRepository).findVendidasByCodigoLote("L-TEST-001");
    }

    @Test
    @DisplayName("test_getTrazasVendidasByCodigoLote_sinTrazasVendidas_debe_retornarListaVacia")
    void test_getTrazasVendidasByCodigoLote_sinTrazasVendidas_debe_retornarListaVacia() {
        // Given
        when(trazaRepository.findVendidasByCodigoLote("L-INEXISTENTE"))
                .thenReturn(Collections.emptyList());

        // When
        List<TrazaDTO> resultado = service.getTrazasVendidasByCodigoLote("L-INEXISTENTE");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(trazaRepository).findVendidasByCodigoLote("L-INEXISTENTE");
    }

    @Test
    @DisplayName("test_getTrazasByCodigoLoteAndNroBulto_debe_retornarTrazasDisponibles")
    void test_getTrazasByCodigoLoteAndNroBulto_debe_retornarTrazasDisponibles() {
        // Given
        when(trazaRepository.findDisponiblesByCodigoLoteAndNroBulto("L-TEST-001", 1))
                .thenReturn(Arrays.asList(traza1));

        // When
        List<TrazaDTO> resultado = service.getTrazasByCodigoLoteAndNroBulto("L-TEST-001", 1);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNroTraza()).isEqualTo(1L);
        assertThat(resultado.get(0).getEstado()).isEqualTo(EstadoEnum.DISPONIBLE);
        verify(trazaRepository).findDisponiblesByCodigoLoteAndNroBulto("L-TEST-001", 1);
    }

    @Test
    @DisplayName("test_getTrazasByCodigoLoteAndNroBulto_sinTrazasDisponibles_debe_retornarListaVacia")
    void test_getTrazasByCodigoLoteAndNroBulto_sinTrazasDisponibles_debe_retornarListaVacia() {
        // Given
        when(trazaRepository.findDisponiblesByCodigoLoteAndNroBulto("L-INEXISTENTE", 999))
                .thenReturn(Collections.emptyList());

        // When
        List<TrazaDTO> resultado = service.getTrazasByCodigoLoteAndNroBulto("L-INEXISTENTE", 999);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(trazaRepository).findDisponiblesByCodigoLoteAndNroBulto("L-INEXISTENTE", 999);
    }
}
