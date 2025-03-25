package com.mb.conitrack.enums;

public enum TipoProductoEnum {
    API("Api", "MP"),
    EXCIPIENTE("Excipiente", "MP"),
    CAPSULA("Capsula", "MP"),
    SEMIELABORADO("Semielaborado", "UV"),
    ACOND_PRIMARIO("Acond. primario", "MP"),
    ACOND_SECUNDARIO("Acond. secundario", "MP"),
    UNIDAD_VENTA("Unidad venta", "UV");

    private final String valor;

    private final String grupo;

    TipoProductoEnum(String valor, String grupo) {
        this.valor = valor;
        this.grupo = grupo;
    }

    public String getValor() {
        return valor;
    }

    public String getGrupo() {
        return grupo;
    }
}
