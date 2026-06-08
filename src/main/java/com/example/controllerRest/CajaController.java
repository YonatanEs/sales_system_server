package com.example.controllerRest;

import com.example.DTO.DtoResponse;
import com.example.DTO.ModificarCaja;
import com.example.DTO.RegistrarCaja;
import com.example.DTO.ValorRequest;
import com.example.domain.Caja;
import com.example.services.CajaServices;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/cajas")
public class CajaController {

    private CajaServices cajaServices;

    public CajaController(CajaServices cajaServices) {
        this.cajaServices = cajaServices;
    }

    @PostMapping("/ListaCajas")
    public List<Caja> listarCaja(@RequestBody ValorRequest valor) {

        List<Caja> listaCajas = cajaServices.listarCajas(valor.getValor());

        return listaCajas;
    }

    @PostMapping("/registrar")
    public ResponseEntity<DtoResponse> registrarCaja(@RequestBody RegistrarCaja request) {

        DtoResponse response = cajaServices.registrarCaja(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/modificar")
    public ResponseEntity<DtoResponse> modificarCaja(@RequestBody ModificarCaja request) {

        DtoResponse response = cajaServices.modificarCaja(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/inactivar")
    public ResponseEntity<DtoResponse> inactivarCaja(@RequestBody Long id) {

        DtoResponse response = cajaServices.inactivarCaja(id);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/CajaSeleccionada")
    public Caja cajaSeleccionada(@RequestBody Long id) {
        return cajaServices.cajaSeleccionada(id);
    }

    @GetMapping("/listarSugerencias")
    public List<String> listarSugerencias() {
        return cajaServices.listarSugerencias();
    }

}
