package com.example.services;

import com.example.DTO.DtoAddStock;
import com.example.DTO.DtoItemSugerenciaProductos;
import com.example.DTO.DtoResponse;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.ModificarProducto;
import com.example.DTO.Producto_tab;
import com.example.DTO.RegistrarProducto;
import com.example.DTO.ValorRequestPag;
import com.example.Repository.CategoriaRepository;
import com.example.Repository.LoteStockRepository;
import com.example.Repository.MedidaRepository;
import com.example.Repository.ProductoRepository;
import com.example.Repository.ProveedorRepository;
import com.example.domain.Categoria;
import com.example.domain.Cliente;
import com.example.domain.LoteStock;
import com.example.domain.Medida;
import com.example.domain.Producto;
import com.example.domain.Proveedor;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
//@RequiredArgsConstructor
public class ProductoServices {

    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final MedidaRepository medidaRepository;
    private final CategoriaRepository categoriaRepository;
    private final LoteStockRepository loteStockRepository;

    public ProductoServices(ProductoRepository productoRepository, ProveedorRepository proveedorRepository,
            MedidaRepository medidaRepository, CategoriaRepository categoriaRepository, LoteStockRepository loteStockRepository) {
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.medidaRepository = medidaRepository;
        this.categoriaRepository = categoriaRepository;
        this.loteStockRepository = loteStockRepository;
    }

    public Page<Producto_tab> listarProductos(ValorRequestPag val) {
        Pageable pageable = PageRequest.of(val.getPage(), val.getSize());
        Page<Producto> productosPage;

        if (val.getBusqueda() != null && !val.getBusqueda().isEmpty()) {
            productosPage = productoRepository.findByCodigoContainingIgnoreCaseOrDescripcionContainingIgnoreCase(val.getBusqueda(), val.getBusqueda(), pageable);
        } else {
            productosPage = productoRepository.findAll(pageable);
        }

        return productosPage.map(p -> {

            // 1. Calculamos stock total y el costo del lote más reciente
            List<LoteStock> lotesActivos = loteStockRepository
                    .findByIdProductoAndEstadoAndStockActualGreaterThanOrderByFechaEntradaAsc(p.getId(), "Activo", BigDecimal.ZERO);

            BigDecimal stockTotal = BigDecimal.ZERO;
            BigDecimal ultimoPrecioCompra = BigDecimal.ZERO;

            if (!lotesActivos.isEmpty()) {
                for (LoteStock lote : lotesActivos) {
                    stockTotal = stockTotal.add(lote.getStockActual());
                    ultimoPrecioCompra = lote.getPrecioCompra(); // Se queda con el costo del lote más nuevo procesado
                }
            } else {
                ultimoPrecioCompra = loteStockRepository.
                        findFirstByIdProductoOrderByIdDesc(p.getId())
                        .map(LoteStock::getPrecioCompra)
                        .orElse(BigDecimal.ZERO);
            }

            // 2. Retornamos el DTO usando tus objetos mapeados directamente
            return new Producto_tab(
                    p.getId(),
                    p.getCodigo(),
                    p.getDescripcion(),
                    stockTotal,
                    p.getPrecioVenta(),
                    ultimoPrecioCompra,
                    p.getCategoria() != null ? new Categoria(p.getCategoria().getId(), p.getCategoria().getNombre()) : null,
                    p.getMedida() != null ? new Medida(p.getMedida().getId(), p.getMedida().getNombre(), p.getMedida().getAbreviatura()) : null,
                    p.getProveedor() != null ? new Proveedor(p.getProveedor().getId(), p.getProveedor().getNombre()) : null,
                    p.getEstado()
            );
        });
    }

    public DtoResponseOb<Producto_tab> registrarProducto(RegistrarProducto registrar) {
        if (productoRepository.existsByCodigo(registrar.getCodigo())) {
            return new DtoResponseOb(false, "¡Ya existe un producto con el mismo codigo!", null);
        }

        Producto producto = new Producto();
        producto.setCodigo(registrar.getCodigo());
        producto.setDescripcion(registrar.getDescripcion());
        producto.setPrecioVenta(registrar.getPrecioVenta());

        producto.setProveedor(proveedorRepository.findById(registrar.getId_proveedor())
                .orElse(null));
        producto.setMedida(medidaRepository.findById(registrar.getId_medida())
                .orElse(null));
        producto.setCategoria(categoriaRepository.findById(registrar.getId_categoria())
                .orElse(null));
        producto.setEstado("Activo");
        Producto p = productoRepository.save(producto);
        return new DtoResponseOb(true, "¡Producto registrado correctamente!", productoSelected(p.getId()));
    }
    
    public DtoResponse modificarProducto(ModificarProducto modificar){
        if(productoRepository.existsByCodigoAndIdNot(modificar.getCodigo(), modificar.getId())){
            return new DtoResponse(false, "¡Ya existe un producto con el mismo codigo!");
        }
        
        Optional<Producto> pOpt = productoRepository.findById(modificar.getId());
        if(pOpt.isEmpty()){
            return new DtoResponse(false, "Selecciona una fila");
        }
        
        Producto p = pOpt.get();
        p.setCodigo(modificar.getCodigo());
        p.setDescripcion(modificar.getDescripcion());
        p.setPrecioVenta(modificar.getPrecioVenta());

        p.setProveedor(proveedorRepository.findById(modificar.getId_proveedor())
                .orElse(null));
        p.setMedida(medidaRepository.findById(modificar.getId_medida())
                .orElse(null));
        p .setCategoria(categoriaRepository.findById(modificar.getId_categoria())
                .orElse(null));
        productoRepository.save(p);
        return new DtoResponse(true, "¡Producto modificado correctamente!");
    } 

    /*@Transactional
    public DtoResponse addStock(DtoAddStock addStock) {
        productoRepository.aumentarStock(addStock.getId(), addStock.getStock());
        return new DtoResponse(true, "¡stock añadido correctamente!");
    }*/
    public Producto_tab productoSelected(Long id) {
        Producto p = productoRepository.findById(id).orElse(null);

        List<LoteStock> lotesActivos = loteStockRepository
                .findByIdProductoAndEstadoAndStockActualGreaterThanOrderByFechaEntradaAsc(p.getId(), "Activo", BigDecimal.ZERO);

        BigDecimal stockTotal = BigDecimal.ZERO;
        BigDecimal ultimoPrecioCompra = BigDecimal.ZERO;

        if (!lotesActivos.isEmpty()) {
            for (LoteStock lote : lotesActivos) {
                stockTotal = stockTotal.add(lote.getStockActual());
                ultimoPrecioCompra = lote.getPrecioCompra(); // Se queda con el costo del lote más nuevo procesado
            }
        } else {
            ultimoPrecioCompra = loteStockRepository.
                    findFirstByIdProductoOrderByIdDesc(p.getId())
                    .map(LoteStock::getPrecioCompra)
                    .orElse(BigDecimal.ZERO);
        }

        return new Producto_tab(
                p.getId(),
                p.getCodigo(),
                p.getDescripcion(),
                stockTotal,
                p.getPrecioVenta(),
                ultimoPrecioCompra,
                p.getCategoria() != null ? new Categoria(p.getCategoria().getId(), p.getCategoria().getNombre()) : null,
                p.getMedida() != null ? new Medida(p.getMedida().getId(), p.getMedida().getNombre(), p.getMedida().getAbreviatura()) : null,
                p.getProveedor() != null ? new Proveedor(p.getProveedor().getId(), p.getProveedor().getNombre()) : null,
                p.getEstado()
        );
    }

    public List<String> listarSugerencias() {
        return productoRepository.listarSugerencias();
    }
    
    public List<DtoItemSugerenciaProductos> listarSugerenciasOb() {
        List<DtoItemSugerenciaProductos> codigos = productoRepository.obtenerSugerenciasCodigo();
        List<DtoItemSugerenciaProductos> descripcion = productoRepository.obtenerSugerenciasDescripcion();
        
        List<DtoItemSugerenciaProductos> total = new ArrayList<>(codigos.size() + descripcion.size());
        
        total.addAll(codigos);
        total.addAll(descripcion);
        
        return total;
    }

    public DtoResponse inactivarProducto(Long id) {
        System.out.println("Se está ejecutando el comando inactivar");
        Optional<Producto> productoOp = productoRepository.findById(id);

        if (productoOp.isEmpty()) {
            return new DtoResponse(false, "Selecciona una fila");
        }
        Producto producto = productoOp.get();

        if (producto.getEstado().equals("Activo")) {
            producto.setEstado("Inactivo");
            productoRepository.save(producto);
            return new DtoResponse(true, "¡Producto Inactivado correctamente!");
        } else {
            producto.setEstado("Activo");
            productoRepository.save(producto);
            return new DtoResponse(true, "¡Producto Activado correctamente!");
        }
    }

}
