package com.mb.conitrack.enums;

/**
 * Enum que representa los estados de un lote/bulto/traza respecto de su cantidad y los movimientos de Alta/Baja relacionados.
 */
public enum EstadoEnum {
    NUEVO("Nuevo"), //Alta -> Cant inicial = actual
    EN_USO ("En uso"), //Baja -> Cant inicial > actual
    CONSUMIDO("Consumido"), //Baja -> Cant actul = 0 x Producción
    VENDIDO("Vendido"), //Baja -> Cant actul = 0 x Venta
    DEVUELTO ("Devuelto"), //Baja -> Cant actul = 0 x Devolución
    DESCARTADO ("Descartado");  //Baja -> Cant actul = 0 x Destrucción

    private final String valor;

    EstadoEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
