
package com.example.controllerRest;

import com.example.DTO.DtoResponse;
import com.example.DTO.ModificarProveedor;
import com.example.DTO.RegistrarProveedor;
import com.example.domain.Proveedor;
import com.example.services.ProveedorServices;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/proveedores")
public class ProveedorController {

    private ProveedorServices proveedorServices;

    public ProveedorController(ProveedorServices proveedorServices) {
        this.proveedorServices = proveedorServices;
    }

    @PostMapping("/registrar")
    public ResponseEntity<DtoResponse> registrarProveedor(@RequestBody RegistrarProveedor request) {
        DtoResponse response = proveedorServices.registrarProveedor(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/modificar")
    public ResponseEntity<DtoResponse> modificarProveedor(@RequestBody ModificarProveedor request) {
        DtoResponse response = proveedorServices.modificarProveedor(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/listarProveedor")
    public List<Proveedor> listarProveedor(){
        return proveedorServices.listarProveedores();
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

    @PostMapping("/proveedorSelected")
    public Proveedor proveedorSelected(@RequestBody Long id) {
        return proveedorServices.proveedorSelected(id);
    }

}
