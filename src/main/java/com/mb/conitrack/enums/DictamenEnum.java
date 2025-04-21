package com.mb.conitrack.enums;

public enum DictamenEnum {
    RECIBIDO("Recibido"),
    CUARENTENA("Cuarentena"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado"),
    ANALISIS_EXPIRADO("Analisis expirado"),
    VENCIDO("Vencido"),
    LIBERADO("Liberado"),
    DEVOLUCION_CLIENTES("Devolucion clientes"),
    RETIRO_MERCADO("Retiro mercado");

    private final String valor;

    DictamenEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
