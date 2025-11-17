package com.mb.conitrack.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.TrazaRepository;
import com.mb.conitrack.service.cu.BajaMuestreoBultoService;
import com.mb.conitrack.service.cu.MuestreoTrazableService;
import com.mb.conitrack.service.cu.MuestreoMultiBultoService;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BajaMuestreoBultoServiceTest {

    @Mock
    LoteRepository loteRepository;

    @Mock
    MovimientoRepository movimientoRepository;

    @Mock
    AnalisisRepository analisisRepository;

    @Mock
    AnalisisService analisisService;

    @Mock
    TrazaRepository trazaRepository;

    @Mock
    SecurityContextService securityContextService;

    @InjectMocks
    BajaMuestreoBultoService muestreoBultoService;

    private MuestreoTrazableService muestreoTrazableService;
    private MuestreoMultiBultoService muestreoMultiBultoService;

    private User testUser;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        testUser = new User("testuser", "password", adminRole);
        testUser.setId(1L);
        lenient().when(securityContextService.getCurrentUser()).thenReturn(testUser);

        // Create real instances of specialized services
        muestreoTrazableService = new MuestreoTrazableService();
        muestreoMultiBultoService = new MuestreoMultiBultoService();

        // Inject mocked repositories into specialized services
        ReflectionTestUtils.setField(muestreoTrazableService, "loteRepository", loteRepository);
        ReflectionTestUtils.setField(muestreoTrazableService, "movimientoRepository", movimientoRepository);
        ReflectionTestUtils.setField(muestreoTrazableService, "analisisRepository", analisisRepository);
        ReflectionTestUtils.setField(muestreoTrazableService, "trazaRepository", trazaRepository);

        ReflectionTestUtils.setField(muestreoMultiBultoService, "loteRepository", loteRepository);
        ReflectionTestUtils.setField(muestreoMultiBultoService, "movimientoRepository", movimientoRepository);
        ReflectionTestUtils.setField(muestreoMultiBultoService, "analisisRepository", analisisRepository);
        ReflectionTestUtils.setField(muestreoMultiBultoService, "trazaRepository", trazaRepository);

        // Inject specialized services into coordinator
        ReflectionTestUtils.setField(muestreoBultoService, "muestreoTrazableService", muestreoTrazableService);
        ReflectionTestUtils.setField(muestreoBultoService, "muestreoMultiBultoService", muestreoMultiBultoService);
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
            () -> muestreoBultoService.crearMovmimientoMuestreoConAnalisisDictaminado(dto, b, testUser)
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
        when(loteMock.getCodigoLote()).thenReturn("L-COD");

        Bulto b = new Bulto();
        b.setLote(loteMock);
        b.setNroBulto(5);

        when(movimientoRepository.save(any(Movimiento.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // when
        Movimiento out = muestreoBultoService.crearMovmimientoMuestreoConAnalisisDictaminado(dto, b, testUser);

        // then
        assertNotNull(out);
        assertEquals("AN-9", out.getNroAnalisis());
        assertSame(loteMock, out.getLote());
        assertFalse(out.getDetalles().isEmpty());
        assertTrue(out.getCodigoMovimiento().startsWith("L-COD-B_5-"));

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
            () -> muestreoBultoService.crearMovimientoMuestreoConAnalisisEnCurso(dto, b, Optional.of(enCurso), testUser)
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
        Movimiento out = muestreoBultoService.crearMovimientoMuestreoConAnalisisEnCurso(dto, b, Optional.of(enCurso), testUser);

        // then
        assertNotNull(out);
        assertEquals("AN-1", out.getNroAnalisis());
        assertSame(b.getLote(), out.getLote());
        assertFalse(out.getDetalles().isEmpty());
        // formato del código (prefijo chequeado)
        assertTrue(out.getCodigoMovimiento().startsWith("L-XYZ-B_2-"));

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
            () -> muestreoBultoService.crearMovimientoMuestreoConAnalisisEnCurso(dto, b, Optional.empty(), testUser)
        );
        assertEquals("El número de análisis esta vacio", ex.getMessage());

        verifyNoInteractions(movimientoRepository, analisisService);
    }



    /* ==============================
       crearMovimientoConPrimerAnalisis
       ============================== */

    private Bulto bultoConLote(String codLote, int nroBulto) {
        Lote lote = new Lote();
        lote.setCodigoLote(codLote);

        Bulto b = new Bulto();
        b.setLote(lote);
        b.setNroBulto(nroBulto);
        return b;
    }

    /* ---------- helpers ---------- */
    private MovimientoDTO dtoBase(String nroAnalisis) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNroAnalisis(nroAnalisis);
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setFechaMovimiento(LocalDate.now());
        dto.setCantidad(new BigDecimal("2"));
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        dto.setObservaciones("obs");
        return dto;
    }

}