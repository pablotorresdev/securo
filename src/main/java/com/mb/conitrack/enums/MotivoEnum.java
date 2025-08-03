package com.mb.conitrack.enums;

public enum MotivoEnum {
    COMPRA("Compra"),
    MUESTREO("Muestreo"),
    DEVOLUCION_COMPRA("Devolucion compra"),
    ANALISIS("Analisis"),
    CONSUMO_PRODUCCION("Consumo produccion"),
    PRODUCCION_PROPIA("Produccion propia"),
    LIBERACION("Liberacion"),
    VENTA("Venta"),
    EXPIRACION_ANALISIS("Expiracion analisis"),
    VENCIMIENTO("Vencimiento"),
    DEVOLUCION_VENTA("Devolucion venta"),
    AJUSTE("Ajuste");

    private final String valor;

    MotivoEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
