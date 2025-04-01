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

        // 1) Fechas
        dto.setFechaMovimiento(entity.getFecha());
        // dto.setFechaAnalisis(...) // El Entity Movimiento no tiene estos campos,
        // dto.setFechaReAnalisis(...) // así que podrías dejarlos en null o derivarlos
        // dto.setFechaVencimiento(...)

        // 2) Lote asociado
        //    Si el Movimiento guarda un Lote, podemos mapear su ID:
        if (entity.getLote() != null) {
            dto.setLoteId(entity.getLote().getId());
            // Si quisieras, dto.setNroBulto(String.valueOf(entity.getLote().getNroBulto()))
            //   o algo similar, dependiendo de la lógica que manejes.
        } else {
            // Si el Movimiento no tiene un Lote asociado, puedes dejarlo en null
            // o derivar un valor según tu lógica de negocio.
            dto.setLoteId(-1l);
        }

        // 3) Cantidad y unidad
        dto.setCantidad(entity.getCantidad());
        dto.setUnidadMedida(entity.getUnidadMedida());

        // 4) Campos textuales
        // 'descripcion' en el Entity es distinto de 'observaciones' en el DTO,
        // o al revés. Ajusta según tu modelo.
        dto.setDescripcion(entity.getDescripcion());
        // Podrías mapear 'observaciones' si el Entity tuviera un campo así:

        dto.setNroAnalisis(entity.getNroAnalisis());
        // El Entity no tiene "nroReAnalisis", a menos que lo guardes en otra columna
        // dto.setNroReAnalisis(...);

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

    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate fechaMovimiento;

    @NotNull(message = "El ID del lote es obligatorio")
    private Long loteId; // Id del registro del lote => Idem a (codigoInterno + nroBulto) o (loteProveedor + nroBulto) o (nroAnalisis + nroBulto)

    //Cantidades
    private String nroBulto;

    @Positive(message = "La cantidad inicial debe ser mayor a cero")
    private BigDecimal cantidad;

    private UnidadMedidaEnum unidadMedida;

    @FutureOrPresent(message = "La fecha de análisis debe ser presente o futura")
    private LocalDate fechaAnalisis;

    @FutureOrPresent(message = "La fecha de re-análisis debe ser presente o futura")
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
