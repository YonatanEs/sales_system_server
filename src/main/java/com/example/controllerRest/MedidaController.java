package com.example.controllerRest;

import com.example.DTO.DtoResponse;
import com.example.DTO.ModificarMedida;
import com.example.DTO.RegistrarMedida;
import com.example.domain.Medida;
import com.example.services.MedidaServices;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/medidas")
public class MedidaController {
        
    private MedidaServices medidaServices;

    public MedidaController(MedidaServices medidaServices) {
        this.medidaServices = medidaServices;
    }

    @PostMapping("/registrar")
    public ResponseEntity<DtoResponse> registrarMedida(@RequestBody RegistrarMedida request) {
        DtoResponse response = medidaServices.registrarMedida(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/modificar")
    public ResponseEntity<DtoResponse> modificarMedida(@RequestBody ModificarMedida request) {
        DtoResponse response = medidaServices.modificarMedida(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/listarMedida")
    public List<Medida> listarMedida(){
        return medidaServices.listarMedida();
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

    @PostMapping("/medidaSelected")
    public Medida mediaSelected(@RequestBody Long id) {
        return medidaServices.medidaSelected(id);
    }
    
}
