package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MovimientoDTO {

    public static MovimientoDTO fromEntity(Movimiento entity) {
        if (entity == null) {
            return null;
        }

        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaMovimiento(entity.getFecha());
        if (entity.getLote() != null) {
            dto.setLoteId(entity.getLote().getId());
        } else {
            dto.setLoteId(-1l);
        }

        dto.setCantidad(entity.getCantidad());
        dto.setUnidadMedida(entity.getUnidadMedida());

        dto.setDescripcion(entity.getDescripcion());
        dto.setNroAnalisis(entity.getNroAnalisis());
        dto.setNroReAnalisis(entity.getNroAnalisis());
        dto.setOrdenProduccion(entity.getOrdenProduccion());

        if (entity.getTipoMovimiento() != null) {
            dto.setTipoMovimiento(entity.getTipoMovimiento().name());
        }
        if (entity.getMotivo() != null) {
            dto.setMotivo(entity.getMotivo().name());
        }

        dto.setDictamenInicial(entity.getDictamenInicial());
        dto.setDictamenFinal(entity.getDictamenFinal());
        return dto;
    }

    @NotNull(message = "La fecha del movimiento es obligatoria")
    private LocalDate fechaMovimiento;

    @NotNull(message = "El ID del lote es obligatorio")
    private Long loteId; // Id del registro del lote => Idem a (codigoInterno + nroBulto) o (loteProveedor + nroBulto) o (nroAnalisis + nroBulto)

    //Cantidades
    private String nroBulto;

    @Positive(message = "La cantidad inicial debe ser mayor a cero")
    private BigDecimal cantidad;

    private UnidadMedidaEnum unidadMedida;

    private LocalDate fechaAnalisis;

    private LocalDate fechaReAnalisis;

    @FutureOrPresent(message = "La fecha de vencimiento debe ser presente o futura")
    private LocalDate fechaVencimiento;

    private String descripcion;

    private String ordenProduccion;

    //Para movimientos productivos
    private String observaciones;

    private String nroAnalisis;

    private String nroReAnalisis;

    //Campos de tipo de CU
    private String tipoMovimiento;

    private String motivo;

    private DictamenEnum dictamenInicial;

    private DictamenEnum dictamenFinal;

    private Movimiento movimientoOrigen;

}
