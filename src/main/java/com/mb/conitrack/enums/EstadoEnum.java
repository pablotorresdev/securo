package com.mb.conitrack.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enum que representa los estados de un lote/bulto/traza respecto de su cantidad y los movimientos de Alta/Baja relacionados.
 */
public enum EstadoEnum {
    NUEVO("Nuevo", 0), //Alta -> Cant inicial = actual
    EN_USO("En uso", 1), //Baja -> Cant inicial > actual
    CONSUMIDO("Consumido", 2), //Baja -> Cant actul = 0 x Producción
    VENDIDO("Vendido", 2), //Baja -> Cant actul = 0 x Venta
    DEVUELTO("Devuelto", 2), //Baja -> Cant actul = 0 x Devolución
    DESCARTADO("Descartado", 2);  //Baja -> Cant actul = 0 x Destrucción

    // Obtener el primero que coincida con el valor
    public static Optional<EstadoEnum> fromValor(String valor) {
        return Arrays.stream(values())
            .filter(e -> e.valor.equalsIgnoreCase(valor))
            .findFirst();
    }

    private final String valor;

    private final int prioridad;

    EstadoEnum(String valor, int prioridad) {
        this.valor = valor;
        this.prioridad = prioridad;
    }

    public String getValor() {
        return valor;
    }

    public int getPrioridad() {
        return prioridad;
    }
}
