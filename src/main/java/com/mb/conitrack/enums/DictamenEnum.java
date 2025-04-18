package com.mb.conitrack.enums;

public enum DictamenEnum {
    RECIBIDO("Recibido"),
    CUARENTENA("Cuarentena"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado"),
    VENCIDO("Vencido"),
    ANALISIS_EXPIRADO("Analisis expirado"),
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
