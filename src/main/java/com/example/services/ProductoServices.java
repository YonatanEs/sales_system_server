package com.example.services;

import com.example.DTO.DtoAddStock;
import com.example.DTO.DtoResponse;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.Producto_tab;
import com.example.DTO.RegistrarProducto;
import com.example.DTO.ValorRequestPag;
import com.example.Repository.CategoriaRepository;
import com.example.Repository.MedidaRepository;
import com.example.Repository.ProductoRepository;
import com.example.Repository.ProveedorRepository;
import com.example.domain.Producto;
import com.example.domain.Proveedor;
import jakarta.transaction.Transactional;
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

    public ProductoServices(ProductoRepository productoRepository, ProveedorRepository proveedorRepository,
            MedidaRepository medidaRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.medidaRepository = medidaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public Page<Producto_tab> listarProductos(ValorRequestPag val) {
        Pageable pageable = PageRequest.of(val.getPage(), val.getSize());
        Page<Producto> productosPage;

        if (val.getBusqueda() != null || !val.getBusqueda().isEmpty()) {
            productosPage = productoRepository.findByCodigoContainingIgnoreCaseOrDescripcionContainingIgnoreCase(val.getBusqueda(), val.getBusqueda(), pageable);
        } else {
            productosPage = productoRepository.findAll(pageable);
        }

        return productosPage.map(p -> new Producto_tab(
                p.getId(),
                p.getCodigo(),
                p.getDescripcion(),
                p.getStock(),
                p.getPrecio_venta(),
                p.getPrecio_compra(),
                p.getCategoria().getId(),
                p.getCategoria().getNombre(),
                p.getMedida().getId(),
                p.getMedida().getNombre(),
                p.getProveedor().getId(),
                p.getProveedor().getNombre(),
                p.getEstado()
        ));
    }

    public DtoResponseOb<Producto_tab> registrarProducto(RegistrarProducto registrar) {
        if (productoRepository.existsByCodigo(registrar.getCodigo())) {
            return new DtoResponseOb(false, "¡Ya existe un producto con el mismo codigo!", null);
        }

        Producto producto = new Producto();
        producto.setCodigo(registrar.getCodigo());
        producto.setDescripcion(registrar.getDescripcion());
        producto.setPrecio_compra(registrar.getPrecio_compra());
        producto.setPrecio_venta(registrar.getPrecio_venta());
        producto.setStock(0);

        producto.setProveedor(proveedorRepository.findById(registrar.getId_proveedor())
                .orElse(null));
        producto.setMedida(medidaRepository.findById(registrar.getId_medida())
                .orElse(null));
        producto.setCategoria(categoriaRepository.findById(registrar.getId_categoria())
                .orElse(null));
        producto.setEstado("Activo");
        Producto p = productoRepository.save(producto);
        return new DtoResponseOb(true, "¡Producto registrado correctamente!", mapearAProductoTab(p));
    }
    
    @Transactional
    public DtoResponse addStock(DtoAddStock addStock) {
        productoRepository.aumentarStock(addStock.getId(), addStock.getStock());
        return new DtoResponse(true, "¡stock añadido correctamente!");
    }
    
    public Producto_tab productoSelected(Long id){
        return mapearAProductoTab(productoRepository.findById(id).orElse(null));
    }
    
    public List<String> listarSugerencias(){
        return productoRepository.listarSugerencias();
    }

    private Producto_tab mapearAProductoTab(Producto p) {
        return new Producto_tab(
                p.getId(),
                p.getCodigo(),
                p.getDescripcion(),
                p.getStock(),
                p.getPrecio_venta(),
                p.getPrecio_compra(),
                p.getCategoria().getId(),
                p.getCategoria().getNombre(),
                p.getMedida().getId(),
                p.getMedida().getNombre(),
                p.getProveedor().getId(),
                p.getProveedor().getNombre(),
                p.getEstado()
        );
    }
    
}
