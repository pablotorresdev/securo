package com.mb.conitrack.enums;

public enum EstadoLoteEnum {
    NUEVO("Nuevo"),
    DISPONIBLE ("Disponible"),
    CONSUMIDO("Consumido"),
    INHABILITADO ("Inhabilitado"),
    DESCARTADO ("Descartado");

    private final String valor;

    EstadoLoteEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
