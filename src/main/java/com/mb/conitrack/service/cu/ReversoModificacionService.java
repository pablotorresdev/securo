package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.*;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mb.conitrack.utils.MovimientoModificacionUtils.createMovimientoReverso;

/**
 * Servicio especializado para reversar movimientos de tipo MODIFICACION.
 * Maneja los reversos de:
 * - CU2/8: Dictamen de Cuarentena
 * - CU5/6: Resultado de Análisis
 * - CU21: Liberación de Producto
 * - CU27: Trazado de Lote
 * - CU11: Anulación de Análisis
 *
 * NO soporta reverso de:
 * - CU10: Vencimiento (proceso automático irreversible)
 * - CU9: Expiración de Análisis (proceso automático)
 * - CU24: Retiro de Mercado (se maneja en ReversoAltaService)
 */
@Service
public class ReversoModificacionService extends AbstractCuService {

    @Transactional
    public LoteDTO reversarModifDictamenCuarentena(final MovimientoDTO dto, final Movimiento movOrigen, final User currentUser) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen, currentUser);
        final Lote lote = movOrigen.getLote();
        lote.setDictamen(movOrigen.getDictamenInicial());

        movimiento.setDictamenFinal(movOrigen.getDictamenInicial());
        movimiento.setDictamenInicial(movOrigen.getDictamenFinal());

        final Analisis ultimoAnalisis = lote.getUltimoAnalisis();

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        ultimoAnalisis.setActivo(false);

        analisisRepository.save(ultimoAnalisis);
        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);

        loteRepository.save(lote);

        return DTOUtils.fromLoteEntity(lote);
    }

    @Transactional
    public LoteDTO reversarModifResultadoAnalisis(final MovimientoDTO dto, final Movimiento movOrigen, final User currentUser) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen, currentUser);
        final Lote lote = movOrigen.getLote();
        lote.setDictamen(movOrigen.getDictamenInicial());

        movimiento.setDictamenFinal(movOrigen.getDictamenInicial());
        movimiento.setDictamenInicial(movOrigen.getDictamenFinal());

        final Analisis ultimoAnalisis = lote.getUltimoAnalisis();

        ultimoAnalisis.setFechaRealizado(null);
        ultimoAnalisis.setDictamen(null);
        ultimoAnalisis.setFechaReanalisis(null);
        ultimoAnalisis.setFechaVencimiento(null);
        ultimoAnalisis.setTitulo(null);
        ultimoAnalisis.setObservaciones(null);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);

        analisisRepository.save(ultimoAnalisis);
        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);

        loteRepository.save(lote);

        return DTOUtils.fromLoteEntity(lote);
    }

    @Transactional
    public LoteDTO reversarModifLiberacionProducto(final MovimientoDTO dto, final Movimiento movOrigen, final User currentUser) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen, currentUser);
        final Lote lote = movOrigen.getLote();

        lote.setDictamen(movOrigen.getDictamenInicial());

        movimiento.setDictamenInicial(movOrigen.getDictamenFinal());
        movimiento.setDictamenFinal(movOrigen.getDictamenInicial());
        lote.setFechaVencimientoProveedor(null);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        movimientoRepository.save(movOrigen);
        movimientoRepository.save(movimiento);

        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public LoteDTO reversarModifTrazadoLote(final MovimientoDTO dto, final Movimiento movOrigen, final User currentUser) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen, currentUser);
        final Lote lote = movOrigen.getLote();

        final List<Traza> trazasLote = lote.getActiveTrazas();
        for (Traza t : trazasLote) {
            t.setActivo(false);
            t.setEstado(EstadoEnum.DESCARTADO);
            t.getDetalles().forEach(d -> d.setActivo(false));
        }
        trazaRepository.saveAll(trazasLote);
        lote.setTrazado(false);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        movimientoRepository.save(movOrigen);
        movimientoRepository.save(movimiento);

        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }

    @Transactional
    public LoteDTO reversarAnulacionAnalisis(final MovimientoDTO dto, final Movimiento movOrigen, final User currentUser) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen, currentUser);
        final Lote lote = movOrigen.getLote();
        final Analisis ultimoAnalisis = lote.getUltimoAnalisis();

        if (ultimoAnalisis.getDictamen() != DictamenEnum.ANULADO) {
            throw new IllegalArgumentException("El ultimo analisis no esta anulado");
        }

        ultimoAnalisis.setDictamen(null);
        analisisRepository.save(ultimoAnalisis);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);

        lote.setDictamen(movOrigen.getDictamenInicial());
        return DTOUtils.fromLoteEntity(loteRepository.save(lote));
    }
}
