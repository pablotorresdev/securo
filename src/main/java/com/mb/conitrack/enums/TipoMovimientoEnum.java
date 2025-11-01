package com.mb.conitrack.enums;

/** Tipo de movimiento de stock (ingreso, egreso o cambio de estado). */
public enum TipoMovimientoEnum {
    ALTA("Alta"),
    BAJA("Baja"),
    MODIFICACION("Modificacion");

    private final String valor;

    TipoMovimientoEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
