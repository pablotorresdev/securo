package com.mb.conitrack.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.repository.LoteRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryServiceLoteTest {

    @Mock
    LoteRepository loteRepository;

    @InjectMocks
    QueryServiceLote service;

    @Test
    @DisplayName("findAllSortByDate - filtra activos y ordena por fecha y código")
    void findAllSortByDate_ok() {
        // given: mezcla de activos e inactivos y desordenados
        Lote a = new Lote();
        a.setActivo(true);
        a.setFechaIngreso(LocalDate.of(2024, 1, 10));
        a.setCodigoInterno("L-02");

        Lote b = new Lote();
        b.setActivo(true);
        b.setFechaIngreso(LocalDate.of(2024, 1, 10)); // misma fecha que 'a'
        b.setCodigoInterno("L-01");                   // código anterior => debe ir antes que 'a'

        Lote c = new Lote();
        c.setActivo(true);
        c.setFechaIngreso(LocalDate.of(2023, 12, 31)); // fecha más vieja => primero
        c.setCodigoInterno("L-99");

        Lote d = new Lote();
        d.setActivo(false); // inactivo => debe ser filtrado
        d.setFechaIngreso(LocalDate.of(2025, 5, 5));
        d.setCodigoInterno("L-00");

        when(loteRepository.findAll()).thenReturn(Arrays.asList(a, b, c, d));

        // when
        List<Lote> out = service.findAllSortByDateAndCodigoInterno();

        // then: sólo activos (c, b, a) en orden por fecha/código
        assertIterableEquals(List.of(c, b, a), out); // <--- mejor que assertEquals para listas
        verify(loteRepository).findAll();
        verifyNoMoreInteractions(loteRepository);
    }

}