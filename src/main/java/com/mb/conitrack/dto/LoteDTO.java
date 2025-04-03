package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
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

    public static LoteDTO fromEntities(List<Lote> entities) {
        if (entities == null || entities.isEmpty()) {
            return null;
        }

        LoteDTO dto = new LoteDTO();
        boolean firstCase = true;
        for (Lote entity : entities) {
            if (firstCase) {
                if (entity.getProducto() != null) {
                    final Producto producto = entity.getProducto();
                    dto.setProductoId(producto.getId());
                    dto.setNombreProducto(producto.getNombreGenerico());
                    dto.setCodigoProducto(producto.getCodigoInterno());
                    dto.setTipoProducto(producto.getTipoProducto());
                    dto.setProductoDestino(producto.getProductoDestino() != null ? producto.getProductoDestino().getNombreGenerico() : null);
                }
                dto.setProveedorId(entity.getProveedor() != null ? entity.getProveedor().getId() : null);
                dto.setNombreProveedor(entity.getProveedor() != null ? entity.getProveedor().getRazonSocial() : null);
                dto.setFechaIngreso(entity.getFechaIngreso());
                dto.setFechaYHoraCreacion(entity.getFechaYHoraCreacion());
                dto.setBultosTotales(entity.getBultosTotales());

                //Del 1ero solo para que no falle por null validation
                dto.setCantidadInicial(entity.getCantidadInicial());
                dto.setUnidadMedida(entity.getUnidadMedida());

                dto.setLoteProveedor(entity.getLoteProveedor());

                dto.setNroRemito(entity.getNroRemito());
                dto.setDetalleConservacion(entity.getDetalleConservacion());
                dto.setFechaVencimiento(entity.getFechaVencimiento());
                dto.setFechaReanalisis(entity.getFechaReanalisis());
                dto.setTitulo(entity.getTitulo());
                dto.setObservaciones(entity.getObservaciones());

                dto.getCantidadesBultos().add(entity.getCantidadInicial());
                dto.getUnidadMedidaBultos().add(entity.getUnidadMedida());

                addMovimientosDTO(dto, entity);
                addAnalisisDTO(dto, entity);

                firstCase = false;
            } else {
                dto.getCantidadesBultos().add(entity.getNroBulto() - 1, entity.getCantidadInicial());
                dto.getUnidadMedidaBultos().add(entity.getNroBulto() - 1, entity.getUnidadMedida());

                addMovimientosDTO(dto, entity);
                addAnalisisDTO(dto, entity);
            }
        }
        return dto;
    }

    private static void addMovimientosDTO(final LoteDTO dto, final Lote entity) {
        for (Movimiento movimiento : entity.getMovimientos()) {
            final MovimientoDTO movimientoDTO = MovimientoDTO.fromEntity(movimiento);
            movimientoDTO.setNroBulto(String.valueOf(entity.getNroBulto()));
            dto.getMovimientoDTOs().add(movimientoDTO);
        }
    }

    private static void addAnalisisDTO(final LoteDTO dto, final Lote entity) {
        for (Analisis analisis : entity.getAnalisisList()) {
            dto.getAnalisisDTOs().add(AnalisisDTO.fromEntity(analisis));
        }
    }

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    private LocalDateTime fechaYHoraCreacion;

    private String nombreProducto;

    private String codigoProducto;

    private TipoProductoEnum tipoProducto;

    private String productoDestino;

    @NotNull(message = "El ID del proveedor es obligatorio")
    private Long proveedorId;

    private String nombreProveedor;

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

    //***********Opcionales***********//
    @Size(max = 30, message = "El número de remito no debe superar 30 caracteres")
    private String nroRemito;

    private String detalleConservacion;

    private LocalDate fechaVencimiento;

    private LocalDate fechaReanalisis;

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
