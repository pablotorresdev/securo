package com.mb.conitrack.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.service.AuditorAccessLogger;
import com.mb.conitrack.service.SecurityContextService;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ReportesController Tests")
class ReportesControllerTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private SecurityContextService securityContextService;

    @Mock
    private AuditorAccessLogger auditorAccessLogger;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ReportesController controller;

    private User auditorUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Role auditorRole = Role.fromEnum(RoleEnum.AUDITOR);
        auditorUser = new User("auditor", "password", auditorRole);

        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminUser = new User("admin", "password", adminRole);
    }

    @Test
    @DisplayName("getAllLotes debe retornar todos los lotes incluyendo bajas lógicas")
    void testGetAllLotes() {
        // Arrange
        List<Lote> lotes = new ArrayList<>();
        Lote lote1 = new Lote();
        lote1.setId(1L);
        lote1.setActivo(true);
        Lote lote2 = new Lote();
        lote2.setId(2L);
        lote2.setActivo(false); // baja lógica
        lotes.add(lote1);
        lotes.add(lote2);

        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(loteRepository.findAll()).thenReturn(lotes);

        // Act
        ResponseEntity<List<Lote>> response = controller.getAllLotes(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(loteRepository, times(1)).findAll();
        verify(auditorAccessLogger, times(1)).logReporteAccess(auditorUser, "Consulta todos los lotes", request);
    }

    @Test
    @DisplayName("getAllLotes debe retornar lista vacía cuando no hay lotes")
    void testGetAllLotes_Empty() {
        // Arrange
        when(securityContextService.getCurrentUser()).thenReturn(adminUser);
        when(loteRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<Lote>> response = controller.getAllLotes(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
        verify(loteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getLoteById debe retornar lote cuando existe")
    void testGetLoteById_Found() {
        // Arrange
        Long id = 1L;
        Lote lote = new Lote();
        lote.setId(id);
        lote.setCodigoLote("LOTE001");

        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(loteRepository.findById(id)).thenReturn(Optional.of(lote));

        // Act
        ResponseEntity<Lote> response = controller.getLoteById(id, request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("LOTE001", response.getBody().getCodigoLote());
        verify(loteRepository, times(1)).findById(id);
        verify(auditorAccessLogger, times(1)).logReporteAccess(eq(auditorUser), contains("Consulta lote por ID: 1"), eq(request));
    }

    @Test
    @DisplayName("getLoteById debe retornar 404 cuando el lote no existe")
    void testGetLoteById_NotFound() {
        // Arrange
        Long id = 999L;

        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(loteRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Lote> response = controller.getLoteById(id, request);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(loteRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("getAllMovimientos debe retornar todos los movimientos")
    void testGetAllMovimientos() {
        // Arrange
        List<Movimiento> movimientos = new ArrayList<>();
        Movimiento mov1 = new Movimiento();
        mov1.setId(1L);
        movimientos.add(mov1);

        when(securityContextService.getCurrentUser()).thenReturn(adminUser);
        when(movimientoRepository.findAll()).thenReturn(movimientos);

        // Act
        ResponseEntity<List<Movimiento>> response = controller.getAllMovimientos(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(movimientoRepository, times(1)).findAll();
        verify(auditorAccessLogger, times(1)).logReporteAccess(adminUser, "Consulta todos los movimientos", request);
    }

    @Test
    @DisplayName("getAllMovimientos debe retornar lista vacía cuando no hay movimientos")
    void testGetAllMovimientos_Empty() {
        // Arrange
        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(movimientoRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<Movimiento>> response = controller.getAllMovimientos(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
        verify(movimientoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getMovimientosByLote debe retornar movimientos del lote cuando existe")
    void testGetMovimientosByLote_Found() {
        // Arrange
        Long loteId = 1L;
        Lote lote = new Lote();
        lote.setId(loteId);

        Movimiento mov1 = new Movimiento();
        mov1.setId(1L);
        mov1.setLote(lote);

        Movimiento mov2 = new Movimiento();
        mov2.setId(2L);
        mov2.setLote(lote);

        List<Movimiento> allMovimientos = new ArrayList<>();
        allMovimientos.add(mov1);
        allMovimientos.add(mov2);

        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(loteRepository.findById(loteId)).thenReturn(Optional.of(lote));
        when(movimientoRepository.findAll()).thenReturn(allMovimientos);

        // Act
        ResponseEntity<List<Movimiento>> response = controller.getMovimientosByLote(loteId, request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(loteRepository, times(1)).findById(loteId);
        verify(movimientoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getMovimientosByLote debe filtrar movimientos de otros lotes y nulls")
    void testGetMovimientosByLote_FiltersOtherLotes() {
        // Arrange
        Long loteId = 1L;
        Lote lote = new Lote();
        lote.setId(loteId);

        Lote otroLote = new Lote();
        otroLote.setId(2L);

        // Movimiento del lote buscado
        Movimiento mov1 = new Movimiento();
        mov1.setId(1L);
        mov1.setLote(lote);

        // Movimiento de otro lote
        Movimiento mov2 = new Movimiento();
        mov2.setId(2L);
        mov2.setLote(otroLote);

        // Movimiento sin lote (null)
        Movimiento mov3 = new Movimiento();
        mov3.setId(3L);
        mov3.setLote(null);

        // Otro movimiento del lote buscado
        Movimiento mov4 = new Movimiento();
        mov4.setId(4L);
        mov4.setLote(lote);

        List<Movimiento> allMovimientos = new ArrayList<>();
        allMovimientos.add(mov1);
        allMovimientos.add(mov2);  // Este debe filtrarse
        allMovimientos.add(mov3);  // Este debe filtrarse
        allMovimientos.add(mov4);

        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(loteRepository.findById(loteId)).thenReturn(Optional.of(lote));
        when(movimientoRepository.findAll()).thenReturn(allMovimientos);

        // Act
        ResponseEntity<List<Movimiento>> response = controller.getMovimientosByLote(loteId, request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size()); // Solo mov1 y mov4
        assertTrue(response.getBody().contains(mov1));
        assertTrue(response.getBody().contains(mov4));
        assertFalse(response.getBody().contains(mov2)); // Filtrado: otro lote
        assertFalse(response.getBody().contains(mov3)); // Filtrado: lote null
        verify(loteRepository, times(1)).findById(loteId);
        verify(movimientoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getMovimientosByLote debe retornar 404 cuando el lote no existe")
    void testGetMovimientosByLote_NotFound() {
        // Arrange
        Long loteId = 999L;

        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(loteRepository.findById(loteId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<Movimiento>> response = controller.getMovimientosByLote(loteId, request);

        // Assert
        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(loteRepository, times(1)).findById(loteId);
        verify(movimientoRepository, times(0)).findAll(); // No debe llamarse
    }

    @Test
    @DisplayName("getLotesActivos debe retornar solo lotes con activo=true")
    void testGetLotesActivos() {
        // Arrange
        List<Lote> allLotes = new ArrayList<>();
        Lote lote1 = new Lote();
        lote1.setId(1L);
        lote1.setActivo(true);
        Lote lote2 = new Lote();
        lote2.setId(2L);
        lote2.setActivo(false);
        Lote lote3 = new Lote();
        lote3.setId(3L);
        lote3.setActivo(true);
        allLotes.add(lote1);
        allLotes.add(lote2);
        allLotes.add(lote3);

        when(securityContextService.getCurrentUser()).thenReturn(adminUser);
        when(loteRepository.findAll()).thenReturn(allLotes);

        // Act
        ResponseEntity<List<Lote>> response = controller.getLotesActivos(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size()); // Solo los activos
        assertTrue(response.getBody().stream().allMatch(Lote::getActivo));
        verify(loteRepository, times(1)).findAll();
        verify(auditorAccessLogger, times(1)).logReporteAccess(adminUser, "Consulta lotes activos", request);
    }

    @Test
    @DisplayName("getLotesActivos debe retornar lista vacía cuando no hay lotes activos")
    void testGetLotesActivos_Empty() {
        // Arrange
        List<Lote> allLotes = new ArrayList<>();
        Lote lote1 = new Lote();
        lote1.setActivo(false);
        allLotes.add(lote1);

        when(securityContextService.getCurrentUser()).thenReturn(auditorUser);
        when(loteRepository.findAll()).thenReturn(allLotes);

        // Act
        ResponseEntity<List<Lote>> response = controller.getLotesActivos(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
        verify(loteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getResumenPermisos debe retornar resumen de permisos del usuario")
    void testGetResumenPermisos() {
        // Arrange
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setNivel(6);
        User user = new User("admin", "password", adminRole);

        when(securityContextService.getCurrentUser()).thenReturn(user);

        // Act
        ResponseEntity<String> response = controller.getResumenPermisos(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("admin"));
        assertTrue(response.getBody().contains("ADMIN"));
        assertTrue(response.getBody().contains("nivel 6"));
        verify(auditorAccessLogger, times(1)).logReporteAccess(user, "Consulta permisos propios", request);
    }

    @Test
    @DisplayName("getResumenPermisos debe funcionar para usuario AUDITOR")
    void testGetResumenPermisos_Auditor() {
        // Arrange
        Role auditorRole = Role.fromEnum(RoleEnum.AUDITOR);
        auditorRole.setNivel(1);
        User user = new User("auditor", "password", auditorRole);

        when(securityContextService.getCurrentUser()).thenReturn(user);

        // Act
        ResponseEntity<String> response = controller.getResumenPermisos(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("auditor"));
        assertTrue(response.getBody().contains("AUDITOR"));
        assertTrue(response.getBody().contains("nivel 1"));
    }
}
