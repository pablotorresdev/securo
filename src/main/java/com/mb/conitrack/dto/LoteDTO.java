package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoteDTO {

    //TODO: Backup -> https://help.heroku.com/sharing/fda99731-8765-4b5d-b60e-8150fce3ddcf

    private LocalDateTime fechaYHoraCreacion;

    @NotNull(message = "El ID del proveedor es obligatorio")
    private Long proveedorId;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @PastOrPresent(message = "La fecha de ingreso no puede ser futura")
    private LocalDate fechaIngreso;

    @Positive(message = "La cantidad de bultos totales debe ser mayor a cero")
    private Integer bultosTotales;

    @NotNull(message = "La cantidad inicial es obligatoria")
    @Positive(message = "La cantidad inicial debe ser mayor a cero")
    private BigDecimal cantidadInicial;

    @NotNull(message = "La unidad de Medida es obligatoria")
    private UnidadMedidaEnum unidadMedida;

    @NotNull(message = "El lote del proveedor es obligatorio")
    private String loteProveedor;

    //Datos internos
    private String nombreProducto;

    private String codigoProducto;

    private TipoProductoEnum tipoProducto;

    private String productoDestino;

    private String nombreProveedor;

    //***********Opcionales***********//
    @Size(max = 30, message = "El número de remito no debe superar 30 caracteres")
    private String nroRemito;

    private String detalleConservacion;

    private LocalDate fechaAnalisis;

    private LocalDate fechaReanalisis;

    private LocalDate fechaVencimiento;

    @Max(value = 100, message = "El título no puede superar el 100%")
    private BigDecimal titulo;

    private String observaciones;

    // NUEVO: Lista de cantidades para cada bulto (en el paso 2)
    private Integer nroBulto;

    private List<BigDecimal> cantidadesBultos = new ArrayList<>();

    private List<UnidadMedidaEnum> unidadMedidaBultos = new ArrayList<>();

    private List<MovimientoDTO> movimientoDTOs = new ArrayList<>();

    private List<AnalisisDTO> analisisDTOs = new ArrayList<>();

}
