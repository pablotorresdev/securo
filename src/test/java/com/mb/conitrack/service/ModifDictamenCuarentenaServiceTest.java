package com.mb.conitrack.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.TrazaRepository;
import com.mb.conitrack.service.cu.ModifDictamenCuarentenaService;
import com.mb.conitrack.utils.MovimientoModificacionUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifDictamenCuarentenaServiceTest {

    @Mock
    LoteRepository loteRepository;

    @Mock
    MovimientoRepository movimientoRepository;

    @Mock
    AnalisisRepository analisisRepository;

    @Mock
    TrazaRepository trazaRepository;

    @Mock
    SecurityContextService securityContextService;

    @Spy
    @InjectMocks
    ModifDictamenCuarentenaService dictamenCuarentenaService;

    private User testUser;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        testUser = new User("testuser", "password", adminRole);
        testUser.setId(1L);
        lenient().when(securityContextService.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    @DisplayName("OK -> setea motivo/DICTAMEN/nroAnalisis/observaciones y guarda")
    void persistirMovimientoCuarentenaPorAnalisis_ok() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setObservaciones("Obs de prueba");

        Lote lote = new Lote();
        lote.setDictamen(DictamenEnum.APROBADO); // dictamen inicial esperado

        String nroAnalisisParam = "AN-123";

        Movimiento base = new Movimiento(); // lo que devuelve LoteEntityUtils
        base.setLote(lote);
        base.setObservaciones("será sobreescrito");

        try (MockedStatic<MovimientoModificacionUtils> ms = mockStatic(MovimientoModificacionUtils.class)) {
            ms.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(dto, lote, testUser)).thenReturn(base);
            // el repo devuelve lo que le pasan (más simple para asserts)
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenAnswer(inv -> inv.getArgument(0, Movimiento.class));

            // when
            Movimiento out = dictamenCuarentenaService.persistirMovimientoCuarentenaPorAnalisis(
                dto,
                lote,
                nroAnalisisParam,
                testUser);

            // then: se guardó el MISMO objeto creado por LoteEntityUtils
            ArgumentCaptor<Movimiento> cap = ArgumentCaptor.forClass(Movimiento.class);
            verify(movimientoRepository).save(cap.capture());
            Movimiento saved = cap.getValue();

            assertSame(base, saved);
            assertSame(saved, out);

            assertEquals(MotivoEnum.ANALISIS, saved.getMotivo());
            assertEquals(DictamenEnum.APROBADO, saved.getDictamenInicial());
            assertEquals(DictamenEnum.CUARENTENA, saved.getDictamenFinal());
            assertEquals("AN-123", saved.getNroAnalisis());
            assertEquals("_CU2_\nObs de prueba", saved.getObservaciones());
            assertSame(lote, saved.getLote());

            ms.verify(() -> MovimientoModificacionUtils.createMovimientoModificacion(dto, lote, testUser));
            verifyNoMoreInteractions(movimientoRepository);
        }
    }

    @Test
    @DisplayName("Usa nroAnalisis del parámetro (no del DTO) y observaciones null")
    void persistirMovimientoCuarentenaPorAnalisis_overrideNroAnalisis_yObsNull() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setObservaciones(null);
        dto.setNroAnalisis("DTO-777"); // NO debe usarse

        Lote lote = new Lote();
        lote.setDictamen(DictamenEnum.RECIBIDO);

        String nroAnalisisParam = "PARAM-888";

        Movimiento base = new Movimiento();
        base.setLote(lote);

        try (MockedStatic<MovimientoModificacionUtils> ms = mockStatic(MovimientoModificacionUtils.class)) {
            ms.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(dto, lote, testUser)).thenReturn(base);
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenAnswer(inv -> inv.getArgument(0, Movimiento.class));

            // when
            Movimiento out = dictamenCuarentenaService.persistirMovimientoCuarentenaPorAnalisis(
                dto,
                lote,
                nroAnalisisParam,
                testUser);

            // then
            verify(movimientoRepository).save(out);
            ms.verify(() -> MovimientoModificacionUtils.createMovimientoModificacion(dto, lote, testUser));

            assertEquals(MotivoEnum.ANALISIS, out.getMotivo());
            assertEquals(DictamenEnum.RECIBIDO, out.getDictamenInicial());
            assertEquals(DictamenEnum.CUARENTENA, out.getDictamenFinal());
            // gana el parámetro, no el dto
            assertEquals("PARAM-888", out.getNroAnalisis());
            // concatena literal con null
            assertEquals("_CU2_\nnull", out.getObservaciones());
        }
    }

    // -------------------- persistirDictamenCuarentena --------------------
    @Test
    @DisplayName("persistirDictamenCuarentena: lote no encontrado → excepción")
    void persistirDictamenCuarentena_loteNoEncontrado() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOTE-999");

        when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            dictamenCuarentenaService.persistirDictamenCuarentena(dto);
        });

        verify(loteRepository).findByCodigoLoteAndActivoTrue("LOTE-999");
    }

    @Test
    @DisplayName("persistirDictamenCuarentena: análisis nuevo (nroReanalisis vacío) → crea análisis")
    void persistirDictamenCuarentena_analisisNuevo_usaNroAnalisis() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOTE-001");
        dto.setNroAnalisis("AN-2025-001");
        dto.setNroReanalisis("");  // vacío, usa nroAnalisis
        dto.setObservaciones("Test observaciones");

        Producto producto = new Producto();
        producto.setCodigoProducto("PROD-001");
        producto.setNombreGenerico("Producto Test");

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Proveedor Test");

        Lote lote = new Lote();
        lote.setCodigoLote("LOTE-001");
        lote.setDictamen(DictamenEnum.APROBADO);
        lote.setFechaIngreso(LocalDate.now());
        lote.setCantidadInicial(BigDecimal.TEN);
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.setProducto(producto);
        lote.setProveedor(proveedor);
        lote.setMovimientos(new ArrayList<>());
        lote.setAnalisisList(new ArrayList<>());

        Analisis nuevoAnalisis = new Analisis();
        nuevoAnalisis.setNroAnalisis("AN-2025-001");
        nuevoAnalisis.setLote(lote);

        Movimiento movimiento = new Movimiento();
        movimiento.setNroAnalisis("AN-2025-001");

        when(securityContextService.getCurrentUser()).thenReturn(testUser);
        when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));
        when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(null);

        try (MockedStatic<DTOUtils> dtoUtils = mockStatic(DTOUtils.class);
             MockedStatic<MovimientoModificacionUtils> movUtils = mockStatic(MovimientoModificacionUtils.class)) {

            dtoUtils.when(() -> DTOUtils.createAnalisis(dto)).thenReturn(nuevoAnalisis);
            movUtils.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(dto, lote, testUser))
                .thenReturn(movimiento);

            when(analisisRepository.save(any(Analisis.class))).thenReturn(nuevoAnalisis);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

            LoteDTO loteDTOResult = new LoteDTO();
            loteDTOResult.setCodigoLote("LOTE-001");
            dtoUtils.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTOResult);

            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            LoteDTO resultado = dictamenCuarentenaService.persistirDictamenCuarentena(dto);

            assertNotNull(resultado);
            assertEquals("LOTE-001", resultado.getCodigoLote());

            verify(analisisRepository).save(any(Analisis.class));
            verify(movimientoRepository).save(any(Movimiento.class));
            verify(loteRepository).save(any(Lote.class));
            assertEquals(DictamenEnum.CUARENTENA, lote.getDictamen());
        }
    }

    @Test
    @DisplayName("persistirDictamenCuarentena: análisis existente → no crea análisis nuevo")
    void persistirDictamenCuarentena_analisisExistente() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOTE-001");
        dto.setNroAnalisis("AN-2025-001");
        dto.setNroReanalisis("");
        dto.setObservaciones("Test");

        Producto producto = new Producto();
        producto.setCodigoProducto("PROD-001");
        producto.setNombreGenerico("Producto Test");

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Proveedor Test");

        Lote lote = new Lote();
        lote.setCodigoLote("LOTE-001");
        lote.setDictamen(DictamenEnum.APROBADO);
        lote.setFechaIngreso(LocalDate.now());
        lote.setCantidadInicial(BigDecimal.TEN);
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.setProducto(producto);
        lote.setProveedor(proveedor);
        lote.setMovimientos(new ArrayList<>());
        lote.setAnalisisList(new ArrayList<>());

        Analisis analisisExistente = new Analisis();
        analisisExistente.setNroAnalisis("AN-2025-001");

        Movimiento movimiento = new Movimiento();
        movimiento.setNroAnalisis("AN-2025-001");

        when(securityContextService.getCurrentUser()).thenReturn(testUser);
        when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));
        when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(analisisExistente);

        try (MockedStatic<DTOUtils> dtoUtils = mockStatic(DTOUtils.class);
             MockedStatic<MovimientoModificacionUtils> movUtils = mockStatic(MovimientoModificacionUtils.class)) {

            movUtils.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(dto, lote, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

            LoteDTO loteDTOResult = new LoteDTO();
            loteDTOResult.setCodigoLote("LOTE-001");
            dtoUtils.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTOResult);

            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            LoteDTO resultado = dictamenCuarentenaService.persistirDictamenCuarentena(dto);

            assertNotNull(resultado);
            verify(analisisRepository, never()).save(any(Analisis.class));  // no crea nuevo
            verify(movimientoRepository).save(any(Movimiento.class));
            verify(loteRepository).save(any(Lote.class));
        }
    }

    @Test
    @DisplayName("persistirDictamenCuarentena: usa nroReanalisis si no está vacío")
    void persistirDictamenCuarentena_usaNroReanalisis() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOTE-001");
        dto.setNroAnalisis("AN-2025-001");
        dto.setNroReanalisis("RAN-2025-002");  // tiene reanalisis
        dto.setObservaciones("Test");

        Producto producto = new Producto();
        producto.setCodigoProducto("PROD-001");
        producto.setNombreGenerico("Producto Test");

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Proveedor Test");

        Lote lote = new Lote();
        lote.setCodigoLote("LOTE-001");
        lote.setDictamen(DictamenEnum.RECHAZADO);
        lote.setFechaIngreso(LocalDate.now());
        lote.setCantidadInicial(BigDecimal.TEN);
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.setProducto(producto);
        lote.setProveedor(proveedor);
        lote.setMovimientos(new ArrayList<>());
        lote.setAnalisisList(new ArrayList<>());

        Analisis nuevoAnalisis = new Analisis();
        nuevoAnalisis.setNroAnalisis("RAN-2025-002");

        Movimiento movimiento = new Movimiento();
        movimiento.setNroAnalisis("RAN-2025-002");

        when(securityContextService.getCurrentUser()).thenReturn(testUser);
        when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));
        when(analisisRepository.findByNroAnalisisAndActivoTrue("RAN-2025-002")).thenReturn(null);

        try (MockedStatic<DTOUtils> dtoUtils = mockStatic(DTOUtils.class);
             MockedStatic<MovimientoModificacionUtils> movUtils = mockStatic(MovimientoModificacionUtils.class)) {

            dtoUtils.when(() -> DTOUtils.createAnalisis(dto)).thenReturn(nuevoAnalisis);
            movUtils.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(dto, lote, testUser))
                .thenReturn(movimiento);

            when(analisisRepository.save(any(Analisis.class))).thenReturn(nuevoAnalisis);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

            LoteDTO loteDTOResult = new LoteDTO();
            dtoUtils.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTOResult);

            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            LoteDTO resultado = dictamenCuarentenaService.persistirDictamenCuarentena(dto);

            assertNotNull(resultado);
            verify(analisisRepository).findByNroAnalisisAndActivoTrue("RAN-2025-002");
        }
    }

    @Test
    @DisplayName("persistirDictamenCuarentena: procesa trazas activas → setea DISPONIBLE")
    void persistirDictamenCuarentena_conTrazas() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOTE-001");
        dto.setNroAnalisis("AN-2025-001");
        dto.setNroReanalisis("");

        Producto producto = new Producto();
        Proveedor proveedor = new Proveedor();

        Lote lote = new Lote();
        lote.setCodigoLote("LOTE-001");
        lote.setDictamen(DictamenEnum.APROBADO);
        lote.setFechaIngreso(LocalDate.now());
        lote.setCantidadInicial(BigDecimal.TEN);
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.setProducto(producto);
        lote.setProveedor(proveedor);
        lote.setMovimientos(new ArrayList<>());
        lote.setAnalisisList(new ArrayList<>());

        // Crear trazas activas
        Traza traza1 = new Traza();
        traza1.setEstado(EstadoEnum.NUEVO);
        traza1.setActivo(true);
        Traza traza2 = new Traza();
        traza2.setEstado(EstadoEnum.EN_USO);
        traza2.setActivo(true);

        Set<Traza> trazas = new HashSet<>();
        trazas.add(traza1);
        trazas.add(traza2);
        lote.setTrazas(trazas);

        Analisis analisisExistente = new Analisis();
        analisisExistente.setNroAnalisis("AN-2025-001");

        Movimiento movimiento = new Movimiento();

        when(securityContextService.getCurrentUser()).thenReturn(testUser);
        when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));
        when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(analisisExistente);

        try (MockedStatic<DTOUtils> dtoUtils = mockStatic(DTOUtils.class);
             MockedStatic<MovimientoModificacionUtils> movUtils = mockStatic(MovimientoModificacionUtils.class)) {

            movUtils.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(dto, lote, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);
            when(trazaRepository.saveAll(any(List.class))).thenAnswer(inv -> inv.getArgument(0));

            LoteDTO loteDTOResult = new LoteDTO();
            dtoUtils.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTOResult);

            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dictamenCuarentenaService.persistirDictamenCuarentena(dto);

            verify(trazaRepository).saveAll(any(List.class));
            assertEquals(EstadoEnum.DISPONIBLE, traza1.getEstado());
            assertEquals(EstadoEnum.DISPONIBLE, traza2.getEstado());
        }
    }

    @Test
    @DisplayName("persistirDictamenCuarentena: sin trazas → no llama saveAll")
    void persistirDictamenCuarentena_sinTrazas() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOTE-001");
        dto.setNroAnalisis("AN-2025-001");
        dto.setNroReanalisis("");

        Producto producto = new Producto();
        Proveedor proveedor = new Proveedor();

        Lote lote = new Lote();
        lote.setCodigoLote("LOTE-001");
        lote.setDictamen(DictamenEnum.APROBADO);
        lote.setFechaIngreso(LocalDate.now());
        lote.setCantidadInicial(BigDecimal.TEN);
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.setProducto(producto);
        lote.setProveedor(proveedor);
        lote.setMovimientos(new ArrayList<>());
        lote.setAnalisisList(new ArrayList<>());
        lote.setTrazas(Collections.emptySet());  // sin trazas

        Analisis analisisExistente = new Analisis();
        Movimiento movimiento = new Movimiento();

        when(securityContextService.getCurrentUser()).thenReturn(testUser);
        when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));
        when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(analisisExistente);

        try (MockedStatic<DTOUtils> dtoUtils = mockStatic(DTOUtils.class);
             MockedStatic<MovimientoModificacionUtils> movUtils = mockStatic(MovimientoModificacionUtils.class)) {

            movUtils.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(dto, lote, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

            LoteDTO loteDTOResult = new LoteDTO();
            dtoUtils.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTOResult);

            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dictamenCuarentenaService.persistirDictamenCuarentena(dto);

            verify(trazaRepository, never()).saveAll(any(List.class));
        }
    }

    // -------------------- validarDictamenCuarentenaInput --------------------
    @Test
    @DisplayName("validarDictamenCuarentenaInput: BindingResult con errores → false")
    void validarDictamenCuarentenaInput_conErroresBinding() {
        MovimientoDTO dto = new MovimientoDTO();
        BindingResult br = new BeanPropertyBindingResult(dto, "movimientoDTO");
        br.addError(new FieldError("movimientoDTO", "codigoLote", "error"));

        boolean result = dictamenCuarentenaService.validarDictamenCuarentenaInput(dto, br);

        assertFalse(result);
    }

    @Test
    @DisplayName("validarDictamenCuarentenaInput: nroAnalisis null o vacío → false")
    void validarDictamenCuarentenaInput_nroAnalisisNullOVacio() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOTE-001");
        dto.setNroAnalisis(null);  // null
        dto.setNroReanalisis(null);
        dto.setFechaMovimiento(LocalDate.now());
        BindingResult br = new BeanPropertyBindingResult(dto, "movimientoDTO");

        boolean result = dictamenCuarentenaService.validarDictamenCuarentenaInput(dto, br);

        assertFalse(result);
        assertTrue(br.hasErrors());
    }

    @Test
    @DisplayName("validarDictamenCuarentenaInput: nroAnalisis duplicado con dictamen → false")
    void validarDictamenCuarentenaInput_nroAnalisisDuplicado() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOTE-001");
        dto.setNroAnalisis("AN-EXISTENTE");
        dto.setNroReanalisis("");
        dto.setFechaMovimiento(LocalDate.now());
        BindingResult br = new BeanPropertyBindingResult(dto, "movimientoDTO");

        // Mock: el análisis ya existe (duplicado) y tiene dictamen
        Lote loteDelAnalisis = new Lote();
        loteDelAnalisis.setCodigoLote("LOTE-001");

        Analisis analisisExistente = new Analisis();
        analisisExistente.setNroAnalisis("AN-EXISTENTE");
        analisisExistente.setLote(loteDelAnalisis);
        analisisExistente.setDictamen(DictamenEnum.CUARENTENA);  // ya tiene dictamen

        when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-EXISTENTE")).thenReturn(analisisExistente);

        boolean result = dictamenCuarentenaService.validarDictamenCuarentenaInput(dto, br);

        assertFalse(result);
        assertTrue(br.hasFieldErrors("nroAnalisis"));
    }

    @Test
    @DisplayName("validarDictamenCuarentenaInput: lote no encontrado → false")
    void validarDictamenCuarentenaInput_loteNoEncontrado() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOTE-999");
        dto.setNroAnalisis("AN-2025-001");
        dto.setFechaMovimiento(LocalDate.now());
        BindingResult br = new BeanPropertyBindingResult(dto, "movimientoDTO");

        when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(null);
        when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-999")).thenReturn(Optional.empty());

        boolean result = dictamenCuarentenaService.validarDictamenCuarentenaInput(dto, br);

        assertFalse(result);
        assertTrue(br.hasFieldErrors("codigoLote"));
        assertEquals("Lote no encontrado.", br.getFieldError("codigoLote").getDefaultMessage());
    }

    @Test
    @DisplayName("validarDictamenCuarentenaInput: fechaMovimiento anterior a fechaIngreso → false")
    void validarDictamenCuarentenaInput_fechaMovimientoAnteriorIngresoLote() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOTE-001");
        dto.setNroAnalisis("AN-2025-001");
        dto.setFechaMovimiento(LocalDate.of(2025, 1, 1));  // anterior
        BindingResult br = new BeanPropertyBindingResult(dto, "movimientoDTO");

        Lote lote = new Lote();
        lote.setFechaIngreso(LocalDate.of(2025, 1, 15));  // posterior

        when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(null);
        when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));

        boolean result = dictamenCuarentenaService.validarDictamenCuarentenaInput(dto, br);

        assertFalse(result);
        assertTrue(br.hasErrors());
    }

    @Test
    @DisplayName("validarDictamenCuarentenaInput: todas validaciones OK → true")
    void validarDictamenCuarentenaInput_todasValidacionesOK() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOTE-001");
        dto.setNroAnalisis("AN-2025-001");
        dto.setFechaMovimiento(LocalDate.of(2025, 1, 20));
        BindingResult br = new BeanPropertyBindingResult(dto, "movimientoDTO");

        Lote lote = new Lote();
        lote.setFechaIngreso(LocalDate.of(2025, 1, 15));

        when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(null);
        when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));

        boolean result = dictamenCuarentenaService.validarDictamenCuarentenaInput(dto, br);

        assertTrue(result);
        assertFalse(br.hasErrors());
    }

}