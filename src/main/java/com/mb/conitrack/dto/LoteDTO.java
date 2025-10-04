package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mb.conitrack.dto.validation.AltaCompra;
import com.mb.conitrack.dto.validation.AltaProduccion;
import com.mb.conitrack.dto.validation.BajaProduccion;
import com.mb.conitrack.dto.validation.ValidacionBaja;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.mb.conitrack.enums.TipoProductoEnum.UNIDAD_VENTA;

@Data
public class LoteDTO {

    //Dato del back
    protected OffsetDateTime fechaYHoraCreacion;

    @NotNull(message = "Debe seleccionar un lote", groups = { BajaProduccion.class })
    protected String codigoLote;

    //Datos de ALTA obligatorios
    @NotNull(message = "La fecha de ingreso es obligatoria", groups = { AltaCompra.class, AltaProduccion.class })
    @PastOrPresent(message = "La fecha de ingreso no puede ser futura", groups = {
        AltaCompra.class,
        AltaProduccion.class
    })
    private LocalDate fechaIngreso;

    @NotNull(message = "El producto es obligatorio", groups = { AltaCompra.class, AltaProduccion.class })
    private Long productoId;

    @NotNull(message = "La cantidad inicial es obligatoria", groups = { AltaCompra.class, AltaProduccion.class })
    @Positive(message = "La cantidad inicial debe ser mayor a cero", groups = {
        AltaCompra.class,
        AltaProduccion.class
    })
    private BigDecimal cantidadInicial;

    @NotNull(message = "La unidad de medida es obligatoria", groups = { AltaCompra.class, AltaProduccion.class })
    private UnidadMedidaEnum unidadMedida;

    @NotNull(message = "La cantidad de bultos totales es obligatoria", groups = {
        AltaCompra.class,
        AltaProduccion.class
    })
    @Positive(message = "La cantidad de bultos totales debe ser mayor a cero", groups = {
        AltaCompra.class,
        AltaProduccion.class
    })
    private Integer bultosTotales;

    @NotNull(message = "El proveedor es obligatorio", groups = { AltaCompra.class })
    private Long proveedorId;

    @NotNull(message = "El lote del proveedor es obligatorio", groups = { AltaCompra.class, AltaProduccion.class })
    private String loteProveedor;

    //Datos de ALTA Opcionales
    @Size(max = 30, message = "El número de remito no debe superar 30 caracteres", groups = { AltaCompra.class })
    private String nroRemito;

    private Long fabricanteId;

    private String paisOrigen;

    @Future(message = "La fecha de reanalisis del proveedor debe ser futura", groups = { AltaCompra.class })
    private LocalDate fechaReanalisisProveedor;

    @Future(message = "La fecha de vencimiento del proveedor debe ser futura", groups = { AltaCompra.class })
    private LocalDate fechaVencimientoProveedor;

    private String detalleConservacion;

    //Datos de BAJA Obligatorios
    @NotNull(message = "La fecha de consumo es obligatoria", groups = { ValidacionBaja.class, BajaProduccion.class })
    @PastOrPresent(message = "La fecha de consumo no puede ser futura", groups = {
        ValidacionBaja.class,
        BajaProduccion.class
    })
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

    //Esto junto con cantidad de unidades total, dara el rango de traza para ese lote
    protected Long trazaInicial;

    protected List<BultoDTO> bultosDTOs = new ArrayList<>();

    protected List<MovimientoDTO> movimientoDTOs = new ArrayList<>();

    protected List<AnalisisDTO> analisisDTOs = new ArrayList<>();

    protected List<TrazaDTO> trazaDTOs = new ArrayList<>();

    //Datos derivados
    protected String nombreProducto;

    protected String codigoProducto;

    protected TipoProductoEnum tipoProducto;

    protected String productoDestino;

    protected String nombreProveedor;

    protected String nombreFabricante;

    protected DictamenEnum dictamen;

    protected EstadoEnum estado;


    //********************Utils********************//
    public boolean esUnidadVenta() {
        return tipoProducto == UNIDAD_VENTA;
    }

    public AnalisisDTO getUltimoAnalisisDto() {
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

    public String getNroUltimoAnalisisDto() {
        final AnalisisDTO currentAnalisisDto = getUltimoAnalisisDto();
        if (currentAnalisisDto == null) {
            return null;
        }
        return currentAnalisisDto.getNroAnalisis();
    }

    public String getNroAnalisisDtoEnCurso() {
        return this.analisisDTOs.stream()
            .filter(analisis -> analisis.getDictamen() == null && analisis.getFechaRealizado() == null)
            .map(AnalisisDTO::getNroAnalisis)
            .findFirst()
            .orElse(null);
    }

    public Long getTrazaFinal() {
        if (this.trazaInicial == null) {
            return null;
        }
        return this.trazaInicial + this.cantidadInicial.longValueExact() - 1;
    }

    public LocalDate getFechaVencimientoVigente() {
        final List<AnalisisDTO> list = this.analisisDTOs.stream().filter(a -> a.getDictamen() != null)
            .filter(a -> a.getCodigoLote().equals(this.codigoLote)).filter(a -> a.getFechaVencimiento() != null).toList();
        if (list.isEmpty()) {
            return fechaVencimientoProveedor;
        } else if (list.size() == 1) {
            LocalDate fechaAnalisis = list.get(0).getFechaVencimiento();
            if (fechaAnalisis == null || fechaVencimientoProveedor == null) {
                return fechaAnalisis != null ? fechaAnalisis : fechaVencimientoProveedor;
            }
            LocalDate hoy = LocalDate.now();
            long diffProveedor = Math.abs(ChronoUnit.DAYS.between(hoy, fechaVencimientoProveedor));
            long diffAnalisis = Math.abs(ChronoUnit.DAYS.between(hoy, fechaAnalisis));
            return diffAnalisis <= diffProveedor ? fechaAnalisis : fechaVencimientoProveedor;
        } else {
            throw new IllegalStateException("Hay más de un análisis activo con fecha de vencimiento");
        }
    }

    public LocalDate getFechaReanalisisVigente() {
        AnalisisDTO analisis = this.analisisDTOs.stream()
            .filter(a -> a.getDictamen() != null)
            .filter(a -> a.getFechaReanalisis() != null)
            .min(Comparator.comparing(AnalisisDTO::getFechaReanalisis))
            .orElse(null);
        if (analisis == null) {
            return fechaReanalisisProveedor;
        } else {
            LocalDate fechaAnalisis = analisis.getFechaReanalisis();
            if (fechaAnalisis == null || fechaReanalisisProveedor == null) {
                return fechaAnalisis != null ? fechaAnalisis : fechaReanalisisProveedor;
            }
            LocalDate hoy = LocalDate.now();
            long diffProveedor = Math.abs(ChronoUnit.DAYS.between(hoy, fechaReanalisisProveedor));
            long diffAnalisis = Math.abs(ChronoUnit.DAYS.between(hoy, fechaAnalisis));
            return diffAnalisis <= diffProveedor ? fechaAnalisis : fechaReanalisisProveedor;
        }
    }

    public Long getDiasHastaFechaReanalisisVigente() {
        LocalDate fecha = getFechaReanalisisVigente();
        return fecha != null ? ChronoUnit.DAYS.between(LocalDate.now(), fecha) : null;
    }

    public Long getDiasHastaFechaVencimientoVigente() {
        LocalDate fecha = getFechaVencimientoVigente();
        return fecha != null ? ChronoUnit.DAYS.between(LocalDate.now(), fecha) : null;
    }

    public AnalisisDTO getUltimoAnalisisDtoDictaminado() {
        return this.analisisDTOs.stream()
            .filter(a -> a.getDictamen() != null)
            .max(Comparator.comparing(AnalisisDTO::getFechaYHoraCreacion))
            .orElse(null);
    }

    public String getUltimoNroAnalisisDto() {
        final AnalisisDTO currentAnalisis = getUltimoAnalisisDto();
        if (currentAnalisis == null) {
            return null;
        }
        return currentAnalisis.getNroAnalisis();
    }

}
