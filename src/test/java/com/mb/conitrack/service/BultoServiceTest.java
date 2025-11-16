package com.mb.conitrack.service;

import com.mb.conitrack.dto.BultoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.BultoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para BultoService.
 * Cobertura completa de todos los métodos de consulta de bultos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - BultoService")
class BultoServiceTest {

    @Mock
    private BultoRepository bultoRepository;

    @InjectMocks
    private BultoService service;

    private Lote loteTest;
    private Bulto bulto1;
    private Bulto bulto2;

    @BeforeEach
    void setUp() {
        loteTest = new Lote();
        loteTest.setId(1L);
        loteTest.setCodigoLote("L-TEST-001");

        bulto1 = crearBultoTest(loteTest, 1);
        bulto2 = crearBultoTest(loteTest, 2);
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

    @Test
    @DisplayName("test_findByCodigoLote_debe_retornarBultosOrdenados")
    void test_findByCodigoLote_debe_retornarBultosOrdenados() {
        // Given
        when(bultoRepository.findAllByLoteCodigoLoteOrderByNroBultoAsc("L-TEST-001"))
                .thenReturn(Arrays.asList(bulto1, bulto2));

        // When
        List<BultoDTO> resultado = service.findByCodigoLote("L-TEST-001");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNroBulto()).isEqualTo(1);
        assertThat(resultado.get(1).getNroBulto()).isEqualTo(2);
        verify(bultoRepository).findAllByLoteCodigoLoteOrderByNroBultoAsc("L-TEST-001");
    }

    @Test
    @DisplayName("test_findByCodigoLote_sinBultos_debe_retornarListaVacia")
    void test_findByCodigoLote_sinBultos_debe_retornarListaVacia() {
        // Given
        when(bultoRepository.findAllByLoteCodigoLoteOrderByNroBultoAsc("L-INEXISTENTE"))
                .thenReturn(Collections.emptyList());

        // When
        List<BultoDTO> resultado = service.findByCodigoLote("L-INEXISTENTE");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(bultoRepository).findAllByLoteCodigoLoteOrderByNroBultoAsc("L-INEXISTENTE");
    }

    @Test
    @DisplayName("test_findAllBultos_debe_retornarTodosBultosActivos")
    void test_findAllBultos_debe_retornarTodosBultosActivos() {
        // Given
        when(bultoRepository.findAllByActivoTrue()).thenReturn(Arrays.asList(bulto1, bulto2));

        // When
        List<BultoDTO> resultado = service.findAllBultos();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNroBulto()).isEqualTo(1);
        assertThat(resultado.get(1).getNroBulto()).isEqualTo(2);
        verify(bultoRepository).findAllByActivoTrue();
    }

    @Test
    @DisplayName("test_findAllBultos_sinBultos_debe_retornarListaVacia")
    void test_findAllBultos_sinBultos_debe_retornarListaVacia() {
        // Given
        when(bultoRepository.findAllByActivoTrue()).thenReturn(Collections.emptyList());

        // When
        List<BultoDTO> resultado = service.findAllBultos();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(bultoRepository).findAllByActivoTrue();
    }
}
