package com.mb.conitrack.enums;

public enum TipoProductoEnum {
    API("Api", "MP", true),
    EXCIPIENTE("Excipiente", "MP", false),
    SEMIELABORADO("Semielaborado", "UP", true),
    ACOND_PRIMARIO("Acond. primario", "MP", false),
    ACOND_SECUNDARIO("Acond. secundario", "MP", true),
    UNIDAD_VENTA("Unidad venta", "UV", false);

    private final String valor;

    private final String grupo;

    private final boolean requiereProductoDestino;

    TipoProductoEnum(String valor, String grupo, boolean productoDestino) {
        this.valor = valor;
        this.grupo = grupo;
        this.requiereProductoDestino = productoDestino;
    }

    public String getValor() {
        return valor;
    }

    public String getGrupo() {
        return grupo;
    }

    public boolean requiereProductoDestino() {
        return requiereProductoDestino;
    }
}
