package com.mb.conitrack.service.cu;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;

import static com.mb.conitrack.utils.LoteEntityUtils.createLoteIngreso;
import static com.mb.conitrack.utils.LoteEntityUtils.populateLoteAltaStockCompra;
import static com.mb.conitrack.utils.MovimientoEntityUtils.addLoteInfoToMovimientoAlta;
import static com.mb.conitrack.utils.MovimientoEntityUtils.createMovimientoAltaIngresoCompra;

/**
 * Service for handling purchase intake operations (CU1 - Alta: Compra).
 * <p>
 * This service implements the business logic for creating new lots from external purchases.
 * It manages the complete transaction including lot creation, bulto (package) initialization,
 * and initial stock movement registration.
 * </p>
 * <p>
 * <b>Use Case CU1 - Alta Ingreso Compra:</b>
 * <ul>
 *   <li>Creates a new {@link Lote} entity from purchase order data</li>
 *   <li>Initializes {@link com.mb.conitrack.entity.Bulto} entities for individual packages</li>
 *   <li>Creates an initial {@link Movimiento} record (tipo=ALTA, motivo=COMPRA)</li>
 *   <li>Validates all business rules before persistence</li>
 * </ul>
 * </p>
 * <p>
 * This service extends {@link AbstractCuService} which provides common validation methods
 * and repository access for all use case services.
 * </p>
 *
 * @see AbstractCuService
 * @see Lote
 * @see Movimiento
 * @see com.mb.conitrack.entity.Bulto
 */
//***********CU1 ALTA: COMPRA***********
@Service
public class AltaIngresoCompraService extends AbstractCuService {

    /**
     * Creates a new lot with initial stock from a purchase order in a single database transaction.
     * <p>
     * This method orchestrates the complete lot creation process:
     * <ol>
     *   <li><b>Entity Lookup:</b> Retrieves Producto, Proveedor, and optionally Fabricante entities</li>
     *   <li><b>Lot Creation:</b> Creates Lote entity with initial quantities and status</li>
     *   <li><b>Bulto Creation:</b> Creates individual package entities for the lot</li>
     *   <li><b>Movement Creation:</b> Creates initial ALTA movement record with tipo=ALTA, motivo=COMPRA</li>
     *   <li><b>Persistence:</b> Saves all entities in cascading order</li>
     * </ol>
     * </p>
     * <p>
     * <b>Entity Relationships Created:</b>
     * <pre>
     * Lote (new lot)
     *  ├─ producto (ManyToOne) → Producto entity
     *  ├─ proveedor (ManyToOne) → Proveedor entity
     *  ├─ fabricante (ManyToOne, optional) → Proveedor entity
     *  ├─ bultos (OneToMany) → List of Bulto entities
     *  └─ movimientos (OneToMany) → Initial Movimiento ALTA
     * </pre>
     * </p>
     * <p>
     * <b>Initial Lot Status:</b><br>
     * The newly created lot will have:
     * <ul>
     *   <li>estado = determined by business rules (typically VIGENTE or CUARENTENA)</li>
     *   <li>dictamen = PENDIENTE (no analysis yet)</li>
     *   <li>activo = true</li>
     *   <li>cantidadInicial = cantidadActual (no consumption yet)</li>
     *   <li>trazado = false (traceability not yet established)</li>
     * </ul>
     * </p>
     * <p>
     * <b>Transaction Behavior:</b><br>
     * All database operations are performed in a single transaction. If any step fails
     * (e.g., invalid product ID, constraint violation), the entire transaction is rolled back.
     * </p>
     *
     * @param loteDTO the data transfer object containing validated lot information including:
     *                <ul>
     *                  <li>productoId - required, must exist in database</li>
     *                  <li>proveedorId - required, must exist in database</li>
     *                  <li>fabricanteId - optional, must exist if provided</li>
     *                  <li>cantidadInicial and bulto details - must be validated</li>
     *                  <li>fechaIngreso, fechaVencimientoProveedor, etc.</li>
     *                </ul>
     * @return the persisted lot converted to DTO with generated IDs and relationships
     * @throws IllegalArgumentException if producto, proveedor, or fabricante ID doesn't exist
     * @throws org.springframework.dao.DataIntegrityViolationException if database constraints are violated
     * @see #validarIngresoCompraInput(LoteDTO, BindingResult) for validation before calling this method
     */
    @Transactional
    public LoteDTO altaStockPorCompra(LoteDTO loteDTO) {
        Proveedor proveedor = proveedorRepository.findById(loteDTO.getProveedorId())
            .orElseThrow(() -> new IllegalArgumentException("El proveedor no existe."));

        Producto producto = productoRepository.findById(loteDTO.getProductoId())
            .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        Optional<Proveedor> fabricante = loteDTO.getFabricanteId() != null
            ? proveedorRepository.findById(loteDTO.getFabricanteId())
            : Optional.empty();

        Lote lote = createLoteIngreso(loteDTO);
        populateLoteAltaStockCompra(lote, loteDTO, producto, proveedor, fabricante.orElse(null));

        Lote loteGuardado = loteRepository.save(lote);
        bultoRepository.saveAll(loteGuardado.getBultos());

        final Movimiento movimientoAltaIngresoCompra = createMovimientoAltaIngresoCompra(loteGuardado);
        addLoteInfoToMovimientoAlta(loteGuardado, movimientoAltaIngresoCompra);

        final Movimiento movimientoGuardado = movimientoRepository.save(movimientoAltaIngresoCompra);
        loteGuardado.getMovimientos().add(movimientoGuardado);

        return DTOUtils.fromLoteEntity(loteGuardado);
    }

    /**
     * Validates all business rules for purchase intake input before lot creation.
     * <p>
     * This method performs comprehensive validation of the purchase data, checking both
     * standard field validation and complex business rules specific to purchase operations.
     * It delegates to validation methods inherited from {@link AbstractCuService}.
     * </p>
     * <p>
     * <b>Validation Rules Applied:</b>
     * <ol>
     *   <li><b>Spring Validation:</b> Checks @Valid constraints on DTO fields</li>
     *   <li><b>Quantity Validation:</b> Via {@link #validarCantidadIngreso(LoteDTO, BindingResult)}
     *     <ul>
     *       <li>cantidadInicial must be positive</li>
     *       <li>If unidadMedida = UNIDAD, quantity must be integer</li>
     *       <li>For UNIDAD, quantity must be ≥ bultosTotales</li>
     *     </ul>
     *   </li>
     *   <li><b>Date Validation:</b> Via {@link #validarFechasProveedor(LoteDTO, BindingResult)}
     *     <ul>
     *       <li>fechaReanalisisProveedor must be ≤ fechaVencimientoProveedor</li>
     *       <li>Both dates must be logically consistent</li>
     *     </ul>
     *   </li>
     *   <li><b>Bulto Validation:</b> Via {@link #validarBultos(LoteDTO, BindingResult)}
     *     <ul>
     *       <li>If bultosTotales = 1, no additional validation needed</li>
     *       <li>If bultosTotales > 1, validates data types and sum consistency</li>
     *       <li>Sum of bulto quantities (converted) must equal cantidadInicial</li>
     *       <li>All bulto quantities must be positive</li>
     *       <li>Unit conversions must be valid</li>
     *     </ul>
     *   </li>
     * </ol>
     * </p>
     * <p>
     * <b>Short-circuit Behavior:</b><br>
     * Validation stops at the first failing rule. If any validation fails, errors are
     * added to the BindingResult and the method returns false without checking subsequent rules.
     * </p>
     * <p>
     * <b>Usage Pattern:</b>
     * <pre>
     * if (!validarIngresoCompraInput(dto, bindingResult)) {
     *     // Re-display form with errors
     *     return "compras/alta/ingreso-compra";
     * }
     * // Proceed with lot creation
     * altaStockPorCompra(dto);
     * </pre>
     * </p>
     *
     * @param dto the lot DTO containing purchase data to validate
     * @param bindingResult Spring validation results, will be populated with field errors if validation fails
     * @return true if all validations pass, false if any validation fails (errors added to bindingResult)
     * @see AbstractCuService#validarCantidadIngreso(LoteDTO, BindingResult)
     * @see AbstractCuService#validarFechasProveedor(LoteDTO, BindingResult)
     * @see AbstractCuService#validarBultos(LoteDTO, BindingResult)
     */
    @Transactional
    public boolean validarIngresoCompraInput(final LoteDTO dto, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        if (!validarCantidadIngreso(dto, bindingResult)) {
            return false;
        }
        if (!validarFechasProveedor(dto, bindingResult)) {
            return false;
        }

        return validarBultos(dto, bindingResult);
    }

}

