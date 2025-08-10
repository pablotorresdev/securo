package com.mb.conitrack.service;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.DetalleMovimientoRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.utils.EntityUtils;

import static com.mb.conitrack.utils.EntityUtils.createMovimientoAltaIngresoCompra;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    TrazaService trazaService;

    @Mock
    AnalisisService analisisService;

    @Mock
    MovimientoRepository movimientoRepository;
    @Mock
    DetalleMovimientoRepository detalleMovimientoRepository;

    @Spy
    @InjectMocks
    MovimientoService service; // si tiene más deps, mockéalas como en otros tests

    @Test
    @DisplayName("Con análisis y en curso presente -> usa crearMovimientoConAnalisisEnCurso")
    void conAnalisis_enCursoPresente() {
        // given
        MovimientoDTO dto = new MovimientoDTO();

        Analisis a = new Analisis();
        List<Analisis> lista = new ArrayList<>();
        lista.add(a);

        Lote lote = new Lote();
        lote.setAnalisisList(lista);

        Bulto b = new Bulto();
        b.setLote(lote);

        Movimiento esperado = new Movimiento();

        try (MockedStatic<EntityUtils> ms = mockStatic(EntityUtils.class)) {
            ms.when(() -> EntityUtils.getAnalisisEnCurso(lista)).thenReturn(Optional.of(a));

            doReturn(esperado)
                .when(service)
                .crearMovimientoConAnalisisEnCurso(eq(dto), eq(b), eq(Optional.of(a)));

            // when
            Movimiento out = service.persistirMovimientoMuestreo(dto, b);

            // then
            assertSame(esperado, out);
            ms.verify(() -> EntityUtils.getAnalisisEnCurso(lista));
        }

        verify(service).crearMovimientoConAnalisisEnCurso(eq(dto), eq(b), eq(Optional.of(a)));
        verify(service, never()).crearMovimientoConPrimerAnalisis(any(), any());
        verify(service, never()).crearMovmimientoConAnalisisDictaminado(any(), any());
    }

    @Test
    @DisplayName("Con análisis y en curso vacío -> usa crearMovmimientoConAnalisisDictaminado")
    void conAnalisis_enCursoVacio() {
        // given
        MovimientoDTO dto = new MovimientoDTO();

        Analisis a = new Analisis();
        List<Analisis> lista = new ArrayList<>();
        lista.add(a);

        Lote lote = new Lote();
        lote.setAnalisisList(lista);

        Bulto b = new Bulto();
        b.setLote(lote);

        Movimiento esperado = new Movimiento();

        try (MockedStatic<EntityUtils> ms = mockStatic(EntityUtils.class)) {
            ms.when(() -> EntityUtils.getAnalisisEnCurso(lista)).thenReturn(Optional.empty());

            doReturn(esperado).when(service).crearMovmimientoConAnalisisDictaminado(dto, b);

            // when
            Movimiento out = service.persistirMovimientoMuestreo(dto, b);

            // then
            assertSame(esperado, out);
            ms.verify(() -> EntityUtils.getAnalisisEnCurso(lista));
        }

        verify(service).crearMovmimientoConAnalisisDictaminado(dto, b);
        verify(service, never()).crearMovimientoConPrimerAnalisis(any(), any());
        verify(service, never()).crearMovimientoConAnalisisEnCurso(any(), any(), any());
    }

    @Test
    @DisplayName("crearMovmimientoConAnalisisDictaminado: nro distinto -> IllegalArgumentException")
    void crearDictaminado_mismatch() {
        MovimientoDTO dto = dtoBase("AN-X");

        Analisis ultimo = new Analisis();
        ultimo.setNroAnalisis("AN-OK");

        Lote loteMock = mock(Lote.class);
        when(loteMock.getUltimoAnalisis()).thenReturn(ultimo);

        Bulto b = new Bulto();
        b.setLote(loteMock);
        b.setNroBulto(1);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.crearMovmimientoConAnalisisDictaminado(dto, b)
        );
        assertEquals("El número de análisis no coincide con el análisis en curso", ex.getMessage());

        verify(loteMock).getUltimoAnalisis();
        verifyNoInteractions(movimientoRepository);
    }

    @Test
    @DisplayName("crearMovmimientoConAnalisisDictaminado: nro coincide con último -> guarda y retorna")
    void crearDictaminado_ok() {
        // given
        MovimientoDTO dto = dtoBase("AN-9");

        Analisis ultimo = new Analisis();
        ultimo.setNroAnalisis("AN-9");

        Lote loteMock = mock(Lote.class);
        when(loteMock.getUltimoAnalisis()).thenReturn(ultimo);
        when(loteMock.getCodigoInterno()).thenReturn("L-COD");

        Bulto b = new Bulto();
        b.setLote(loteMock);
        b.setNroBulto(5);

        when(movimientoRepository.save(any(Movimiento.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // when
        Movimiento out = service.crearMovmimientoConAnalisisDictaminado(dto, b);

        // then
        assertNotNull(out);
        assertEquals("AN-9", out.getNroAnalisis());
        assertSame(loteMock, out.getLote());
        assertTrue(out.getBultos().contains(b));
        assertTrue(out.getCodigoInterno().startsWith("L-COD-B_5-"));

        verify(loteMock).getUltimoAnalisis();
        verify(movimientoRepository).save(any(Movimiento.class));
        verifyNoMoreInteractions(movimientoRepository);
    }

    @Test
    @DisplayName("crearMovimientoConAnalisisEnCurso: nro distinto -> IllegalArgumentException (mensaje correcto)")
    void crearEnCurso_mismatch() {
        MovimientoDTO dto = dtoBase("AN-2");
        Bulto b = bultoConLote("L-XYZ", 1);

        Analisis enCurso = new Analisis();
        enCurso.setNroAnalisis("AN-1");

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.crearMovimientoConAnalisisEnCurso(dto, b, Optional.of(enCurso))
        );
        assertEquals("El número de análisis no coincide con el análisis en curso", ex.getMessage());

        verifyNoInteractions(movimientoRepository, analisisService);
    }

    @Test
    @DisplayName("crearMovimientoConAnalisisEnCurso: nro coincide -> guarda y retorna Movimiento")
    void crearEnCurso_ok() {
        // given
        MovimientoDTO dto = dtoBase("AN-1");
        Bulto b = bultoConLote("L-XYZ", 2);

        Analisis enCurso = new Analisis();
        enCurso.setNroAnalisis("AN-1");

        // repo devuelve mismo objeto que recibe
        when(movimientoRepository.save(any(Movimiento.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // when
        Movimiento out = service.crearMovimientoConAnalisisEnCurso(dto, b, Optional.of(enCurso));

        // then
        assertNotNull(out);
        assertEquals("AN-1", out.getNroAnalisis());
        assertSame(b.getLote(), out.getLote());
        assertTrue(out.getBultos().contains(b));
        // formato del código (prefijo chequeado)
        assertTrue(out.getCodigoInterno().startsWith("L-XYZ-B_2-"));

        verify(movimientoRepository).save(any(Movimiento.class));
        verifyNoMoreInteractions(movimientoRepository, analisisService);
    }

    @Test
    @DisplayName("crearMovimientoConAnalisisEnCurso: Optional.empty() -> IllegalArgumentException (mensaje correcto)")
    void crearEnCurso_vacio() {
        MovimientoDTO dto = dtoBase("AN-1");
        Bulto b = bultoConLote("L-XYZ", 1);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.crearMovimientoConAnalisisEnCurso(dto, b, Optional.empty())
        );
        assertEquals("El número de análisis esta vacio", ex.getMessage());

        verifyNoInteractions(movimientoRepository, analisisService);
    }

    @Test
    @DisplayName("crearMovimientoConPrimerAnalisis: crea Analisis a partir de DTO y guarda movimiento")
    void crearPrimerAnalisis_ok() {
        // given
        MovimientoDTO dto = dtoBase("AN-NEW");
        Bulto b = bultoConLote("L-AAA", 3);

        Analisis analisisFromDto = new Analisis();            // lo que devuelve DTOUtils.createAnalisis(dto)
        Analisis analisisPersistido = new Analisis();         // lo que devuelve analisisService.save(...)
        analisisPersistido.setNroAnalisis("AN-SAVED");

        when(analisisService.save(analisisFromDto)).thenReturn(analisisPersistido);
        when(movimientoRepository.save(any(Movimiento.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<DTOUtils> ms = mockStatic(DTOUtils.class)) {
            ms.when(() -> DTOUtils.createAnalisis(dto)).thenReturn(analisisFromDto);

            // when
            Movimiento out = service.crearMovimientoConPrimerAnalisis(dto, b);

            // then
            assertNotNull(out);
            // Debe usar el nro del Analisis persistido
            assertEquals("AN-SAVED", out.getNroAnalisis());
            assertSame(b.getLote(), out.getLote());
            assertTrue(out.getBultos().contains(b));

            // verify: se creó analisis a partir de DTO, se guardó y luego se guardó el movimiento
            ms.verify(() -> DTOUtils.createAnalisis(dto));
            ArgumentCaptor<Analisis> analisisCap = ArgumentCaptor.forClass(Analisis.class);
            verify(analisisService).save(analisisCap.capture());
            assertSame(analisisFromDto, analisisCap.getValue());

            verify(movimientoRepository).save(any(Movimiento.class));
        }

        verifyNoMoreInteractions(movimientoRepository, analisisService);
    }

    @Test
    void findAll() {
    }

    @Test
    void findAllMuestreos() {
    }

    @Test
    @DisplayName("EntityUtils.getAnalisisEnCurso lanza IAE (más de uno) -> propaga excepción")
    void multipleAnalisisEnCurso_lanza() {
        // given
        MovimientoDTO dto = new MovimientoDTO();

        List<Analisis> lista = new ArrayList<>();
        lista.add(new Analisis());
        lista.add(new Analisis());

        Lote lote = new Lote();
        lote.setAnalisisList(lista);

        Bulto b = new Bulto();
        b.setLote(lote);

        try (MockedStatic<EntityUtils> ms = mockStatic(EntityUtils.class)) {
            ms.when(() -> EntityUtils.getAnalisisEnCurso(lista))
                .thenThrow(new IllegalArgumentException("El lote tiene más de un análisis en curso"));

            // when / then
            assertThrows(IllegalArgumentException.class, () -> service.persistirMovimientoMuestreo(dto, b));
            ms.verify(() -> EntityUtils.getAnalisisEnCurso(lista));
        }

        // no debe llamar a ningún creador
        verify(service, never()).crearMovimientoConPrimerAnalisis(any(), any());
        verify(service, never()).crearMovimientoConAnalisisEnCurso(any(), any(), any());
        verify(service, never()).crearMovmimientoConAnalisisDictaminado(any(), any());
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
            trazaService, analisisService, movimientoRepository, detalleMovimientoRepository
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
    @DisplayName("OK -> setea motivo/DICTAMEN/nroAnalisis/observaciones y guarda")
    void persistirMovimientoCuarentenaPorAnalisis_ok() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setObservaciones("Obs de prueba");

        Lote lote = new Lote();
        lote.setDictamen(DictamenEnum.APROBADO); // dictamen inicial esperado

        String nroAnalisisParam = "AN-123";

        Movimiento base = new Movimiento(); // lo que devuelve EntityUtils
        base.setLote(lote);
        base.setObservaciones("será sobreescrito");

        try (MockedStatic<EntityUtils> ms = mockStatic(EntityUtils.class)) {
            ms.when(() -> EntityUtils.createMovimientoModificacion(dto, lote)).thenReturn(base);
            // el repo devuelve lo que le pasan (más simple para asserts)
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenAnswer(inv -> inv.getArgument(0, Movimiento.class));

            // when
            Movimiento out = service.persistirMovimientoCuarentenaPorAnalisis(dto, lote, nroAnalisisParam);

            // then: se guardó el MISMO objeto creado por EntityUtils
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

            ms.verify(() -> EntityUtils.createMovimientoModificacion(dto, lote));
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

        try (MockedStatic<EntityUtils> ms = mockStatic(EntityUtils.class)) {
            ms.when(() -> EntityUtils.createMovimientoModificacion(dto, lote)).thenReturn(base);
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenAnswer(inv -> inv.getArgument(0, Movimiento.class));

            // when
            Movimiento out = service.persistirMovimientoCuarentenaPorAnalisis(dto, lote, nroAnalisisParam);

            // then
            verify(movimientoRepository).save(out);
            ms.verify(() -> EntityUtils.createMovimientoModificacion(dto, lote));

            assertEquals(MotivoEnum.ANALISIS, out.getMotivo());
            assertEquals(DictamenEnum.RECIBIDO, out.getDictamenInicial());
            assertEquals(DictamenEnum.CUARENTENA, out.getDictamenFinal());
            // gana el parámetro, no el dto
            assertEquals("PARAM-888", out.getNroAnalisis());
            // concatena literal con null
            assertEquals("_CU2_\nnull", out.getObservaciones());
        }
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

    /* ===============================
       crearMovimientoConAnalisisEnCurso
       =============================== */

    @Test
    void persistirMovimientoReanalisisProducto() {
    }

    @Test
    void persistirMovimientoResultadoAnalisis() {
    }

    @Test
    void save() {
    }

    /* =========================================
       crearMovmimientoConAnalisisDictaminado
       ========================================= */

    @Test
    @DisplayName("Sin análisis -> usa crearMovimientoConPrimerAnalisis; NO llama EntityUtils.getAnalisisEnCurso")
    void sinAnalisis_llamaPrimerAnalisis() {
        // given
        MovimientoDTO dto = new MovimientoDTO();

        Lote lote = new Lote();
        lote.setAnalisisList(new ArrayList<>()); // vacío

        Bulto b = new Bulto();
        b.setLote(lote);

        Movimiento esperado = new Movimiento();
        doReturn(esperado).when(service).crearMovimientoConPrimerAnalisis(dto, b);

        // when
        Movimiento out = service.persistirMovimientoMuestreo(dto, b);

        // then
        assertSame(esperado, out);
        verify(service).crearMovimientoConPrimerAnalisis(dto, b);
        verify(service, never()).crearMovimientoConAnalisisEnCurso(any(), any(), any());
        verify(service, never()).crearMovmimientoConAnalisisDictaminado(any(), any());
    }

    private Bulto bultoConLote(String codLote, int nroBulto) {
        Lote lote = new Lote();
        lote.setCodigoInterno(codLote);

        Bulto b = new Bulto();
        b.setLote(lote);
        b.setNroBulto(nroBulto);
        return b;
    }

    /* ==============================
       crearMovimientoConPrimerAnalisis
       ============================== */

    /* ---------- helpers ---------- */
    private MovimientoDTO dtoBase(String nroAnalisis) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNroAnalisis(nroAnalisis);
        dto.setFechaYHoraCreacion(LocalDateTime.now());
        dto.setFechaMovimiento(LocalDate.now());
        dto.setCantidad(new BigDecimal("2"));
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        dto.setObservaciones("obs");
        return dto;
    }


    @Test
    @DisplayName("createMovimientoConAnalisis arma código, setea lote/bulto y usa el movimiento base")
    void createMovimientoConAnalisis_ok() throws Exception {
        // DTO con fecha/hora conocida para validar el timestamp
        MovimientoDTO dto = new MovimientoDTO();
        LocalDateTime now = LocalDateTime.of(2025, 1, 2, 3, 4, 5);
        dto.setFechaYHoraCreacion(now);
        dto.setNroAnalisis("IGNORADO_PORQUE_SE_SOBRESCRIBE");

        // Lote y Bulto
        Lote lote = new Lote();
        lote.setCodigoInterno("L-ABC");
        Bulto bulto = new Bulto();
        bulto.setLote(lote);
        bulto.setNroBulto(3);

        // Analisis “último”
        Analisis ultimo = new Analisis();
        ultimo.setNroAnalisis("AN-77");

        // Movimiento base que debería devolver EntityUtils.createMovimientoPorMuestreo(dto)
        Movimiento base = new Movimiento();
        base.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        base.setMotivo(MotivoEnum.MUESTREO);

        // Mock al método estático que el helper usa internamente
        try (MockedStatic<EntityUtils> ms = mockStatic(EntityUtils.class)) {
            ms.when(() -> EntityUtils.createMovimientoPorMuestreo(dto)).thenReturn(base);

            // Invocamos por reflection (por si el método no es público)
            Method m = MovimientoService.class.getDeclaredMethod(
                "createMovimientoConAnalisis",
                MovimientoDTO.class, Bulto.class, Analisis.class
            );
            m.setAccessible(true);

            Movimiento out = (Movimiento) m.invoke(service, dto, bulto, ultimo);

            // --- then: misma instancia que la creada por EntityUtils ---
            assertSame(base, out, "Debe reutilizar el movimiento base retornado por createMovimientoPorMuestreo");

            // Código interno con timestamp formateado
            String ts = now.format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));
            assertEquals("L-ABC-B_3-" + ts, out.getCodigoInterno());

            // Seteo de lote y agregado del bulto
            assertSame(lote, out.getLote());
            assertTrue(out.getBultos().contains(bulto), "Debe contener el bulto asociado");

            // Nro de análisis del 'ultimoAnalisis' (sobrescribe lo que viniera del DTO)
            assertEquals("AN-77", out.getNroAnalisis());

            // Y conserva lo que venía del movimiento base
            assertEquals(TipoMovimientoEnum.BAJA, out.getTipoMovimiento());
            assertEquals(MotivoEnum.MUESTREO, out.getMotivo());

            ms.verify(() -> EntityUtils.createMovimientoPorMuestreo(dto));
        }
    }
}