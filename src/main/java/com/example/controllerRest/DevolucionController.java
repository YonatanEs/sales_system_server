package com.example.controllerRest;

import com.example.DTO.DtoId;
import com.example.DTO.DtoRegistrarDevolucion;
import com.example.DTO.DtoResponse;
import com.example.DTO.DtoResponseDevolucion;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.DtoResumenDevolucion;
import com.example.DTO.Dto_infoDevoluciones;
import com.example.services.DevolucionServices;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<DtoResponseOb<Dto_infoDevoluciones>> infoDevolucion(@RequestBody DtoId dto){
        DtoResponseOb<Dto_infoDevoluciones> response = devolucionService.infoDevolucion(dto);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/resumenDevolucion")
    public DtoResumenDevolucion resumenDevolucion(@RequestBody Dto_infoDevoluciones dto){
        return devolucionService.resumenDevolucion(dto);
    }
    
    @PostMapping("/registrarDevolucion")
    public ResponseEntity<DtoResponseDevolucion> registrarDevolucion(@RequestBody DtoRegistrarDevolucion dto){

        DtoResponseDevolucion response = devolucionService.registrarDevolucion(dto);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
}
