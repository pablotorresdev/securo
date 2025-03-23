package com.mb.securo.enums;

public enum DictamenEnum {
    RECIBIDO("Recibido"),
    CUARENTENA("Cuarentena"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado"),
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
