package com.mb.securo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.mb.securo.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoteRequestDTO {

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @PastOrPresent(message = "La fecha de ingreso no puede ser futura")
    private LocalDate fechaIngreso;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @NotNull(message = "El ID del proveedor es obligatorio")
    private Long proveedorId;

    // El fabricante puede ser opcional (si no es Conifarma)
    private Long fabricanteId;

    @NotNull(message = "La unidad de Medida es obligatoria")
    private UnidadMedidaEnum unidadMedida;

    @NotNull(message = "La cantidad inicial es obligatoria")
    @Positive(message = "La cantidad inicial debe ser mayor a cero")
    private BigDecimal cantidadInicial;

    @NotNull(message = "El número de bulto es obligatorio")
    @Positive(message = "El número de bulto debe ser mayor a cero")
    private Integer nroBulto;

    @NotNull(message = "La cantidad de bultos totales es obligatoria")
    @Positive(message = "La cantidad de bultos totales debe ser mayor a cero")
    private Integer bultosTotales;

    @Size(max = 30, message = "El número de remito no debe superar 30 caracteres")
    private String nroRemito;

    // Otros campos opcionales:
    private String loteProveedor;
    private String ordenElaboracion;
    private String detalleConservacion;
    private String analisisProveedor;

    // Opcionales: fechas, valoraciones, observaciones, etc.
    private LocalDate fechaVencimiento;
    private LocalDate fechaReanalisis;

    private String observaciones;
}
