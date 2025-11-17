package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.mb.conitrack.enums.EstadoEnum.DISPONIBLE;
import static com.mb.conitrack.enums.EstadoEnum.RECALL;
import static com.mb.conitrack.utils.MovimientoModificacionUtils.createMovimientoModifRecall;
import static java.lang.Boolean.TRUE;

/**
 * Servicio especializado para modificar el estado de lotes a RECALL.
 * Maneja la operación MODIFICACION del CU24 - Retiro de Mercado.
 *
 * Este servicio:
 * - Cambia el estado del lote original de venta a RECALL
 * - Marca bultos como RECALL
 * - Actualiza trazas DISPONIBLE a RECALL
 * - Cancela análisis en curso si existe
 */
@Service
public class ModificacionRecallService extends AbstractCuService {

    /**
     * Procesa la modificación del lote de venta original cambiando su estado a RECALL.
     * Si el lote ya está en RECALL, no hace nada.
     */
    @Transactional
    public void procesarModificacionRecall(
            final MovimientoDTO dto,
            final Lote loteOrigenRecall,
            final Movimiento movimientoVentaOrigen,
            final List<Lote> result,
            User currentUser) {

        if (loteOrigenRecall.getEstado() == RECALL) {
            loteRepository.findById(loteOrigenRecall.getId()).ifPresent(result::add);
            return;
        }

        final Movimiento movimientoModifRecall = createMovimientoModifRecall(dto, currentUser);
        movimientoModifRecall.setDictamenInicial(loteOrigenRecall.getDictamen());
        movimientoModifRecall.setMovimientoOrigen(movimientoVentaOrigen);
        movimientoModifRecall.setLote(loteOrigenRecall);

        if (TRUE.equals(loteOrigenRecall.getTrazado())) {
            procesarRecallTrazado(loteOrigenRecall);
        } else {
            procesarRecallSinTrazas(loteOrigenRecall);
        }

        final Movimiento savedMovModifRecall = movimientoRepository.save(movimientoModifRecall);

        bultoRepository.saveAll(loteOrigenRecall.getBultos());

        loteOrigenRecall.setEstado(RECALL);
        loteOrigenRecall.getMovimientos().add(savedMovModifRecall);

        cancelarAnalisisEnCurso(loteOrigenRecall);

        loteRepository.findById(loteRepository.save(loteOrigenRecall).getId()).ifPresent(result::add);
    }

    /**
     * Procesa recall para productos trazados: marca trazas DISPONIBLE como RECALL.
     */
    private void procesarRecallTrazado(final Lote loteOrigenRecall) {
        for (Bulto bulto : loteOrigenRecall.getBultos()) {
            final Set<Traza> trazas = bulto.getTrazas();
            final List<Traza> trazasRecall = new ArrayList<>();
            boolean recall = false;
            for (Traza tr : trazas) {
                if (tr.getEstado() != DISPONIBLE) {
                    continue;
                }

                tr.setEstado(RECALL);
                trazasRecall.add(tr);
                recall = true;
            }
            if (recall) {
                bulto.setEstado(RECALL);
            }
            trazaRepository.saveAll(trazasRecall);
        }
    }

    /**
     * Procesa recall para productos no trazados: marca bultos con stock como RECALL.
     */
    private void procesarRecallSinTrazas(final Lote loteOrigenRecall) {
        for (Bulto bultoRecall : loteOrigenRecall.getBultos()) {
            if (bultoRecall.getCantidadActual().compareTo(BigDecimal.ZERO) > 0) {
                bultoRecall.setEstado(RECALL);
            }
        }
    }

    /**
     * Cancela el análisis en curso si existe y no tiene dictamen.
     * Esto evita que se complete un análisis para un lote que ya está en recall.
     */
    private void cancelarAnalisisEnCurso(final Lote loteOrigenRecall) {
        if (loteOrigenRecall.getUltimoAnalisis() != null &&
            loteOrigenRecall.getUltimoAnalisis().getDictamen() == null) {
            loteOrigenRecall.getUltimoAnalisis().setDictamen(DictamenEnum.CANCELADO);
            analisisRepository.save(loteOrigenRecall.getUltimoAnalisis());
        }
    }
}
