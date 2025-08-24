package com.mb.conitrack.service.cu;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;

import lombok.AllArgsConstructor;

import static com.mb.conitrack.enums.DictamenEnum.LIBERADO;
import static com.mb.conitrack.enums.MotivoEnum.LIBERACION;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoModificacion;

//***********CU11 MODIFICACION: LIBERACION DE PRODUCTO***********
@Service
public class ModifLiberacionVentasService extends AbstractCuService {

    @Transactional
    public Lote persistirLiberacionProducto(final MovimientoDTO dto, final Lote lote) {

        final Movimiento movimiento = persistirMovimientoLiberacionProducto(dto, lote);

        lote.setFechaReanalisisProveedor(lote.getFechaReanalisisVigente());
        lote.setFechaVencimientoProveedor(lote.getFechaVencimientoVigente());

        lote.setDictamen(movimiento.getDictamenFinal());
        lote.getMovimientos().add(movimiento);
        return loteRepository.save(lote);
    }

    @Transactional
    public Movimiento persistirMovimientoLiberacionProducto(final MovimientoDTO dto, final Lote lote) {
        Movimiento movimiento = createMovimientoModificacion(dto, lote);

        movimiento.setFecha(dto.getFechaMovimiento());
        movimiento.setMotivo(LIBERACION);
        movimiento.setDictamenInicial(lote.getDictamen());
        movimiento.setDictamenFinal(LIBERADO);

        movimiento.setObservaciones("_CU11_\n" + dto.getObservaciones());
        return movimientoRepository.save(movimiento);
    }

}
