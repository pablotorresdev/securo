package com.mb.conitrack.service.cu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.TrazaRepository;
import com.mb.conitrack.service.SecurityContextService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModifTrazadoLoteServiceTest {

    @Spy
    @InjectMocks
    ModifTrazadoLoteService service;

    @Mock
    LoteRepository loteRepository;

    @Mock
    MovimientoRepository movimientoRepository;

    @Mock
    TrazaRepository trazaRepository;

    @Mock
    SecurityContextService securityContextService;

    MovimientoDTO movDto;
    BindingResult binding;
    Lote lote;
    User user;

    @BeforeEach
    void setUp() {
        movDto = new MovimientoDTO();
        movDto.setCodigoLote("L-001");
        movDto.setFechaMovimiento(LocalDate.now());
        movDto.setFechaYHoraCreacion(OffsetDateTime.now());
        movDto.setObservaciones("Test");
        movDto.setTrazaInicial(1L);

        TrazaDTO trazaDto = new TrazaDTO();
        trazaDto.setNroBulto(1);
        trazaDto.setNroTraza(1L);
        movDto.setTrazaDTOs(List.of(trazaDto));

        binding = new BeanPropertyBindingResult(movDto, "movimientoDTO");

        lote = new Lote();
        lote.setId(1L);
        lote.setCodigoLote("L-001");
        lote.setFechaIngreso(LocalDate.now().minusDays(1));
        lote.setDictamen(DictamenEnum.APROBADO);
        lote.setMovimientos(new ArrayList<>());
        lote.setTrazas(new HashSet<>());
        lote.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        lote.setCantidadActual(BigDecimal.valueOf(100));

        // Crear un bulto con unidades para soportar trazas
        Bulto bulto = new Bulto();
        bulto.setNroBulto(1);
        bulto.setCantidadInicial(BigDecimal.valueOf(100));
        bulto.setCantidadActual(BigDecimal.valueOf(100));
        bulto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        bulto.setLote(lote);
        bulto.setTrazas(new HashSet<>());
        bulto.setActivo(true);
        lote.setBultos(List.of(bulto));

        Producto producto = new Producto();
        producto.setTipoProducto(TipoProductoEnum.UNIDAD_VENTA);
        lote.setProducto(producto);

        user = new User();
        user.setUsername("testuser");
    }

    @Test
    void testPersistirTrazadoLote_Exitoso() {
        when(securityContextService.getCurrentUser()).thenReturn(user);
        when(loteRepository.findByCodigoLoteAndActivoTrue(anyString())).thenReturn(Optional.of(lote));
        when(movimientoRepository.save(any(Movimiento.class))).thenReturn(new Movimiento());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);

        LoteDTO result = service.persistirTrazadoLote(movDto);

        assertNotNull(result);
        verify(trazaRepository, atLeastOnce()).saveAll(any());
        verify(movimientoRepository).save(any(Movimiento.class));
        verify(loteRepository).save(any(Lote.class));
    }

    @Test
    void testPersistirTrazadoLote_LoteNoExiste() {
        when(securityContextService.getCurrentUser()).thenReturn(user);
        when(loteRepository.findByCodigoLoteAndActivoTrue(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.persistirTrazadoLote(movDto));
    }

    @Test
    void testPersistirTrazadoLote_NoEsUnidadVenta() {
        Producto producto = new Producto();
        producto.setTipoProducto(TipoProductoEnum.API);
        lote.setProducto(producto);

        when(securityContextService.getCurrentUser()).thenReturn(user);
        when(loteRepository.findByCodigoLoteAndActivoTrue(anyString())).thenReturn(Optional.of(lote));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.persistirTrazadoLote(movDto)
        );
        assertTrue(exception.getMessage().contains("UNIDAD_VENTA"));
    }

    @Test
    void testPersistirMovimientoTrazadoLote() {
        Movimiento movimiento = new Movimiento();
        movimiento.setId(1L);

        when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

        Movimiento result = service.persistirMovimientoTrazadoLote(movDto, lote, user);

        assertNotNull(result);
        verify(movimientoRepository).save(any(Movimiento.class));
    }

    @Test
    void testValidarTrazadoLoteInput_Exitoso() {
        when(loteRepository.findByCodigoLoteAndActivoTrue(anyString())).thenReturn(Optional.of(lote));
        doReturn(true).when(service).validarTrazaInicialLote(any(), any());

        boolean result = service.validarTrazadoLoteInput(movDto, binding);

        assertTrue(result);
        assertFalse(binding.hasErrors());
    }

    @Test
    void testValidarTrazadoLoteInput_ConErroresBinding() {
        binding.rejectValue("codigoLote", "", "Error");

        boolean result = service.validarTrazadoLoteInput(movDto, binding);

        assertFalse(result);
    }

    @Test
    void testValidarTrazadoLoteInput_LoteNoEncontrado() {
        when(loteRepository.findByCodigoLoteAndActivoTrue(anyString())).thenReturn(Optional.empty());

        boolean result = service.validarTrazadoLoteInput(movDto, binding);

        assertFalse(result);
        assertTrue(binding.hasErrors());
        assertNotNull(binding.getFieldError("codigoLote"));
    }

    @Test
    void testValidarTrazadoLoteInput_FechaInvalida() {
        movDto.setFechaMovimiento(LocalDate.now().minusDays(10));
        when(loteRepository.findByCodigoLoteAndActivoTrue(anyString())).thenReturn(Optional.of(lote));

        boolean result = service.validarTrazadoLoteInput(movDto, binding);

        assertFalse(result);
        assertTrue(binding.hasErrors());
    }
}
