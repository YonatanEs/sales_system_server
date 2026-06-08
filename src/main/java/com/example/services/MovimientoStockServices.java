package com.example.services;

import com.example.Repository.MovimientosStockRepository;
import com.example.domain.MovimientoStock;
import org.springframework.stereotype.Service;

@Service
public class MovimientoStockServices {
    
    private MovimientosStockRepository movimientoRepository;
    
    public MovimientoStockServices(MovimientosStockRepository movimientoRepository){
        this.movimientoRepository = movimientoRepository;
    }
    
    public void RegistrarMovimientoStock(MovimientoStock movimientoStock){
        movimientoRepository.save(movimientoStock);
    }
    
}
