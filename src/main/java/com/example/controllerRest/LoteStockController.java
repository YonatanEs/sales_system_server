package com.example.controllerRest;

import com.example.DTO.DtoAddStock;
import com.example.DTO.DtoRemoveStock;
import com.example.DTO.DtoResponse;
import com.example.services.LoteStockServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loteStock")
public class LoteStockController {
 
    private LoteStockServices loteStockServices;
    
    public LoteStockController(LoteStockServices loteStockServices){
        this.loteStockServices = loteStockServices;
    }
    
    @PostMapping("/añadirstock")
    public ResponseEntity<DtoResponse> addStock(@RequestBody DtoAddStock addStock){
        DtoResponse response = loteStockServices.añadirStock(addStock);
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/removerstock")
    public ResponseEntity<DtoResponse> removeStock(@RequestBody DtoRemoveStock removeStock){
        DtoResponse response = loteStockServices.retirarStockMultilote(removeStock);
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
