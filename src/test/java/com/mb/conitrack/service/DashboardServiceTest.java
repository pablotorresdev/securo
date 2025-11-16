package com.mb.conitrack.service;

import com.mb.conitrack.dto.DashboardMetricsDTO;
import com.mb.conitrack.dto.UserInfoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para DashboardService.
 * Cobertura completa de métricas del dashboard y conversión de usuario.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - DashboardService")
class DashboardServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private AnalisisRepository analisisRepository;

    @InjectMocks
    private DashboardService service;

    private Lote loteActivo;
    private Lote loteCuarentena;
    private Analisis analisisPendiente;
    private Movimiento movimientoHoy;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Lote activo
        loteActivo = new Lote();
        loteActivo.setId(1L);
        loteActivo.setCodigoLote("L-001");
        loteActivo.setActivo(true);
        loteActivo.setDictamen(DictamenEnum.APROBADO);
        loteActivo.setCantidadActual(new BigDecimal("100.50"));
        loteActivo.setFechaVencimientoProveedor(LocalDate.now().plusDays(60));

        // Lote en cuarentena
        loteCuarentena = new Lote();
        loteCuarentena.setId(2L);
        loteCuarentena.setCodigoLote("L-002");
        loteCuarentena.setActivo(true);
        loteCuarentena.setDictamen(DictamenEnum.CUARENTENA);
        loteCuarentena.setCantidadActual(new BigDecimal("50.25"));
        loteCuarentena.setFechaVencimientoProveedor(LocalDate.now().plusDays(20)); // Alerta

        // Análisis pendiente
        analisisPendiente = new Analisis();
        analisisPendiente.setId(1L);
        analisisPendiente.setNroAnalisis("AN-001");
        analisisPendiente.setActivo(true);
        analisisPendiente.setDictamen(null); // Pendiente

        // Movimiento de hoy
        movimientoHoy = new Movimiento();
        movimientoHoy.setId(1L);
        movimientoHoy.setCodigoMovimiento("MOV-001");
        movimientoHoy.setFecha(LocalDate.now());

        // Usuario test
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        testUser = new User("testuser", "password", adminRole);
        testUser.setId(1L);
    }

    @Test
    @DisplayName("test_getDashboardMetrics_conDatos_debe_calcularMetricasCorrectamente")
    void test_getDashboardMetrics_conDatos_debe_calcularMetricasCorrectamente() {
        // Given
        when(loteRepository.findAll()).thenReturn(Arrays.asList(loteActivo, loteCuarentena));
        when(analisisRepository.findAll()).thenReturn(Arrays.asList(analisisPendiente));
        when(movimientoRepository.findAll()).thenReturn(Arrays.asList(movimientoHoy));

        // When
        DashboardMetricsDTO metrics = service.getDashboardMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getLotesActivos()).isEqualTo(2);
        assertThat(metrics.getLotesCuarentena()).isEqualTo(1);
        assertThat(metrics.getAnalisisPendientes()).isEqualTo(1);
        assertThat(metrics.getMovimientosHoy()).isEqualTo(1);
        assertThat(metrics.getStockTotal()).isEqualTo(150.75); // 100.50 + 50.25
        assertThat(metrics.getAlertasPendientes()).isEqualTo(1); // Lote con vencimiento en 20 días

        verify(loteRepository, atLeastOnce()).findAll();
        verify(analisisRepository).findAll();
        verify(movimientoRepository).findAll();
    }

    @Test
    @DisplayName("test_getDashboardMetrics_sinDatos_debe_retornarMetricasEnCero")
    void test_getDashboardMetrics_sinDatos_debe_retornarMetricasEnCero() {
        // Given
        when(loteRepository.findAll()).thenReturn(Collections.emptyList());
        when(analisisRepository.findAll()).thenReturn(Collections.emptyList());
        when(movimientoRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        DashboardMetricsDTO metrics = service.getDashboardMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getLotesActivos()).isEqualTo(0);
        assertThat(metrics.getLotesCuarentena()).isEqualTo(0);
        assertThat(metrics.getAnalisisPendientes()).isEqualTo(0);
        assertThat(metrics.getMovimientosHoy()).isEqualTo(0);
        assertThat(metrics.getStockTotal()).isEqualTo(0.0);
        assertThat(metrics.getAlertasPendientes()).isEqualTo(0);

        verify(loteRepository, atLeastOnce()).findAll();
        verify(analisisRepository).findAll();
        verify(movimientoRepository).findAll();
    }

    @Test
    @DisplayName("test_getDashboardMetrics_conExcepcion_debe_retornarMetricasEnCeroYRegistrarError")
    void test_getDashboardMetrics_conExcepcion_debe_retornarMetricasEnCeroYRegistrarError() {
        // Given
        when(loteRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When
        DashboardMetricsDTO metrics = service.getDashboardMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getLotesActivos()).isEqualTo(0);
        assertThat(metrics.getLotesCuarentena()).isEqualTo(0);
        assertThat(metrics.getAnalisisPendientes()).isEqualTo(0);
        assertThat(metrics.getMovimientosHoy()).isEqualTo(0);
        assertThat(metrics.getStockTotal()).isEqualTo(0.0);
        assertThat(metrics.getAlertasPendientes()).isEqualTo(0);

        verify(loteRepository).findAll();
    }

    @Test
    @DisplayName("test_getDashboardMetrics_soloLotesActivos_debe_filtrarInactivos")
    void test_getDashboardMetrics_soloLotesActivos_debe_filtrarInactivos() {
        // Given
        Lote loteInactivo = new Lote();
        loteInactivo.setId(3L);
        loteInactivo.setActivo(false); // Inactivo
        loteInactivo.setDictamen(DictamenEnum.APROBADO);
        loteInactivo.setCantidadActual(new BigDecimal("200"));

        when(loteRepository.findAll()).thenReturn(Arrays.asList(loteActivo, loteInactivo));
        when(analisisRepository.findAll()).thenReturn(Collections.emptyList());
        when(movimientoRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        DashboardMetricsDTO metrics = service.getDashboardMetrics();

        // Then
        assertThat(metrics.getLotesActivos()).isEqualTo(1); // Solo el activo
        assertThat(metrics.getStockTotal()).isEqualTo(100.50); // Solo cantidad del lote activo
    }

    @Test
    @DisplayName("test_getDashboardMetrics_analisisEnCuarentena_debe_contarComoPendiente")
    void test_getDashboardMetrics_analisisEnCuarentena_debe_contarComoPendiente() {
        // Given
        Analisis analisisCuarentena = new Analisis();
        analisisCuarentena.setId(2L);
        analisisCuarentena.setNroAnalisis("AN-002");
        analisisCuarentena.setActivo(true);
        analisisCuarentena.setDictamen(DictamenEnum.CUARENTENA);

        Analisis analisisAprobado = new Analisis();
        analisisAprobado.setId(3L);
        analisisAprobado.setNroAnalisis("AN-003");
        analisisAprobado.setActivo(true);
        analisisAprobado.setDictamen(DictamenEnum.APROBADO);

        when(loteRepository.findAll()).thenReturn(Collections.emptyList());
        when(analisisRepository.findAll()).thenReturn(Arrays.asList(analisisPendiente, analisisCuarentena, analisisAprobado));
        when(movimientoRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        DashboardMetricsDTO metrics = service.getDashboardMetrics();

        // Then
        assertThat(metrics.getAnalisisPendientes()).isEqualTo(2); // Sin dictamen + cuarentena
    }

    @Test
    @DisplayName("test_getUserInfo_conUsuarioValido_debe_retornarDTOCompleto")
    void test_getUserInfo_conUsuarioValido_debe_retornarDTOCompleto() {
        // Given
        testUser.setFechaExpiracion(LocalDate.now().plusDays(30));

        // When
        UserInfoDTO userInfo = service.getUserInfo(testUser);

        // Then
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getUsername()).isEqualTo("testuser");
        assertThat(userInfo.getRoleName()).isEqualTo("ADMIN");
        assertThat(userInfo.getRoleLevel()).isEqualTo(6);
        assertThat(userInfo.getExpirationDate()).isEqualTo(LocalDate.now().plusDays(30));
        assertThat(userInfo.getIsAuditor()).isFalse();
        assertThat(userInfo.getIsExpired()).isFalse();
        assertThat(userInfo.getLastAccessDate()).isNotNull();
    }

    @Test
    @DisplayName("test_getUserInfo_conUsuarioNull_debe_retornarNull")
    void test_getUserInfo_conUsuarioNull_debe_retornarNull() {
        // When
        UserInfoDTO userInfo = service.getUserInfo(null);

        // Then
        assertThat(userInfo).isNull();
    }

    @Test
    @DisplayName("test_getUserInfo_usuarioSinRole_debe_manejarCasoGracefully")
    void test_getUserInfo_usuarioSinRole_debe_manejarCasoGracefully() {
        // Given
        User userSinRole = new User();
        userSinRole.setUsername("usersinrole");
        userSinRole.setRole(null);

        // When
        UserInfoDTO userInfo = service.getUserInfo(userSinRole);

        // Then
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getUsername()).isEqualTo("usersinrole");
        assertThat(userInfo.getRoleName()).isEqualTo("N/A");
        assertThat(userInfo.getRoleLevel()).isEqualTo(0);
    }

    @Test
    @DisplayName("test_getUserInfo_usuarioAuditor_debe_identificarCorrectamente")
    void test_getUserInfo_usuarioAuditor_debe_identificarCorrectamente() {
        // Given
        Role auditorRole = Role.fromEnum(RoleEnum.AUDITOR);
        auditorRole.setId(2L);
        User auditor = new User("auditor", "password", auditorRole);
        auditor.setFechaExpiracion(LocalDate.now().plusDays(15));

        // When
        UserInfoDTO userInfo = service.getUserInfo(auditor);

        // Then
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getIsAuditor()).isTrue();
        assertThat(userInfo.getRoleName()).isEqualTo("AUDITOR");
    }

    @Test
    @DisplayName("test_getUserInfo_usuarioExpirado_debe_marcarComoExpirado")
    void test_getUserInfo_usuarioExpirado_debe_marcarComoExpirado() {
        // Given
        testUser.setFechaExpiracion(LocalDate.now().minusDays(1)); // Expirado

        // When
        UserInfoDTO userInfo = service.getUserInfo(testUser);

        // Then
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getIsExpired()).isTrue();
    }

    @Test
    @DisplayName("test_getDashboardMetrics_analisisInactivo_no_debe_contarComoPendiente")
    void test_getDashboardMetrics_analisisInactivo_no_debe_contarComoPendiente() {
        // Given
        Analisis analisisInactivo = new Analisis();
        analisisInactivo.setId(3L);
        analisisInactivo.setNroAnalisis("AN-003");
        analisisInactivo.setActivo(false); // Inactivo
        analisisInactivo.setDictamen(null);

        when(loteRepository.findAll()).thenReturn(Collections.emptyList());
        when(analisisRepository.findAll()).thenReturn(Arrays.asList(analisisPendiente, analisisInactivo));
        when(movimientoRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        DashboardMetricsDTO metrics = service.getDashboardMetrics();

        // Then
        assertThat(metrics.getAnalisisPendientes()).isEqualTo(1); // Solo el activo
    }

    @Test
    @DisplayName("test_getDashboardMetrics_movimientoSinFecha_no_debe_contarComoHoy")
    void test_getDashboardMetrics_movimientoSinFecha_no_debe_contarComoHoy() {
        // Given
        Movimiento movSinFecha = new Movimiento();
        movSinFecha.setId(2L);
        movSinFecha.setCodigoMovimiento("MOV-002");
        movSinFecha.setFecha(null); // Sin fecha

        when(loteRepository.findAll()).thenReturn(Collections.emptyList());
        when(analisisRepository.findAll()).thenReturn(Collections.emptyList());
        when(movimientoRepository.findAll()).thenReturn(Arrays.asList(movimientoHoy, movSinFecha));

        // When
        DashboardMetricsDTO metrics = service.getDashboardMetrics();

        // Then
        assertThat(metrics.getMovimientosHoy()).isEqualTo(1); // Solo el que tiene fecha de hoy
    }

    @Test
    @DisplayName("test_getDashboardMetrics_movimientoFechaAnterior_no_debe_contarComoHoy")
    void test_getDashboardMetrics_movimientoFechaAnterior_no_debe_contarComoHoy() {
        // Given
        Movimiento movAyer = new Movimiento();
        movAyer.setId(3L);
        movAyer.setCodigoMovimiento("MOV-003");
        movAyer.setFecha(LocalDate.now().minusDays(1)); // Ayer

        when(loteRepository.findAll()).thenReturn(Collections.emptyList());
        when(analisisRepository.findAll()).thenReturn(Collections.emptyList());
        when(movimientoRepository.findAll()).thenReturn(Arrays.asList(movimientoHoy, movAyer));

        // When
        DashboardMetricsDTO metrics = service.getDashboardMetrics();

        // Then
        assertThat(metrics.getMovimientosHoy()).isEqualTo(1); // Solo el de hoy
    }

    @Test
    @DisplayName("test_getDashboardMetrics_loteSinCantidadActual_debe_usarCero")
    void test_getDashboardMetrics_loteSinCantidadActual_debe_usarCero() {
        // Given
        Lote loteSinCantidad = new Lote();
        loteSinCantidad.setId(4L);
        loteSinCantidad.setCodigoLote("L-004");
        loteSinCantidad.setActivo(true);
        loteSinCantidad.setCantidadActual(null); // Sin cantidad

        when(loteRepository.findAll()).thenReturn(Arrays.asList(loteActivo, loteSinCantidad));
        when(analisisRepository.findAll()).thenReturn(Collections.emptyList());
        when(movimientoRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        DashboardMetricsDTO metrics = service.getDashboardMetrics();

        // Then
        assertThat(metrics.getStockTotal()).isEqualTo(100.50); // Solo la cantidad del lote activo, el otro suma 0
    }

    @Test
    @DisplayName("test_getDashboardMetrics_loteSinFechaVencimiento_no_debe_contarComoAlerta")
    void test_getDashboardMetrics_loteSinFechaVencimiento_no_debe_contarComoAlerta() {
        // Given
        Lote loteSinVencimiento = new Lote();
        loteSinVencimiento.setId(5L);
        loteSinVencimiento.setCodigoLote("L-005");
        loteSinVencimiento.setActivo(true);
        loteSinVencimiento.setFechaVencimientoProveedor(null); // Sin fecha de vencimiento

        when(loteRepository.findAll()).thenReturn(Arrays.asList(loteCuarentena, loteSinVencimiento));
        when(analisisRepository.findAll()).thenReturn(Collections.emptyList());
        when(movimientoRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        DashboardMetricsDTO metrics = service.getDashboardMetrics();

        // Then
        assertThat(metrics.getAlertasPendientes()).isEqualTo(1); // Solo loteCuarentena (vence en 20 días)
    }
}
