package com.example.services;

import com.example.DTO.DtoAddStock;
import com.example.DTO.DtoRemoveStock;
import com.example.DTO.DtoResponse;
import com.example.Repository.LoteStockRepository;
import com.example.domain.LoteStock;
import com.example.domain.MovimientoStock;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class LoteStockServices {

    private LoteStockRepository loteStockRepository;
    private MovimientoStockServices movimientoServices;

    public LoteStockServices(LoteStockRepository loteStockRepository, MovimientoStockServices movimientoStockServices) {
        this.loteStockRepository = loteStockRepository;
        this.movimientoServices = movimientoStockServices;
    }

    public DtoResponse añadirStock(DtoAddStock addStock) {
        LoteStock loteStock = new LoteStock();
        loteStock.setIdProducto(addStock.getId());
        loteStock.setFechaEntrada(addStock.getFechaEntrada());
        loteStock.setStockInicial(addStock.getStock());
        loteStock.setStockActual(addStock.getStock());
        loteStock.setPrecioCompra(addStock.getPrecioCompra());
        loteStock.setEstado("Activo");

        LoteStock L = loteStockRepository.save(loteStock);

        MovimientoStock m = new MovimientoStock();
        m.setIdLote(L.getId());
        m.setIdProducto(addStock.getId());
        m.setTipoMovimiento("INGRESO");
        m.setCantidad(addStock.getStock());
        m.setCosto(addStock.getPrecioCompra());
        m.setConcepto(addStock.getConcepto());
        m.setFecha(addStock.getFechaEntrada());

        movimientoServices.RegistrarMovimientoStock(m);

        return new DtoResponse(true, "¡Stock registrado correctamente!");
    }

    @Transactional
    public DtoResponse retirarStockMultilote(DtoRemoveStock removeStock) {

        List<LoteStock> lotesDisponibles = loteStockRepository
                .findByIdProductoAndEstadoAndStockActualGreaterThanOrderByFechaEntradaAsc(
                        removeStock.getIdProducto(), "Activo", BigDecimal.ZERO
                );

        // 2. Calcular el stock total sumando lo que hay en todos los lotes
        double stockTotalDisponible = lotesDisponibles.stream()
                .mapToDouble(lote -> lote.getStockActual() != null ? lote.getStockActual().doubleValue() : 0.0)
                .sum();

        // 3. VALIDACIÓN DE ORO: Si ni sumando todos los lotes nos alcanza, rebotamos la operación
        if (removeStock.getStock().compareTo(BigDecimal.valueOf(stockTotalDisponible))>0) {
            return new DtoResponse(false, "Error: Stock insuficiente en todo el sistema. Total disponible: " + stockTotalDisponible);
        }
 
        double cantidadPendientePorRetirar = removeStock.getStock().doubleValue();

        // 4. Recorrer los lotes y empezar a descontar en cadena
        for (LoteStock lote : lotesDisponibles) {
            if (cantidadPendientePorRetirar <= 0) {
                break; // Ya descontamos todo lo solicitado, salimos del ciclo
            }
            double stockActualLote = lote.getStockActual().doubleValue();
            double cantidadADescontarDeEsteLote;

            if (stockActualLote <= cantidadPendientePorRetirar) {
                // Caso A: El lote tiene menos o lo mismo de lo que me falta. Lo vaciamos por completo.
                cantidadADescontarDeEsteLote = stockActualLote;
                lote.setStockActual(BigDecimal.ZERO);
                lote.setEstado("Agotado");
            } else {
                // Caso B: Este lote tiene suficiente para cubrir todo lo que me falta.
                cantidadADescontarDeEsteLote = cantidadPendientePorRetirar;
                double cantidadPendiente = stockActualLote - cantidadPendientePorRetirar;
                lote.setStockActual(BigDecimal.valueOf(cantidadPendiente));
            }

            // Restamos lo que ya tomamos de este lote a nuestra deuda total
            cantidadPendientePorRetirar -= cantidadADescontarDeEsteLote;

            // Guardamos el lote con su nuevo stock
            loteStockRepository.save(lote);

            // 5. Registrar un historial de movimiento por CADA lote afectado
            MovimientoStock m = new MovimientoStock();
            m.setIdLote(lote.getId());
            m.setIdProducto(lote.getIdProducto());
            m.setTipoMovimiento("EGRESO");
            m.setCantidad(BigDecimal.valueOf(cantidadADescontarDeEsteLote)); // La porción que le quitamos a este lote
            m.setCosto(lote.getPrecioCompra());
            m.setConcepto(removeStock.getConcepto());
            m.setFecha(removeStock.getFechaSalida());

            movimientoServices.RegistrarMovimientoStock(m);
        }

        return new DtoResponse(true, "¡Salida de inventario registrada correctamente!");
    }

}
