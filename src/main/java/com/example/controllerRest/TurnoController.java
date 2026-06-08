package com.example.controllerRest;

import com.example.DTO.DtoCerrarTurno;
import com.example.DTO.DtoEfectivoCaja;
import com.example.DTO.DtoResponse;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.DtoUnirATurno;
import com.example.DTO.Dto_cajaSelected_abierta;
import com.example.DTO.Dto_turnoUserAuth;
import com.example.DTO.Producto_tab;
import com.example.DTO.RegistrarTurno;
import com.example.DTO.ValorRequestAndId;
import com.example.DTO.ValorRequestPag;
import com.example.domain.Caja;
import com.example.domain.MovimientosTurno;
import com.example.domain.Turno;
import com.example.services.TurnoServices;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/turnos")
public class TurnoController {
    
    private TurnoServices turnoServices;
    
    public TurnoController(TurnoServices turnoServices){
        this.turnoServices=turnoServices;
    }
    
    @PostMapping("/validarTurno")
    public ResponseEntity<Dto_turnoUserAuth> validarturno(@RequestBody Long id){
        Dto_turnoUserAuth turnoUserAuth = turnoServices.isTurnoLogged(id);
        
        return ResponseEntity.ok(turnoUserAuth); 
    }
    
    @PostMapping("/turnoSelected")
   public Turno turnoSeleccionado(@RequestBody Long id){
       return turnoServices.turnoSelected(id);
   }
    
   @PostMapping("/listarTurnos")
    public Page<Turno> listarTurnos(@RequestBody ValorRequestPag val){
        return turnoServices.listarTurnos(val);
    }
    
    @PostMapping("/listarMovimientosTurnos")
    public List<MovimientosTurno> listarMovimientosTurno(@RequestBody ValorRequestAndId val){ 
       return turnoServices.listarMovimientosTurnos(val);
    }
    
    @PostMapping("/validarCajaSeleccionada")
    public ResponseEntity<Dto_cajaSelected_abierta> validarCajaSeleccionada(@RequestBody Caja caja){
        Dto_cajaSelected_abierta dto = turnoServices.validarCajaSeleccionada(caja);
        
        return ResponseEntity.ok(dto); 
    }
    
    @PostMapping("/abrirTurno")
   public ResponseEntity<DtoResponse> abrirTurno(@RequestBody RegistrarTurno request){
       
        DtoResponse response = turnoServices.abrirTurno(request);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response); 
   }
   
   @PostMapping("/unirATurno")
   public ResponseEntity<DtoResponse> unirATurno(@RequestBody DtoUnirATurno request){
       
        DtoResponse response = turnoServices.unirATurno(request);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response); 
   }
   
   @PostMapping("/cerrarTurno")
   public ResponseEntity<DtoResponseOb<Turno>> cerrarTurno(@RequestBody DtoCerrarTurno request){
       
        DtoResponseOb<Turno> response = turnoServices.cerrarTurno(request, false);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response); 
   }
   
   @PostMapping("/cerrarTurnoForzado")
   public ResponseEntity<DtoResponseOb<Turno>> cerrarTurnoForzado(@RequestBody DtoCerrarTurno request){
       
        DtoResponseOb<Turno> response = turnoServices.cerrarTurno(request, true);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response); 
   }
   
   @PostMapping("/ingresoRetiroEfectivo")
   public ResponseEntity<DtoResponse> ingresoRetiroEfectivo(@RequestBody DtoEfectivoCaja request){
       
        DtoResponse response = turnoServices.ingresoRetiroEfectivo(request);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response); 
   }
   
}
