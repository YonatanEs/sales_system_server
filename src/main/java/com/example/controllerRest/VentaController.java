package com.example.controllerRest;

import com.example.DTO.DtoFiltroHistorialVentas;
import com.example.DTO.DtoHistorialVentas;
import com.example.DTO.DtoIdVenta;
import com.example.DTO.DtoRecibo;
import com.example.DTO.DtoVentaContado;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.DtoVentaCredito;
import com.example.DTO.DtoVentaDeposito;
import com.example.services.VentaServices;
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
@RequestMapping("api/ventas")
public class VentaController {
    
    @Autowired
    private VentaServices ventaServices;
    
    @PostMapping("/registarVentaContado")
    public ResponseEntity<DtoResponseOb<DtoIdVenta>> registrarVentaContado(@RequestBody DtoVentaContado registrar){
        DtoResponseOb<DtoIdVenta> response = ventaServices.RegistrarVentaAlContado(registrar);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/registrarVentaDeposito")
    public ResponseEntity<DtoResponseOb<DtoIdVenta>> registrarVentaDeposito(
            @RequestParam(value = "ventaJson") String datosJson,
            @RequestPart(value = "comprobante", required = false) MultipartFile comprobante) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            DtoVentaDeposito ventaDeposito = mapper.readValue(datosJson, DtoVentaDeposito.class);

            DtoResponseOb<DtoIdVenta> response = ventaServices.RegistrarVentaConDeposito(ventaDeposito, comprobante);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new DtoResponseOb(false, e.getMessage(), null));
        }
    }
    
    @PostMapping("/registarVentaCredito")
    public ResponseEntity<DtoResponseOb<DtoIdVenta>> registrarVenta(@RequestBody DtoVentaCredito registrar){
        DtoResponseOb<DtoIdVenta> response = ventaServices.RegistrarVentasCredito(registrar);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/obtenerRecibo")
    public DtoRecibo obtenerRecibo(@RequestBody DtoIdVenta idVenta){
        return ventaServices.obtenerRecibo(idVenta);
    }
    
    @PostMapping("/obtenerComprobanteDeposito")
    public ResponseEntity<Resource> obtenerComprobanteDeposito(@RequestBody DtoIdVenta idVenta){
        return ventaServices.obtenerComprobanteDeposito(idVenta);
    }
    
    @PostMapping("/listarHistorialVentas")
    public Page<DtoHistorialVentas> listarHistorial(@RequestBody DtoFiltroHistorialVentas filtro){
        Page<DtoHistorialVentas> historial = null;
        if(filtro.getContexto()==DtoFiltroHistorialVentas.ContextoOrigen.GENERAL){
           historial = ventaServices.listarHistorialGeneral(filtro);
        }else if(filtro.getContexto()==DtoFiltroHistorialVentas.ContextoOrigen.TURNO){
            historial = ventaServices.listarHistorialTurno(filtro);
        }else{
            historial = ventaServices.listarHistorialCliente(filtro);
            System.out.println("Se esta llamando al historial desde el cliente"+historial+" pagina "+filtro.getPage());
        }
        return historial;
    }
    
    
}
