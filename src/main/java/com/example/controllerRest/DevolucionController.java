package com.example.controllerRest;

import com.example.DTO.DtoAnulacionRecibo;
import com.example.DTO.DtoDevolucion;
import com.example.DTO.DtoFiltroHistorialDevolucion;
import com.example.DTO.DtoHistorialDevoluciones;
import com.example.DTO.DtoId;
import com.example.DTO.DtoRegistrarDevolucion;
import com.example.DTO.DtoResponse;
import com.example.DTO.DtoResponseDevolucion;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.DtoResumenDevolucion;
import com.example.DTO.Dto_infoDevoluciones;
import com.example.services.DevolucionServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/devoluciones")
public class DevolucionController {

    @Autowired
    private DevolucionServices devolucionService;

    @PostMapping("/infoDevolucion")
    public ResponseEntity<DtoResponseOb<Dto_infoDevoluciones>> infoDevolucion(@RequestBody DtoId dto) {
        DtoResponseOb<Dto_infoDevoluciones> response = devolucionService.infoDevolucion(dto);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/anularRecibo")
    public DtoResponse anularRecibo(@RequestBody DtoAnulacionRecibo dto) {
        return devolucionService.anularRecibo(dto);
    }
    
    @PostMapping("/resumenDevolucion")
    public DtoResumenDevolucion resumenDevolucion(@RequestBody Dto_infoDevoluciones dto) {
        return devolucionService.resumenDevolucion(dto);
    }

    @PostMapping("/devolucionDetalles")
    public DtoDevolucion devolucionDetalles(@RequestBody DtoId dto) {
        return devolucionService.devolucionDetalles(dto);
    }
    
    @PostMapping("/registrarDevolucion") 
    public ResponseEntity<DtoResponseDevolucion> registrarDevolucion(@RequestBody DtoRegistrarDevolucion dto) {

        DtoResponseDevolucion response = devolucionService.registrarDevolucion(dto);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/listarHistorialDevoluciones")
    public Page<DtoHistorialDevoluciones> listarHistorial(@RequestBody DtoFiltroHistorialDevolucion filtro) {
        System.out.println("Se esta ejecutando el controller de devoluciones, window: "+filtro.getWindow());
        switch (filtro.getWindow().toUpperCase()) {
            case "GENERAL":
                System.out.println("Se esta ejecutando en General de devoluciones");
                return devolucionService.listarHistorialGeneralDevoluciones(filtro);
            case "TURNO":
                System.out.println("Se esta ejecutando en TURNO de devoluciones");
                return devolucionService.listarHistorialTurnoDevoluciones(filtro);
            case "CLIENTE":
                System.out.println("Se esta ejecutando en CLIENTE 1 de devoluciones");
            default:
                System.out.println("Se esta ejecutando en CLIENTE de devoluciones");
                return devolucionService.listarHistorialClienteDevoluciones(filtro);
        }
    }

}
