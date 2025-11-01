package com.mb.conitrack.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.mb.conitrack.enums.UseCaseTag;

/** Utilidades comunes para creación de códigos y timestamps de movimientos. */
public class MovimientoCommonUtils {

    public static final String TIMESTAMP_PATTERN = "yy.MM.dd_HH.mm.ss";
    public static final String CODE_SEPARATOR = "-";
    public static final String BULTO_PREFIX = "-B_";

    private MovimientoCommonUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /** Genera código único de movimiento: {codigoLote}-{timestamp}. */
    static String generateMovimientoCode(final String codigoLote, final OffsetDateTime timestamp) {
        Objects.requireNonNull(codigoLote, "codigoLote cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        return codigoLote + CODE_SEPARATOR + formatTimestamp(timestamp);
    }

    /** Genera código de movimiento específico de bulto: {codigoLote}-B_{nroBulto}-{timestamp}. */
    static String generateMovimientoCodeForBulto(
        final String codigoLote,
        final int nroBulto,
        final OffsetDateTime timestamp) {
        Objects.requireNonNull(codigoLote, "codigoLote cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        if (nroBulto <= 0) {
            throw new IllegalArgumentException("nroBulto must be positive, got: " + nroBulto);
        }
        return codigoLote + BULTO_PREFIX + nroBulto + CODE_SEPARATOR + formatTimestamp(timestamp);
    }

    /** Formatea timestamp según patrón estándar de códigos de movimiento. */
    static String formatTimestamp(final OffsetDateTime timestamp) {
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        return timestamp.format(DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN));
    }

    /** Formatea observaciones con prefijo de tag CU para auditoría. */
    static String formatObservacionesWithCU(final UseCaseTag useCaseTag, final String observaciones) {
        Objects.requireNonNull(useCaseTag, "useCaseTag cannot be null");
        return useCaseTag.getTag() + "\n" + (observaciones != null ? observaciones : "");
    }

}
