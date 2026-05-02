package com.example.controllerRest;

import com.example.DTO.DtoResponse;
import com.example.DTO.ModificarCategoria;
import com.example.DTO.RegistrarCategoria;
import com.example.domain.Categoria;
import com.example.domain.Proveedor;
import com.example.services.CategoriaServices;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/categorias")
public class CategoriaController {
    
    private CategoriaServices categoriaServices;

    public CategoriaController(CategoriaServices categoriaServices) {
        this.categoriaServices = categoriaServices;
    }

    @PostMapping("/registrar")
    public ResponseEntity<DtoResponse> registrarCategoria(@RequestBody RegistrarCategoria request) {
        DtoResponse response = categoriaServices.registrarCategoria(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/modificar")
    public ResponseEntity<DtoResponse> modificarCategoria(@RequestBody ModificarCategoria request) {
        DtoResponse response = categoriaServices.modificarCategoria(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/listarCategoria")
    public List<Categoria> listarCategoria(){
        return categoriaServices.listarCategoria();
    }
    
    /*@GetMapping("/listarProveedor")
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(proveedorServices.listarProveedores());
        } catch (Exception e) {
            e.printStackTrace(); // Esto imprimirá el error REAL en la consola roja de IntelliJ
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }*/

    @PostMapping("/categoriaSelected")
    public Categoria categoriaSelected(@RequestBody Long id) {
        return categoriaServices.categoriaSelected(id);
    }

    
}
