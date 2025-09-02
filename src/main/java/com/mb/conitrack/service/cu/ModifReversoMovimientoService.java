package com.mb.conitrack.service.cu;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.MotivoEnum;

import jakarta.validation.Valid;

import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoReverso;

//***********CUX ALTA: DEVOLUCION VENTA***********
@Service
public class ModifReversoMovimientoService extends AbstractCuService {

    @Transactional
    public LoteDTO persistirReversoMovmiento(final MovimientoDTO dto) {

        final Movimiento movOrigen = movimientoRepository.findByCodigoMovimientoAndActivoTrue(dto.getCodigoMovimientoOrigen())
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
                    System.out.println("REVERSO ANALISIS");
                }

                if (movOrigen.getMotivo() == MotivoEnum.RESULTADO_ANALISIS) {
                    System.out.println("REVERSO RESULTADO ANALISIS");
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
                if (movOrigen.getMotivo() == MotivoEnum.CONSUMO_PRODUCCION) {
                    System.out.println("REVERSO BAJA CONSUMO PRODUCCION");
                }
                if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_COMPRA) {
                    System.out.println("REVERSO DEVOLUCION COMPRA");
                }
                if (movOrigen.getMotivo() == MotivoEnum.MUESTREO) {
                    System.out.println("REVERSO MUESTREO");
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



}
