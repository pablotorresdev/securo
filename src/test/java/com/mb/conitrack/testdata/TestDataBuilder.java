package com.mb.conitrack.testdata;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder centralizado para crear objetos de test con datos realistas del dominio farmacéutico.
 * Proporciona valores por defecto coherentes y permite personalización fluida.
 */
public class TestDataBuilder {

    // ============ CONSTANTES DE DATOS DE TEST ============

    // Productos farmacéuticos
    public static final String PRODUCTO_API_CODIGO = "API-001";
    public static final String PRODUCTO_API_NOMBRE = "Paracetamol";
    public static final Long PRODUCTO_API_ID = 1L;

    // Proveedores
    public static final String PROVEEDOR_SIGMA_NOMBRE = "Sigma-Aldrich";
    public static final String PROVEEDOR_SIGMA_PAIS = "Argentina";
    public static final Long PROVEEDOR_SIGMA_ID = 100L;

    // Fabricantes
    public static final String FABRICANTE_MERCK_NOMBRE = "Merck";
    public static final String FABRICANTE_MERCK_PAIS = "Alemania";
    public static final Long FABRICANTE_MERCK_ID = 200L;

    // ============ BUILDERS ============

    /**
     * Builder para LoteDTO con valores por defecto válidos para alta de compra.
     */
    public static class LoteDTOBuilder {
        private LoteDTO dto = new LoteDTO();

        public LoteDTOBuilder() {
            // Valores por defecto válidos
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setFechaIngreso(LocalDate.now());
            dto.setProductoId(PRODUCTO_API_ID);
            dto.setProveedorId(PROVEEDOR_SIGMA_ID);
            dto.setFabricanteId(FABRICANTE_MERCK_ID);
            dto.setPaisOrigen(FABRICANTE_MERCK_PAIS);
            dto.setLoteProveedor("LP-2024-001");
            dto.setCantidadInicial(new BigDecimal("25.0"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setBultosTotales(1);
            dto.setNroRemito("REM-001");
            dto.setFechaReanalisisProveedor(LocalDate.now().plusMonths(6));
            dto.setFechaVencimientoProveedor(LocalDate.now().plusYears(2));
            dto.setDetalleConservacion("Almacenar en lugar fresco y seco");
            dto.setObservaciones("Test lote");
            dto.setCantidadesBultos(new ArrayList<>());
            dto.setUnidadMedidaBultos(new ArrayList<>());
        }

        public LoteDTOBuilder withFechaIngreso(LocalDate fecha) {
            dto.setFechaIngreso(fecha);
            return this;
        }

        public LoteDTOBuilder withProductoId(Long id) {
            dto.setProductoId(id);
            return this;
        }

        public LoteDTOBuilder withProveedorId(Long id) {
            dto.setProveedorId(id);
            return this;
        }

        public LoteDTOBuilder withFabricanteId(Long id) {
            dto.setFabricanteId(id);
            return this;
        }

        public LoteDTOBuilder withCantidadInicial(BigDecimal cantidad) {
            dto.setCantidadInicial(cantidad);
            return this;
        }

        public LoteDTOBuilder withUnidadMedida(UnidadMedidaEnum unidad) {
            dto.setUnidadMedida(unidad);
            return this;
        }

        public LoteDTOBuilder withBultosTotales(Integer bultos) {
            dto.setBultosTotales(bultos);
            return this;
        }

        public LoteDTOBuilder withLoteProveedor(String lote) {
            dto.setLoteProveedor(lote);
            return this;
        }

        public LoteDTOBuilder withCantidadesBultos(List<BigDecimal> cantidades) {
            dto.setCantidadesBultos(cantidades);
            return this;
        }

        public LoteDTOBuilder withUnidadMedidaBultos(List<UnidadMedidaEnum> unidades) {
            dto.setUnidadMedidaBultos(unidades);
            return this;
        }

        public LoteDTOBuilder withFechaReanalisisProveedor(LocalDate fecha) {
            dto.setFechaReanalisisProveedor(fecha);
            return this;
        }

        public LoteDTOBuilder withFechaVencimientoProveedor(LocalDate fecha) {
            dto.setFechaVencimientoProveedor(fecha);
            return this;
        }

        public LoteDTOBuilder withPaisOrigen(String pais) {
            dto.setPaisOrigen(pais);
            return this;
        }

        public LoteDTOBuilder withObservaciones(String obs) {
            dto.setObservaciones(obs);
            return this;
        }

        public LoteDTOBuilder withTrazaInicial(Long trazaInicial) {
            dto.setTrazaInicial(trazaInicial);
            return this;
        }

        public LoteDTO build() {
            return dto;
        }
    }

    /**
     * Builder para Producto farmacéutico.
     */
    public static class ProductoBuilder {
        private Producto producto = new Producto();

        public ProductoBuilder() {
            producto.setId(PRODUCTO_API_ID);
            producto.setCodigoProducto(PRODUCTO_API_CODIGO);
            producto.setNombreGenerico(PRODUCTO_API_NOMBRE);
            producto.setTipoProducto(TipoProductoEnum.API);
            producto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            producto.setActivo(true);
        }

        public ProductoBuilder withId(Long id) {
            producto.setId(id);
            return this;
        }

        public ProductoBuilder withCodigo(String codigo) {
            producto.setCodigoProducto(codigo);
            return this;
        }

        public ProductoBuilder withNombre(String nombre) {
            producto.setNombreGenerico(nombre);
            return this;
        }

        public ProductoBuilder withTipo(TipoProductoEnum tipo) {
            producto.setTipoProducto(tipo);
            return this;
        }

        public ProductoBuilder withUnidadMedida(UnidadMedidaEnum unidad) {
            producto.setUnidadMedida(unidad);
            return this;
        }

        public Producto build() {
            return producto;
        }
    }

    /**
     * Builder para Proveedor/Fabricante.
     */
    public static class ProveedorBuilder {
        private Proveedor proveedor = new Proveedor();

        public ProveedorBuilder() {
            proveedor.setId(PROVEEDOR_SIGMA_ID);
            proveedor.setRazonSocial(PROVEEDOR_SIGMA_NOMBRE);
            proveedor.setPais(PROVEEDOR_SIGMA_PAIS);
            proveedor.setCuit("20-12345678-9");
            proveedor.setDireccion("Direccion Test 123");
            proveedor.setCiudad("Buenos Aires");
            proveedor.setActivo(true);
        }

        public ProveedorBuilder withId(Long id) {
            proveedor.setId(id);
            return this;
        }

        public ProveedorBuilder withNombre(String nombre) {
            proveedor.setRazonSocial(nombre);
            return this;
        }

        public ProveedorBuilder withPais(String pais) {
            proveedor.setPais(pais);
            return this;
        }

        public ProveedorBuilder withCuit(String cuit) {
            proveedor.setCuit(cuit);
            return this;
        }

        public Proveedor build() {
            return proveedor;
        }
    }

    /**
     * Builder para entidad Lote.
     */
    public static class LoteBuilder {
        private Lote lote = new Lote();

        public LoteBuilder() {
            lote.setCodigoLote("L-API-001-25.01.01_10.00.00");
            lote.setFechaYHoraCreacion(OffsetDateTime.now());
            lote.setFechaIngreso(LocalDate.now());
            lote.setEstado(EstadoEnum.NUEVO);
            lote.setDictamen(DictamenEnum.RECIBIDO);
            lote.setActivo(true);
            lote.setCantidadInicial(new BigDecimal("25.0"));
            lote.setCantidadActual(new BigDecimal("25.0"));
            lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            lote.setBultosTotales(1);
            lote.setLoteProveedor("LP-2024-001");
            lote.setPaisOrigen(FABRICANTE_MERCK_PAIS);
        }

        public LoteBuilder withCodigoLote(String codigo) {
            lote.setCodigoLote(codigo);
            return this;
        }

        public LoteBuilder withProducto(Producto producto) {
            lote.setProducto(producto);
            return this;
        }

        public LoteBuilder withProveedor(Proveedor proveedor) {
            lote.setProveedor(proveedor);
            return this;
        }

        public LoteBuilder withFabricante(Proveedor fabricante) {
            lote.setFabricante(fabricante);
            return this;
        }

        public LoteBuilder withCantidadInicial(BigDecimal cantidad) {
            lote.setCantidadInicial(cantidad);
            lote.setCantidadActual(cantidad);
            return this;
        }

        public LoteBuilder withUnidadMedida(UnidadMedidaEnum unidad) {
            lote.setUnidadMedida(unidad);
            return this;
        }

        public LoteBuilder withBultosTotales(Integer bultos) {
            lote.setBultosTotales(bultos);
            return this;
        }

        public Lote build() {
            return lote;
        }
    }

    // ============ MÉTODOS FACTORY ESTÁTICOS ============

    public static LoteDTOBuilder unLoteDTO() {
        return new LoteDTOBuilder();
    }

    public static ProductoBuilder unProducto() {
        return new ProductoBuilder();
    }

    public static ProveedorBuilder unProveedor() {
        return new ProveedorBuilder();
    }

    public static ProveedorBuilder unFabricante() {
        return new ProveedorBuilder()
                .withId(FABRICANTE_MERCK_ID)
                .withNombre(FABRICANTE_MERCK_NOMBRE)
                .withPais(FABRICANTE_MERCK_PAIS);
    }

    public static LoteBuilder unLote() {
        return new LoteBuilder();
    }
}
