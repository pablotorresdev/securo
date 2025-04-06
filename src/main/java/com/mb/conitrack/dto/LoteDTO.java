package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoteDTO {

    //TODO: Backup -> https://help.heroku.com/sharing/fda99731-8765-4b5d-b60e-8150fce3ddcf

    private LocalDateTime fechaYHoraCreacion;

    private String codigoInterno;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;
    private String nombreProducto;
    private String codigoProducto;
    private TipoProductoEnum tipoProducto;
    private String productoDestino;

    @NotNull(message = "El ID del proveedor es obligatorio")
    private Long proveedorId;
    private String nombreProveedor;

    private Long fabricanteId;
    private String nombreFabricante;

    private String paisOrigen;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @PastOrPresent(message = "La fecha de ingreso no puede ser futura")
    private LocalDate fechaIngreso;

    // NUEVO: Lista de cantidades para cada bulto (en el paso 2)
    private Integer nroBulto;

    @NotNull(message = "La cantidad de bultos totales es obligatoria")
    @Positive(message = "La cantidad de bultos totales debe ser mayor a cero")
    private Integer bultosTotales;

    @NotNull(message = "La cantidad inicial es obligatoria")
    @Positive(message = "La cantidad inicial debe ser mayor a cero")
    private BigDecimal cantidadInicial;

    @NotNull(message = "La unidad de Medida es obligatoria")
    private UnidadMedidaEnum unidadMedida;

    @NotNull(message = "El lote del proveedor es obligatorio")
    private String loteProveedor;

    @Future(message = "La fecha de ingreso de ser futura")
    private LocalDate fechaReanalisisProveedor;

    @Future(message = "La fecha de ingreso de ser futura")
    private LocalDate fechaVencimientoProveedor;

    private String estadoLote;

    private DictamenEnum dictamen;

    private Long loteOrigenId;

    @Size(max = 30, message = "El número de remito no debe superar 30 caracteres")
    private String nroRemito;

    private String detalleConservacion;

    private String observaciones;

    private List<BigDecimal> cantidadesBultos = new ArrayList<>();

    private List<UnidadMedidaEnum> unidadMedidaBultos = new ArrayList<>();

    private List<MovimientoDTO> movimientoDTOs = new ArrayList<>();

    private List<AnalisisDTO> analisisDTOs = new ArrayList<>();

}
