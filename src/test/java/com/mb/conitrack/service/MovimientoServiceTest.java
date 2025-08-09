package com.mb.conitrack.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.utils.EntityUtils;

import static com.mb.conitrack.utils.EntityUtils.createMovimientoAltaIngresoCompra;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    TrazaService trazaService;

    @Mock
    AnalisisService analisisService;

    @Mock
    MovimientoRepository movimientoRepository;

    @Test
    void findAll() {
    }

    @Test
    void findAllMuestreos() {
    }

    @Test
    void persistirMovimientoAltaDevolucionVenta() {
    }

    @Test
    void persistirMovimientoAltaIngresoCompra() {
    }

    @Test
    @DisplayName("Crea movimiento, setea lote y guarda — devuelve lo persistido")
    void persistirMovimientoAltaIngresoCompra_ok() {
        // Servicio con dependencias mockeadas
        MovimientoService service = new MovimientoService(
            trazaService, analisisService, movimientoRepository
        );

        Lote lote = new Lote();

        // 1) Mock del método estático: retorna SIEMPRE este objeto
        Movimiento creado = new Movimiento();
        try (var mocked = mockStatic(EntityUtils.class)) {
            mocked.when(() -> createMovimientoAltaIngresoCompra(lote))
                .thenReturn(creado);

            // 2) El repo devuelve otra instancia (para verificar que se propaga el retorno)
            Movimiento persistido = new Movimiento();
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(persistido);

            // 3) Ejecutar
            Movimiento result = service.persistirMovimientoAltaIngresoCompra(lote);

            // 4) Verificaciones
            mocked.verify(() -> createMovimientoAltaIngresoCompra(lote)); // se llama al factory

            // capturamos el que se guardó y validamos que es el MISMO creado y con lote seteado
            ArgumentCaptor<Movimiento> cap = ArgumentCaptor.forClass(Movimiento.class);
            verify(movimientoRepository).save(cap.capture());
            assertSame(creado, cap.getValue(), "Debe persistir la misma instancia creada por el factory");
            assertSame(lote, cap.getValue().getLote(), "Debe setear el lote en el movimiento antes de guardar");

            // retorna lo que devuelve el repository (no el creado)
            assertSame(persistido, result, "Debe devolver lo que retorna el repository.save()");
        }
    }

    @Test
    void persistirMovimientoAltaIngresoProduccion() {
    }

    @Test
    void persistirMovimientoBajaConsumoProduccion() {
    }

    @Test
    void persistirMovimientoBajaVenta() {
    }

    @Test
    void persistirMovimientoCuarentenaPorAnalisis() {
    }

    @Test
    void persistirMovimientoDevolucionCompra() {
    }

    @Test
    void persistirMovimientoExpiracionAnalisis() {
    }

    @Test
    void persistirMovimientoLiberacionProducto() {
    }

    @Test
    void persistirMovimientoMuestreo() {
    }

    @Test
    void persistirMovimientoProductoVencido() {
    }

    @Test
    void persistirMovimientoReanalisisProducto() {
    }

    @Test
    void persistirMovimientoResultadoAnalisis() {
    }

    @Test
    void save() {
    }

}