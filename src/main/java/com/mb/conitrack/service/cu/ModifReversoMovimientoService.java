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

                    return reversarIngreoCompra(dto, movOrigen);
                }

                if (movOrigen.getMotivo() == MotivoEnum.PRODUCCION_PROPIA) {

                }
                if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_VENTA) {

                }
            }
            case MODIFICACION -> {
                if (movOrigen.getMotivo() == MotivoEnum.ANALISIS) {

                }

                if (movOrigen.getMotivo() == MotivoEnum.RESULTADO_ANALISIS) {

                }
                if (movOrigen.getMotivo() == MotivoEnum.LIBERACION) {

                }
                if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_VENTA) {

                }
                if (movOrigen.getMotivo() == MotivoEnum.RETIRO_MERCADO) {

                }
            }
            case BAJA -> {
                if (movOrigen.getMotivo() == MotivoEnum.CONSUMO_PRODUCCION) {

                }
                if (movOrigen.getMotivo() == MotivoEnum.DEVOLUCION_COMPRA) {

                }
                if (movOrigen.getMotivo() == MotivoEnum.MUESTREO) {

                }
                if (movOrigen.getMotivo() == MotivoEnum.VENTA) {

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

    private LoteDTO reversarIngreoCompra(final MovimientoDTO dto, final Movimiento movOrigen) {
        Movimiento movimiento = createMovimientoReverso(dto, movOrigen);

        movOrigen.setActivo(false);
        movimiento.setActivo(false);
        movOrigen.getLote().setActivo(false);

        movimientoRepository.save(movimiento);
        movimientoRepository.save(movOrigen);
        loteRepository.save(movOrigen.getLote());

        return DTOUtils.fromLoteEntity(movOrigen.getLote());
    }

}
