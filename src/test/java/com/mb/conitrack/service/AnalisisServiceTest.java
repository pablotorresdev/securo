package com.mb.conitrack.service;

import com.mb.conitrack.dto.AnalisisDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AnalisisService.
 * Cobertura completa de todos los métodos de consulta de análisis.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - AnalisisService")
class AnalisisServiceTest {

    @Mock
    private AnalisisRepository analisisRepository;

    @InjectMocks
    private AnalisisService service;

    private Analisis analisis1;
    private Analisis analisis2;
    private Lote loteTest;

    @BeforeEach
    void setUp() {
        loteTest = new Lote();
        loteTest.setId(1L);
        loteTest.setCodigoLote("L-TEST-001");

        analisis1 = crearAnalisisTest("AN-2025-001", DictamenEnum.APROBADO);
        analisis2 = crearAnalisisTest("AN-2025-002", null); // En curso
    }

    Analisis crearAnalisisTest(String nroAnalisis, DictamenEnum dictamen) {
        Analisis analisis = new Analisis();
        analisis.setId(1L);
        analisis.setNroAnalisis(nroAnalisis);
        analisis.setLote(loteTest);
        analisis.setDictamen(dictamen);
        analisis.setFechaRealizado(dictamen != null ? LocalDate.now() : null);
        analisis.setActivo(true);
        return analisis;
    }

    @Test
    @DisplayName("test_findAllAnalisis_debe_retornarTodosAnalisisActivos")
    void test_findAllAnalisis_debe_retornarTodosAnalisisActivos() {
        // Given
        when(analisisRepository.findAllByActivoTrue()).thenReturn(Arrays.asList(analisis1, analisis2));

        // When
        List<AnalisisDTO> resultado = service.findAllAnalisis();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNroAnalisis()).isEqualTo("AN-2025-001");
        assertThat(resultado.get(1).getNroAnalisis()).isEqualTo("AN-2025-002");
        verify(analisisRepository).findAllByActivoTrue();
    }

    @Test
    @DisplayName("test_findAllAnalisis_sinAnalisis_debe_retornarListaVacia")
    void test_findAllAnalisis_sinAnalisis_debe_retornarListaVacia() {
        // Given
        when(analisisRepository.findAllByActivoTrue()).thenReturn(Collections.emptyList());

        // When
        List<AnalisisDTO> resultado = service.findAllAnalisis();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(analisisRepository).findAllByActivoTrue();
    }

    @Test
    @DisplayName("test_findAllEnCursoDTOs_debe_retornarAnalisisEnCurso")
    void test_findAllEnCursoDTOs_debe_retornarAnalisisEnCurso() {
        // Given
        when(analisisRepository.findAllEnCurso()).thenReturn(Arrays.asList(analisis2));

        // When
        List<AnalisisDTO> resultado = service.findAllEnCursoDTOs();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNroAnalisis()).isEqualTo("AN-2025-002");
        assertThat(resultado.get(0).getDictamen()).isNull();
        verify(analisisRepository).findAllEnCurso();
    }

    @Test
    @DisplayName("test_findAllEnCursoDTOs_sinAnalisisEnCurso_debe_retornarListaVacia")
    void test_findAllEnCursoDTOs_sinAnalisisEnCurso_debe_retornarListaVacia() {
        // Given
        when(analisisRepository.findAllEnCurso()).thenReturn(Collections.emptyList());

        // When
        List<AnalisisDTO> resultado = service.findAllEnCursoDTOs();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(analisisRepository).findAllEnCurso();
    }

    @Test
    @DisplayName("test_findAllEnCursoForLotesCuarentenaDTOs_debe_retornarAnalisisEnCurso")
    void test_findAllEnCursoForLotesCuarentenaDTOs_debe_retornarAnalisisEnCurso() {
        // Given
        when(analisisRepository.findAllEnCursoForLotesCuarentena()).thenReturn(Arrays.asList(analisis2));

        // When
        List<AnalisisDTO> resultado = service.findAllEnCursoForLotesCuarentenaDTOs();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNroAnalisis()).isEqualTo("AN-2025-002");
        verify(analisisRepository).findAllEnCursoForLotesCuarentena();
    }

    @Test
    @DisplayName("test_findAllEnCursoForLotesCuarentenaDTOs_sinAnalisis_debe_retornarListaVacia")
    void test_findAllEnCursoForLotesCuarentenaDTOs_sinAnalisis_debe_retornarListaVacia() {
        // Given
        when(analisisRepository.findAllEnCursoForLotesCuarentena()).thenReturn(Collections.emptyList());

        // When
        List<AnalisisDTO> resultado = service.findAllEnCursoForLotesCuarentenaDTOs();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(analisisRepository).findAllEnCursoForLotesCuarentena();
    }

    @Test
    @DisplayName("test_findByNroAnalisis_analisisExiste_debe_retornarAnalisis")
    void test_findByNroAnalisis_analisisExiste_debe_retornarAnalisis() {
        // Given
        when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(analisis1);

        // When
        Analisis resultado = service.findByNroAnalisis("AN-2025-001");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNroAnalisis()).isEqualTo("AN-2025-001");
        assertThat(resultado.getDictamen()).isEqualTo(DictamenEnum.APROBADO);
        verify(analisisRepository).findByNroAnalisisAndActivoTrue("AN-2025-001");
    }

    @Test
    @DisplayName("test_findByNroAnalisis_analisisNoExiste_debe_retornarNull")
    void test_findByNroAnalisis_analisisNoExiste_debe_retornarNull() {
        // Given
        when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-INEXISTENTE")).thenReturn(null);

        // When
        Analisis resultado = service.findByNroAnalisis("AN-INEXISTENTE");

        // Then
        assertThat(resultado).isNull();
        verify(analisisRepository).findByNroAnalisisAndActivoTrue("AN-INEXISTENTE");
    }

    @Test
    @DisplayName("test_findByCodigoLote_debe_retornarAnalisisDelLote")
    void test_findByCodigoLote_debe_retornarAnalisisDelLote() {
        // Given
        when(analisisRepository.findByCodigoLote("L-TEST-001")).thenReturn(Arrays.asList(analisis1, analisis2));

        // When
        List<AnalisisDTO> resultado = service.findByCodigoLote("L-TEST-001");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNroAnalisis()).isEqualTo("AN-2025-001");
        assertThat(resultado.get(1).getNroAnalisis()).isEqualTo("AN-2025-002");
        verify(analisisRepository).findByCodigoLote("L-TEST-001");
    }

    @Test
    @DisplayName("test_findByCodigoLote_sinAnalisis_debe_retornarListaVacia")
    void test_findByCodigoLote_sinAnalisis_debe_retornarListaVacia() {
        // Given
        when(analisisRepository.findByCodigoLote("L-INEXISTENTE")).thenReturn(Collections.emptyList());

        // When
        List<AnalisisDTO> resultado = service.findByCodigoLote("L-INEXISTENTE");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(analisisRepository).findByCodigoLote("L-INEXISTENTE");
    }
}
