package com.example.controllerRest;

import com.example.DTO.DtoAddStock;
import com.example.DTO.DtoResponse;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.Producto_tab;
import com.example.DTO.RegistrarProducto;
import com.example.DTO.ValorRequestPag;
import com.example.services.ProductoServices;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/productos")
public class ProductoController {
    
    private ProductoServices productoServices;
    
    public ProductoController(ProductoServices productoServices){
        this.productoServices = productoServices;
    }
    
    @PostMapping("/listarProductos")
    public Page<Producto_tab> listarProductos(@RequestBody ValorRequestPag val){
        System.out.println("Busqueda: "+val.getBusqueda());
        System.out.println("Pagina: "+val.getPage());
        System.out.println("Tamaño pagina: "+val.getSize());
        return productoServices.listarProductos(val);
    }
    
    @PostMapping("/registrar")
    public ResponseEntity<DtoResponseOb<Producto_tab>> registrarProducto(@RequestBody RegistrarProducto request){
        DtoResponseOb<Producto_tab> response = productoServices.registrarProducto(request);
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/añadirstock")
    public ResponseEntity<DtoResponse> addStock(@RequestBody DtoAddStock addStock){
        DtoResponse response = productoServices.addStock(addStock);
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/productoSelected")
    public Producto_tab productoSelected(@RequestBody Long id){
        return productoServices.productoSelected(id);
    }
    
    @GetMapping("/listarSugerencias")
    public List<String> listarSugerencias(){
        return productoServices.listarSugerencias();
    }
    
}
