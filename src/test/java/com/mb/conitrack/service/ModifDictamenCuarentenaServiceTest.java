package com.mb.conitrack.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.service.cu.ModifDictamenCuarentenaService;
import com.mb.conitrack.utils.MovimientoEntityUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifDictamenCuarentenaServiceTest {

    @Mock
    LoteRepository loteRepository;

    @Mock
    MovimientoRepository movimientoRepository;

    @Mock
    AnalisisRepository analisisRepository;

    @InjectMocks
    ModifDictamenCuarentenaService dictamenCuarentenaService;

    @Test
    @DisplayName("OK -> setea motivo/DICTAMEN/nroAnalisis/observaciones y guarda")
    void persistirMovimientoCuarentenaPorAnalisis_ok() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setObservaciones("Obs de prueba");

        Lote lote = new Lote();
        lote.setDictamen(DictamenEnum.APROBADO); // dictamen inicial esperado

        String nroAnalisisParam = "AN-123";

        Movimiento base = new Movimiento(); // lo que devuelve LoteEntityUtils
        base.setLote(lote);
        base.setObservaciones("será sobreescrito");

        try (MockedStatic<MovimientoEntityUtils> ms = mockStatic(MovimientoEntityUtils.class)) {
            ms.when(() -> MovimientoEntityUtils.createMovimientoModificacion(dto, lote)).thenReturn(base);
            // el repo devuelve lo que le pasan (más simple para asserts)
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenAnswer(inv -> inv.getArgument(0, Movimiento.class));

            // when
            Movimiento out = dictamenCuarentenaService.persistirMovimientoCuarentenaPorAnalisis(
                dto,
                lote,
                nroAnalisisParam);

            // then: se guardó el MISMO objeto creado por LoteEntityUtils
            ArgumentCaptor<Movimiento> cap = ArgumentCaptor.forClass(Movimiento.class);
            verify(movimientoRepository).save(cap.capture());
            Movimiento saved = cap.getValue();

            assertSame(base, saved);
            assertSame(saved, out);

            assertEquals(MotivoEnum.ANALISIS, saved.getMotivo());
            assertEquals(DictamenEnum.APROBADO, saved.getDictamenInicial());
            assertEquals(DictamenEnum.CUARENTENA, saved.getDictamenFinal());
            assertEquals("AN-123", saved.getNroAnalisis());
            assertEquals("_CU2_\nObs de prueba", saved.getObservaciones());
            assertSame(lote, saved.getLote());

            ms.verify(() -> MovimientoEntityUtils.createMovimientoModificacion(dto, lote));
            verifyNoMoreInteractions(movimientoRepository);
        }
    }

    @Test
    @DisplayName("Usa nroAnalisis del parámetro (no del DTO) y observaciones null")
    void persistirMovimientoCuarentenaPorAnalisis_overrideNroAnalisis_yObsNull() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setObservaciones(null);
        dto.setNroAnalisis("DTO-777"); // NO debe usarse

        Lote lote = new Lote();
        lote.setDictamen(DictamenEnum.RECIBIDO);

        String nroAnalisisParam = "PARAM-888";

        Movimiento base = new Movimiento();
        base.setLote(lote);

        try (MockedStatic<MovimientoEntityUtils> ms = mockStatic(MovimientoEntityUtils.class)) {
            ms.when(() -> MovimientoEntityUtils.createMovimientoModificacion(dto, lote)).thenReturn(base);
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenAnswer(inv -> inv.getArgument(0, Movimiento.class));

            // when
            Movimiento out = dictamenCuarentenaService.persistirMovimientoCuarentenaPorAnalisis(
                dto,
                lote,
                nroAnalisisParam);

            // then
            verify(movimientoRepository).save(out);
            ms.verify(() -> MovimientoEntityUtils.createMovimientoModificacion(dto, lote));

            assertEquals(MotivoEnum.ANALISIS, out.getMotivo());
            assertEquals(DictamenEnum.RECIBIDO, out.getDictamenInicial());
            assertEquals(DictamenEnum.CUARENTENA, out.getDictamenFinal());
            // gana el parámetro, no el dto
            assertEquals("PARAM-888", out.getNroAnalisis());
            // concatena literal con null
            assertEquals("_CU2_\nnull", out.getObservaciones());
        }
    }


}