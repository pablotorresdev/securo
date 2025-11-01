package com.mb.conitrack.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mb.conitrack.enums.UseCaseTag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MovimientoCommonUtilsTest {

    @Test
    @DisplayName("Constructor lanza UnsupportedOperationException")
    void constructor_lanzaExcepcion() throws Exception {
        Constructor<MovimientoCommonUtils> constructor = MovimientoCommonUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
        assertEquals("Utility class cannot be instantiated", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("generateMovimientoCode genera código con formato correcto")
    void generateMovimientoCode_generaFormatoEsperado() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 1, 15, 14, 30, 45, 0, ZoneOffset.UTC);
        String codigoLote = "LOT-123";

        // when
        String resultado = MovimientoCommonUtils.generateMovimientoCode(codigoLote, timestamp);

        // then
        assertEquals("LOT-123-25.01.15_14.30.45", resultado);
    }

    @Test
    @DisplayName("generateMovimientoCode lanza NullPointerException si codigoLote es null")
    void generateMovimientoCode_codigoLoteNull_lanzaExcepcion() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.now();

        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoCommonUtils.generateMovimientoCode(null, timestamp)
        );
    }

    @Test
    @DisplayName("generateMovimientoCode lanza NullPointerException si timestamp es null")
    void generateMovimientoCode_timestampNull_lanzaExcepcion() {
        // given
        String codigoLote = "LOT-123";

        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoCommonUtils.generateMovimientoCode(codigoLote, null)
        );
    }

    @Test
    @DisplayName("generateMovimientoCodeForBulto genera código con nro bulto correcto")
    void generateMovimientoCodeForBulto_generaFormatoEsperado() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 2, 20, 10, 15, 30, 0, ZoneOffset.UTC);
        String codigoLote = "LOT-ABC";
        int nroBulto = 5;

        // when
        String resultado = MovimientoCommonUtils.generateMovimientoCodeForBulto(codigoLote, nroBulto, timestamp);

        // then
        assertEquals("LOT-ABC-B_5-25.02.20_10.15.30", resultado);
    }

    @Test
    @DisplayName("generateMovimientoCodeForBulto lanza IllegalArgumentException si nroBulto es cero")
    void generateMovimientoCodeForBulto_nroBultoCero_lanzaExcepcion() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.now();
        String codigoLote = "LOT-123";

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
            MovimientoCommonUtils.generateMovimientoCodeForBulto(codigoLote, 0, timestamp)
        );
    }

    @Test
    @DisplayName("generateMovimientoCodeForBulto lanza IllegalArgumentException si nroBulto es negativo")
    void generateMovimientoCodeForBulto_nroBultoNegativo_lanzaExcepcion() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.now();
        String codigoLote = "LOT-123";

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
            MovimientoCommonUtils.generateMovimientoCodeForBulto(codigoLote, -1, timestamp)
        );
    }

    @Test
    @DisplayName("generateMovimientoCodeForBulto lanza NullPointerException si codigoLote es null")
    void generateMovimientoCodeForBulto_codigoLoteNull_lanzaExcepcion() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.now();

        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoCommonUtils.generateMovimientoCodeForBulto(null, 1, timestamp)
        );
    }

    @Test
    @DisplayName("generateMovimientoCodeForBulto lanza NullPointerException si timestamp es null")
    void generateMovimientoCodeForBulto_timestampNull_lanzaExcepcion() {
        // given
        String codigoLote = "LOT-123";

        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoCommonUtils.generateMovimientoCodeForBulto(codigoLote, 1, null)
        );
    }

    @Test
    @DisplayName("formatTimestamp formatea timestamp con patrón correcto")
    void formatTimestamp_generaFormatoEsperado() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC);

        // when
        String resultado = MovimientoCommonUtils.formatTimestamp(timestamp);

        // then
        assertEquals("25.12.31_23.59.59", resultado);
    }

    @Test
    @DisplayName("formatTimestamp lanza NullPointerException si timestamp es null")
    void formatTimestamp_timestampNull_lanzaExcepcion() {
        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoCommonUtils.formatTimestamp(null)
        );
    }

    @Test
    @DisplayName("formatObservacionesWithCU agrega tag al inicio con salto de línea")
    void formatObservacionesWithCU_agregaTagCorrectamente() {
        // given
        UseCaseTag tag = UseCaseTag.CU1;
        String observaciones = "Compra de proveedor XYZ";

        // when
        String resultado = MovimientoCommonUtils.formatObservacionesWithCU(tag, observaciones);

        // then
        assertEquals("_CU1_\nCompra de proveedor XYZ", resultado);
    }

    @Test
    @DisplayName("formatObservacionesWithCU maneja observaciones null como string vacío")
    void formatObservacionesWithCU_observacionesNull_retornaTagConVacio() {
        // given
        UseCaseTag tag = UseCaseTag.CU4;

        // when
        String resultado = MovimientoCommonUtils.formatObservacionesWithCU(tag, null);

        // then
        assertEquals("_CU4_\n", resultado);
    }

    @Test
    @DisplayName("formatObservacionesWithCU maneja observaciones vacías correctamente")
    void formatObservacionesWithCU_observacionesVacias_retornaTagConVacio() {
        // given
        UseCaseTag tag = UseCaseTag.CU7;
        String observaciones = "";

        // when
        String resultado = MovimientoCommonUtils.formatObservacionesWithCU(tag, observaciones);

        // then
        assertEquals("_CU7_\n", resultado);
    }

    @Test
    @DisplayName("formatObservacionesWithCU lanza NullPointerException si useCaseTag es null")
    void formatObservacionesWithCU_tagNull_lanzaExcepcion() {
        // given
        String observaciones = "Alguna observación";

        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoCommonUtils.formatObservacionesWithCU(null, observaciones)
        );
    }

}
