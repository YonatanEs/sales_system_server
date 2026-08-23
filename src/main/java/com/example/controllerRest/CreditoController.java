package com.example.controllerRest;

import com.example.DTO.DtoAbonoComprobante;
import com.example.DTO.DtoCredito;
import com.example.DTO.DtoFiltroHistorialabonos;
import com.example.DTO.DtoId;
import com.example.DTO.DtoIdPage;
import com.example.DTO.DtoIdVenta;
import com.example.DTO.DtoItemHistorialabonos;
import com.example.DTO.DtoRegistrarAbono;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.RespuestaPaginada;
import com.example.services.CreditoServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/creditos")
public class CreditoController {

    @Autowired
    private CreditoServices creditoServices;

    @PostMapping("/listarCreditos")
    public DtoCredito obtenerCreditoClientes(@RequestBody DtoIdPage dto) {
        return creditoServices.datosCredito(dto);
    }

    @PostMapping("/registrarAbonoEfectivo")
    public ResponseEntity<DtoResponseOb<DtoId>> registrarAbonoEfectivo(@RequestBody DtoRegistrarAbono dto) {
        DtoResponseOb<DtoId> response = creditoServices.registrarAbonoEfectivo(dto);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/registrarAbonoDeposito")
    public ResponseEntity<DtoResponseOb<DtoId>> registrarAbonoDeposito(
            @RequestParam(value = "creditoJson") String creditoJson,
            @RequestPart(value = "comprobante", required = false) MultipartFile comprobante) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            DtoRegistrarAbono abonoDeposito = mapper.readValue(creditoJson, DtoRegistrarAbono.class);

            DtoResponseOb<DtoId> response = creditoServices.registrarAbonoDeposito(abonoDeposito, comprobante);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new DtoResponseOb(false, e.getMessage(), null));
        }
    }

    @PostMapping("/obtenerDatosAbono")
    public ResponseEntity<DtoAbonoComprobante> obtenerDatosAbono(@RequestBody DtoId dtoId) {
        if (dtoId.getId() == null || dtoId.getId() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        DtoAbonoComprobante abonoComprobante = creditoServices.obtenerAbonoComprobante(dtoId);

        if (abonoComprobante == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(abonoComprobante);
    }
    
    @PostMapping("/listarAbonos")
    public ResponseEntity<Page<DtoItemHistorialabonos>> listarAbonos(@RequestBody DtoFiltroHistorialabonos dto) {
        Page<DtoItemHistorialabonos> listAbonos = creditoServices.listarAbonos(dto);
        
        return ResponseEntity.ok(listAbonos);
    }
    
    @PostMapping("/obtenerComprobanteDeposito")
    public ResponseEntity<Resource> obtenerComprobanteDeposito(@RequestBody DtoId idAbono){
        return creditoServices.obtenerComprobanteDeposito(idAbono);
    }
}
