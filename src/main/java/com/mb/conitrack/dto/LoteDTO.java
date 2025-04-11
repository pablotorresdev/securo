package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoteDTO {

    //Dato del back
    protected LocalDateTime fechaYHoraCreacion;

    //Datos de ALTA
    // Obligatorios
    @NotNull(message = "La fecha de ingreso es obligatoria", groups = {ValidacionAlta.class})
    @PastOrPresent(message = "La fecha de ingreso no puede ser futura", groups = {ValidacionAlta.class})
    private LocalDate fechaIngreso;

    @NotNull(message = "El ID del producto es obligatorio", groups = {ValidacionAlta.class})
    private Long productoId;

    @NotNull(message = "La cantidad inicial es obligatoria", groups = {ValidacionAlta.class})
    @Positive(message = "La cantidad inicial debe ser mayor a cero", groups = {ValidacionAlta.class})
    private BigDecimal cantidadInicial;

    @NotNull(message = "La unidad de Medida es obligatoria", groups = {ValidacionAlta.class})
    private UnidadMedidaEnum unidadMedida;

    @NotNull(message = "La cantidad de bultos totales es obligatoria", groups = {ValidacionAlta.class})
    @Positive(message = "La cantidad de bultos totales debe ser mayor a cero", groups = {ValidacionAlta.class})
    private Integer bultosTotales;

    @NotNull(message = "El ID del proveedor es obligatorio", groups = {ValidacionAlta.class})
    private Long proveedorId;

    @NotNull(message = "El lote del proveedor es obligatorio", groups = {ValidacionAlta.class})
    private String loteProveedor;

    //Opcionales
    @Size(max = 30, message = "El número de remito no debe superar 30 caracteres", groups = {ValidacionAlta.class})
    private String nroRemito;
    private Long fabricanteId;
    private String paisOrigen;
    @Future(message = "La fecha de ingreso de ser futura", groups = {ValidacionAlta.class})
    private LocalDate fechaReanalisisProveedor;
    @Future(message = "La fecha de ingreso de ser futura", groups = {ValidacionAlta.class})
    private LocalDate fechaVencimientoProveedor;
    private String detalleConservacion;


    //Datos de BAJA
    // Obligatorios
    @NotNull(message = "La fecha de consumo es obligatoria", groups = {ValidacionBaja.class})
    @PastOrPresent(message = "La fecha de consumo no puede ser futura", groups = {ValidacionBaja.class})
    private LocalDate fechaEgreso;

    @NotNull(message = "La orden de producción obligatoria", groups = {ValidacionBaja.class})
    private String ordenProduccion;

    protected String observaciones;

    //Segun nro de bultos
    protected List<BigDecimal> cantidadesBultos = new ArrayList<>();
    protected List<UnidadMedidaEnum> unidadMedidaBultos = new ArrayList<>();

    //Datos de salida
    protected String codigoInterno;
    protected String nombreProducto;
    protected String codigoProducto;
    protected TipoProductoEnum tipoProducto;
    protected String productoDestino;
    protected String nombreProveedor;
    protected String nombreFabricante;
    protected DictamenEnum dictamen;
    protected Long loteOrigenId;
    protected String estadoLote;

    // NUEVO: Lista de cantidades para cada bulto (en el paso 2)
    protected Integer nroBulto;

    protected List<MovimientoDTO> movimientoDTOs = new ArrayList<>();

    protected List<AnalisisDTO> analisisDTOs = new ArrayList<>();

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
        if(currentAnalisisDto== null) {
            return null;
        }
        return currentAnalisisDto.getNroAnalisis();
    }

}
