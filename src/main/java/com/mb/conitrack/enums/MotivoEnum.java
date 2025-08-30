package com.mb.conitrack.enums;

public enum MotivoEnum {

    COMPRA("Compra"),//ALTA
    MUESTREO("Muestreo"),//BAJA
    DEVOLUCION_COMPRA("Devolucion compra"), //BAJA
    ANALISIS("Analisis"), //MODIFICACION
    RESULTADO_ANALISIS("Resultado analisis"), //MODIFICACION
    CONSUMO_PRODUCCION("Consumo produccion"), //BAJA
    PRODUCCION_PROPIA("Produccion propia"), //ALTA
    LIBERACION("Liberacion"), //MODIFICACION
    VENTA("Venta"), //BAJA
    EXPIRACION_ANALISIS("Expiracion analisis"), //MODIF SHCEDULLED
    VENCIMIENTO("Vencimiento"), //MODIFIC SHCEDULLED
    DEVOLUCION_VENTA("Devolucion venta"), // ALLA / MODIF
    RETIRO_MERCADO("Retiro mercado"), // ALTA / MODIF
    AJUSTE("Ajuste"); // BAJA/MODIFICACION

    private final String valor;

    MotivoEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
