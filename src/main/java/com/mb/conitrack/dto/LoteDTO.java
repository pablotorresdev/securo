package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mb.conitrack.dto.validation.AltaCompra;
import com.mb.conitrack.dto.validation.AltaProduccion;
import com.mb.conitrack.dto.validation.BajaProduccion;
import com.mb.conitrack.dto.validation.ValidacionBaja;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoteDTO {

    //Dato del back
    protected LocalDateTime fechaYHoraCreacion;

    @NotNull(message = "Debe seleccionar un lote", groups = { BajaProduccion.class })
    protected String codigoInterno;

    //Datos de ALTA obligatorios
    @NotNull(message = "La fecha de ingreso es obligatoria", groups = { AltaCompra.class, AltaProduccion.class  })
    @PastOrPresent(message = "La fecha de ingreso no puede ser futura", groups = { AltaCompra.class })
    private LocalDate fechaIngreso;

    @NotNull(message = "El ID del producto es obligatorio", groups = { AltaCompra.class, AltaProduccion.class })
    private Long productoId;

    @NotNull(message = "La cantidad inicial es obligatoria", groups = { AltaCompra.class, AltaProduccion.class })
    @Positive(message = "La cantidad inicial debe ser mayor a cero", groups = { AltaCompra.class, AltaProduccion.class })
    private BigDecimal cantidadInicial;

    @NotNull(message = "La unidad de Medida es obligatoria", groups = { AltaCompra.class, AltaProduccion.class })
    private UnidadMedidaEnum unidadMedida;

    @NotNull(message = "La cantidad de bultos totales es obligatoria", groups = { AltaCompra.class, AltaProduccion.class })
    @Positive(message = "La cantidad de bultos totales debe ser mayor a cero", groups = { AltaCompra.class, AltaProduccion.class })
    private Integer bultosTotales;

    @NotNull(message = "El ID del proveedor es obligatorio", groups = { AltaCompra.class })
    private Long proveedorId;

    @NotNull(message = "El lote del proveedor es obligatorio", groups = { AltaCompra.class, AltaProduccion.class })
    private String loteProveedor;

    //Datos de ALTA Opcionales
    @Size(max = 30, message = "El número de remito no debe superar 30 caracteres", groups = { AltaCompra.class })
    private String nroRemito;

    private Long fabricanteId;

    private String paisOrigen;

    @Future(message = "La fecha de ingreso de ser futura", groups = { AltaCompra.class })
    private LocalDate fechaReanalisisProveedor;

    @Future(message = "La fecha de ingreso de ser futura", groups = { AltaCompra.class })
    private LocalDate fechaVencimientoProveedor;

    private String detalleConservacion;

    //Datos de BAJA Obligatorios
    @NotNull(message = "La fecha de consumo es obligatoria", groups = { ValidacionBaja.class, BajaProduccion.class })
    @PastOrPresent(message = "La fecha de consumo no puede ser futura", groups = { ValidacionBaja.class, BajaProduccion.class })
    private LocalDate fechaEgreso;

    @NotNull(message = "La orden de producción es obligatoria", groups = { BajaProduccion.class, AltaProduccion.class })
    private String ordenProduccion;

    protected String observaciones;

    protected Long loteOrigenId;

    //Identificadores individuales de bultos y cantidades
    protected List<Integer> nroBultoList = new ArrayList<>();
    protected List<BigDecimal> cantidadesBultos = new ArrayList<>();
    protected List<UnidadMedidaEnum> unidadMedidaBultos = new ArrayList<>();

    @PositiveOrZero(message = "La cantidad no puede ser negativa")
    private BigDecimal cantidadActual;
    private Integer bultosActuales;


    //Esto junto con cantidad de unidades total, dara el rango de traza para ese lote
    protected Long trazaInicial;

    // TODO: REMOVER
    protected Integer nroBulto;

    protected List<MovimientoDTO> movimientoDTOs = new ArrayList<>();
    protected List<AnalisisDTO> analisisDTOs = new ArrayList<>();

    //Datos derivados
    protected String nombreProducto;
    protected String codigoProducto;
    protected TipoProductoEnum tipoProducto;
    protected String productoDestino;
    protected String nombreProveedor;
    protected String nombreFabricante;
    protected DictamenEnum dictamen;
    protected String estado;

    //********************Utils********************//
    public AnalisisDTO getCurrentAnalisisDto() {
        if (this.analisisDTOs.isEmpty()) {
            return null;
        } else if (this.analisisDTOs.size() == 1) {
            return this.analisisDTOs.get(0);
        } else {
            return this.analisisDTOs.stream()
                .max(Comparator.comparing(AnalisisDTO::getFechaYHoraCreacion))
                .orElse(null);
        }
    }

    public String getCurrentNroAnalisis() {
        final AnalisisDTO currentAnalisisDto = getCurrentAnalisisDto();
        if (currentAnalisisDto == null) {
            return null;
        }
        return currentAnalisisDto.getNroAnalisis();
    }

}
