package com.mb.conitrack.enums;

public enum TipoProductoEnum {
    API("Api", "MatPrima", 1, true),
    EXCIPIENTE("Excipiente", "MatPrima",  1,false),

    ACOND_PRIMARIO("Acond. primario", "MatAcond", 2, false),
    ACOND_SECUNDARIO("Acond. secundario", "MatAcond", 2, true),

    SEMIELABORADO("Semielaborado", "SemiElab", 9,true),
    GRANEL_MEZCLA_POLVO("Granel mezcla de polvo", "SemiElab", 9, true),
    GRANEL_CAPSULAS("Granel de cápsulas", "SemiElab", 9, true),
    GRANEL_COMPRIMIDOS("Granel de comprimidos", "SemiElab", 9, true),
    GRANEL_FRASCOS("Granel frascos", "SemiElab", 9, true),

    UNIDAD_VENTA("Unidad venta", "UniVenta", 9,false);

    private final String valor;

    private final String grupo;

    private int codigo;

    private final boolean requiereProductoDestino;

    TipoProductoEnum(String valor, String grupo, int codigo, boolean productoDestino) {
        this.valor = valor;
        this.grupo = grupo;
        this.codigo = codigo;
        this.requiereProductoDestino = productoDestino;
    }

    public String getValor() {
        return valor;
    }

    public String getGrupo() {
        return grupo;
    }

    public int getCodigo() {
        return codigo;
    }

    public boolean requiereProductoDestino() {
        return requiereProductoDestino;
    }

    public boolean esSemiElaborado() {
        return "SemiElab".equals(this.grupo);
    }
}
