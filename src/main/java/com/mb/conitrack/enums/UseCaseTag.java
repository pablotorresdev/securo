package com.mb.conitrack.enums;

/**
 * Enumeration of use case tags for audit trail tracking in movement observations.
 * <p>
 * Each tag corresponds to a specific business use case (CU) in the Conitrack system.
 * Tags are automatically prepended to movement observations to enable traceability
 * and auditing of which use case generated each movement.
 * </p>
 * <p>
 * Format in observations: {tag}\n{user observations}
 * Example: "_CU1_\nPurchase from supplier ABC"
 * </p>
 *
 * @see com.mb.conitrack.entity.Movimiento
 */
public enum UseCaseTag {

    /**
     * CU1 - Alta Ingreso Compra: Purchase intake from external suppliers.
     */
    CU1("_CU1_"),

    /**
     * CU3 - Baja Muestreo: Sampling operations for quality analysis.
     */
    CU3("_CU3_"),

    /**
     * CU4 - Baja Devolución Compra: Return to supplier (defective products).
     */
    CU4("_CU4_"),

    /**
     * CU7 - Baja Consumo Producción: Raw material consumption in production.
     */
    CU7("_CU7_"),

    /**
     * CU20 - Alta Ingreso Producción: Finished goods from internal production.
     */
    CU20("_CU20_"),

    /**
     * CU22 - Baja Venta: Sales to customers.
     */
    CU22("_CU22_"),

    /**
     * CU23 - Alta Devolución Venta: Returns from customers.
     */
    CU23("_CU23_"),

    /**
     * CU24 - Retiro Mercado (Alta/Modif): Market recalls (returns or status change).
     */
    CU24("_CU24_"),

    /**
     * CU25 - Baja Ajuste Stock: Inventory adjustments (corrections, losses).
     */
    CU25("_CU25_");

    private final String tag;

    /**
     * Constructor for use case tag.
     *
     * @param tag the string representation of the tag (e.g., "_CU1_")
     */
    UseCaseTag(String tag) {
        this.tag = tag;
    }

    /**
     * Gets the string representation of the tag.
     *
     * @return the tag string (e.g., "_CU1_")
     */
    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return tag;
    }

}
