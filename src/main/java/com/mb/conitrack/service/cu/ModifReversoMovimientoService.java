package com.mb.conitrack.service.cu;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.utils.UnidadMedidaUtils;

import jakarta.validation.Valid;

import static com.mb.conitrack.enums.EstadoEnum.EN_USO;
import static com.mb.conitrack.enums.EstadoEnum.NUEVO;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoReverso;
import static com.mb.conitrack.utils.UnidadMedidaUtils.sumarMovimientoConvertido;
import static java.lang.Integer.parseInt;

//***********CUX ALTA: DEVOLUCION VENTA***********
@Service
public class ModifReversoMovimientoService extends AbstractCuService {

    @Transactional
    public LoteDTO persistirReversoMovmiento(final MovimientoDTO dto) {

        final Movimiento movOrigen = movimientoRepository
            .findByCodigoMovimientoAndActivoTrue(dto.getCodigoMovimientoOrigen())
            .orElseThrow(() -> new IllegalArgumentException("El Movimmiento no existe."));

        switch (movOrigen.getTipoMovimiento()) {
            case ALTA -> {
                if (movOrigen.getMotivo() == MotivoEnum.COMPRA) {
                    return reversarIngresoCompra(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.PRODUCCION_PROPIA) {
                    return reversarIngresoProduccion(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_VENTA) {
                    System.out.println("REVERSO DEVOLUCION VENTA");
                }
            }
            case MODIFICACION -> {
                if (movOrigen.getMotivo() == MotivoEnum.ANALISIS) {
                    return reversarDictamenCuarentena(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.RESULTADO_ANALISIS) {
                    return reversarResultadoAnalisis(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.LIBERACION) {
                    System.out.println("REVERSO LIBERACION");
                }
                if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_VENTA) {
                    System.out.println("REVERSO DEVOLUCION VENTA");
                }
                if (movOrigen.getMotivo() == MotivoEnum.RETIRO_MERCADO) {
                    System.out.println("REVERSO RETIRO MERCADO");
                }
            }
            case BAJA -> {
                if (movOrigen.getMotivo() == MotivoEnum.MUESTREO) {
                    return reversarMuestreoBulto(dto, movOrigen);
                }
                if (movOrigen.getMotivo() == MotivoEnum.CONSUMO_PRODUCCION) {
                    System.out.println("REVERSO BAJA CONSUMO PRODUCCION");
                }
                if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_COMPRA) {
                    System.out.println("REVERSO DEVOLUCION COMPRA");
                }

                if (movOrigen.getMotivo() == MotivoEnum.VENTA) {
                    System.out.println("REVERSO VENTA");
                }
            }
        }
        return new LoteDTO();
    }

    public boolean validarReversoMovmientoInput(
        final @Valid MovimientoDTO movimientoDTO,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        return true;
    }

    @Transactional
    LoteDTO reversarDictamenCuarentena(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
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
    LoteDTO reversarIngresoCompra(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        movOrigen.getLote().setActivo(false);

        movOrigen.getLote().getBultos().forEach(b -> b.setActivo(false));
        bultoRepository.saveAll(movOrigen.getLote().getBultos());

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        loteRepository.save(movOrigen.getLote());

        return DTOUtils.fromLoteEntity(movOrigen.getLote());
    }

    @Transactional
    LoteDTO reversarIngresoProduccion(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        movOrigen.getLote().setActivo(false);

        movOrigen.getLote().getBultos().forEach(b -> b.setActivo(false));
        bultoRepository.saveAll(movOrigen.getLote().getBultos());

        movOrigen.getLote().getTrazas().forEach(t -> t.setActivo(false));
        trazaRepository.saveAll(movOrigen.getLote().getTrazas());

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        loteRepository.save(movOrigen.getLote());

        return DTOUtils.fromLoteEntity(movOrigen.getLote());
    }

    @Transactional
    LoteDTO reversarMuestreoBulto(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
        movimiento.setCantidad(movOrigen.getCantidad());
        movimiento.setUnidadMedida(movOrigen.getUnidadMedida());

        if(movOrigen.getDetalles().size() > 1) {
            throw new IllegalArgumentException("Multimuestreo no soportado aun");
        }

        final DetalleMovimiento detalleMovimiento = movOrigen.getDetalles().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("El detalle del movimiento a reversar no existe."));

        final Bulto bulto = detalleMovimiento.getBulto();
        detalleMovimiento.getTrazas().forEach(t -> t.setEstado(EstadoEnum.DISPONIBLE));

        dto.setCantidad(movOrigen.getCantidad());
        dto.setUnidadMedida(movOrigen.getUnidadMedida());

        bulto.setCantidadActual(sumarMovimientoConvertido(dto, bulto));
        if(bulto.getCantidadInicial().compareTo(bulto.getCantidadActual()) == 0) {
            bulto.setEstado(NUEVO);
        }else{
            bulto.setEstado(EN_USO);
        }

        final Lote lote = movOrigen.getLote();
        lote.setCantidadActual(sumarMovimientoConvertido(dto, lote));

        if(lote.getCantidadInicial().compareTo(lote.getCantidadActual()) == 0) {
            lote.setEstado(NUEVO);
        }else{
            lote.setEstado(EN_USO);
        }

        movOrigen.setActivo(false);
        movimiento.setActivo(false);

        trazaRepository.saveAll(detalleMovimiento.getTrazas());
        bultoRepository.save(bulto);

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        loteRepository.save(lote);

        return DTOUtils.fromLoteEntity(movOrigen.getLote());
    }

    @Transactional
    LoteDTO reversarResultadoAnalisis(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);
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

}
