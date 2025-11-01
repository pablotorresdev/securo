package com.mb.conitrack.controller.cu;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.validation.AltaCompra;
import com.mb.conitrack.service.cu.AltaIngresoCompraService;

/**
 * Controller for handling purchase intake operations (Alta Ingreso Compra use case).
 * <p>
 * This controller manages the creation of new lots (Lote) from external purchase orders.
 * It handles the complete workflow from form display through validation to successful
 * lot creation with initial stock movements (Movimiento ALTA).
 * </p>
 * <p>
 * Main responsibilities:
 * <ul>
 *   <li>Display purchase intake form with product and supplier data</li>
 *   <li>Validate purchase data including bulto quantities and units</li>
 *   <li>Create new lot records with associated bultos and initial stock movement</li>
 *   <li>Handle success/error scenarios with appropriate redirects</li>
 * </ul>
 * </p>
 *
 * @see AltaIngresoCompraService
 * @see LoteDTO
 * @see AltaCompra validation group
 */
@Controller
@RequestMapping("/compras/alta")
public class AltaIngresoCompraController extends AbstractCuController {

    @Autowired
    private AltaIngresoCompraService ingresoCompraService;

    /**
     * Cancels the current purchase intake operation and redirects to home page.
     * <p>
     * This is a generic cancel handler that discards any form data and returns
     * the user to the main index page.
     * </p>
     *
     * @return redirect to home page ("/")
     */
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    /**
     * Displays the purchase intake form for creating a new lot from an external purchase.
     * <p>
     * This method initializes the model with all necessary data for the form:
     * <ul>
     *   <li>List of external products (raw materials, ingredients)</li>
     *   <li>List of external suppliers/vendors</li>
     *   <li>List of countries for origin selection</li>
     *   <li>Empty DTO with initialized collections for bulto data</li>
     * </ul>
     * </p>
     *
     * @param loteDTO the data transfer object for lot creation, bound to the form
     * @param model the Spring MVC model for passing data to the view
     * @return the view name "compras/alta/ingreso-compra"
     */
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/ingreso-compra")
    public String showIngresoCompra(@ModelAttribute("loteDTO") LoteDTO loteDTO, Model model) {
        initModelIngresoCompra(loteDTO, model);
        return "compras/alta/ingreso-compra";
    }

    /**
     * Processes the purchase intake form submission and creates a new lot with initial stock.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Validates the submitted lot data using {@link AltaCompra} validation group</li>
     *   <li>If validation fails, redisplays the form with error messages</li>
     *   <li>If validation succeeds, creates the lot entity with associated bultos</li>
     *   <li>Creates an initial ALTA stock movement (Movimiento)</li>
     *   <li>Redirects to success page with flash attributes</li>
     * </ol>
     * </p>
     * <p>
     * Validation includes:
     * <ul>
     *   <li>Required fields (product, supplier, dates, quantities)</li>
     *   <li>Bulto quantities must sum to total lot quantity</li>
     *   <li>Unit conversions must be valid and compatible</li>
     *   <li>Dates must be logically consistent</li>
     * </ul>
     * </p>
     *
     * @param loteDTO the validated lot data from the form
     * @param bindingResult validation results, contains errors if validation fails
     * @param model the Spring MVC model for repopulating the form on errors
     * @param redirectAttributes attributes to pass to the redirect target (success data)
     * @return redirect to success page on success, or the form view on validation errors
     */
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/ingreso-compra")
    public String ingresoCompra(
        @Validated(AltaCompra.class) @ModelAttribute("loteDTO") LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!ingresoCompraService.validarIngresoCompraInput(loteDTO, bindingResult)) {
            initModelIngresoCompra(loteDTO, model);
            return "compras/alta/ingreso-compra";
        }

        procesarIngresoCompra(loteDTO, redirectAttributes);
        return "redirect:/compras/alta/ingreso-compra-ok";
    }

    /**
     * Displays the success page after a purchase intake operation completes successfully.
     * <p>
     * This page shows the details of the newly created lot including:
     * <ul>
     *   <li>Generated lot code (codigoLote)</li>
     *   <li>Product and supplier information</li>
     *   <li>Quantity and bulto details</li>
     *   <li>Success message from flash attributes</li>
     * </ul>
     * The loteDTO is populated via flash attributes from the POST handler.
     * </p>
     *
     * @param loteDTO the newly created lot data passed via flash attributes
     * @return the view name "compras/alta/ingreso-compra-ok"
     */
    @GetMapping("/ingreso-compra-ok")
    public String exitoIngresoCompra(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "compras/alta/ingreso-compra-ok"; // Template Thymeleaf
    }

    /**
     * Initializes the model with all required data for the purchase intake form.
     * <p>
     * This method populates the model with reference data needed by the Thymeleaf template:
     * <ul>
     *   <li><b>productos</b> - List of external products (raw materials from suppliers)</li>
     *   <li><b>proveedores</b> - List of external suppliers/vendors</li>
     *   <li><b>paises</b> - List of countries for origin/manufacturer selection</li>
     *   <li><b>loteDTO</b> - The form backing object with initialized collections</li>
     * </ul>
     * </p>
     * <p>
     * If the loteDTO's bulto collections are null, they are initialized as empty ArrayLists
     * to prevent null pointer exceptions in the view when dynamically adding bultos.
     * </p>
     *
     * @param loteDTO the lot DTO to be bound to the form, will be modified if collections are null
     * @param model the Spring MVC model to populate with attributes
     */
    void initModelIngresoCompra(final LoteDTO loteDTO, final Model model) {
        model.addAttribute("productos", productoService.getProductosExternos());
        model.addAttribute("proveedores", proveedorService.getProveedoresExternos());

        if (loteDTO.getCantidadesBultos() == null) {
            loteDTO.setCantidadesBultos(new ArrayList<>());
        }
        if (loteDTO.getUnidadMedidaBultos() == null) {
            loteDTO.setUnidadMedidaBultos(new ArrayList<>());
        }
        model.addAttribute("loteDTO", loteDTO);
        model.addAttribute("paises", ingresoCompraService.getCountryList());
    }

    /**
     * Processes the validated lot data and creates the lot entity in the database.
     * <p>
     * This method delegates to {@link AltaIngresoCompraService#altaStockPorCompra(LoteDTO)}
     * which performs the following transactional operations:
     * <ol>
     *   <li>Creates the Lote entity with initial stock quantities</li>
     *   <li>Creates associated Bulto entities for each package/container</li>
     *   <li>Creates an initial Movimiento record (tipo=ALTA, motivo=COMPRA)</li>
     *   <li>Sets the lot status to the appropriate EstadoEnum value</li>
     *   <li>Returns the persisted lot as a DTO</li>
     * </ol>
     * </p>
     * <p>
     * The method sets the current timestamp on the DTO before processing and adds
     * the result to flash attributes for display on the success page. A success or
     * error message is also added based on the operation outcome.
     * </p>
     *
     * @param loteDTO the validated lot data to persist
     * @param redirectAttributes flash attributes for passing data to the redirect target
     */
    void procesarIngresoCompra(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {

        loteDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO resultDTO = ingresoCompraService.altaStockPorCompra(loteDTO);

        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Ingreso de stock por compra exitoso."
                : "Hubo un error en el ingreso de stock por compra.");
    }

}
