package com.example.services;

import com.example.DTO.DtoId;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.DtoResumenDevolucion;
import com.example.DTO.Dto_infoDevoluciones;
import com.example.DTO.ItemProductosDevoluciones;
import com.example.Repository.CreditoRepository;
import com.example.Repository.DetallesDevolucionRepository;
import com.example.Repository.DetallesVentaRepository;
import com.example.Repository.DevolucionRepository;
import com.example.Repository.VentaRepository;
import com.example.domain.Credito;
import com.example.domain.DetallesDevolucion;
import com.example.domain.DetallesVenta;
import com.example.domain.Devolucion;
import com.example.domain.Venta;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DevolucionServices {

    @Autowired
    private DevolucionRepository devolucionRepository;

    @Autowired
    private DetallesDevolucionRepository detallesdevolucionRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetallesVentaRepository detallesventaRepository;

    @Autowired
    private CreditoRepository creditoRepository;

    public DtoResponseOb<Dto_infoDevoluciones> infoDevolucion(DtoId dtoId) {
        Venta venta = ventaRepository.findById(dtoId.getId()).orElse(null);

        if (venta == null) {
            return new DtoResponseOb<>(false, "El Recibo seleccionado no existe", null);
        }
        if (venta.getEstado().equalsIgnoreCase("DEVUELTA")) {
            return new DtoResponseOb<>(false, "Los productos del recibo ya han sido devueltos",
                    null);
        }
        if (venta.getEstado().equalsIgnoreCase("ANULADA")) {
            return new DtoResponseOb<>(false, "El recibo ya a sido anulado",
                    null);
        }

        LocalDate fecha = venta.getFecha().toLocalDate();
        if (!dentroDePlazo(fecha, 5)) {
            return new DtoResponseOb<>(false, "El plazo de devolución ha vencido",
                    null);
        }
        List<Devolucion> devoluciones = devolucionRepository.findByIdVenta(dtoId.getId());

        List<DetallesVenta> productosVenta = detallesventaRepository.findByIdVenta(dtoId.getId());

        if (productosVenta == null || productosVenta.isEmpty()) {
            return new DtoResponseOb<>(false, "No hay ningun producto registrado en esta venta", null);
        }

        if (devoluciones != null && !devoluciones.isEmpty()) {
            Map<Long, BigDecimal> devolucionesMap = new HashMap<>();

            // Recorremos todas las devoluciones
            for (Devolucion devolucion : devoluciones) {
                List<DetallesDevolucion> productosDevueltos
                        = detallesdevolucionRepository.findByIdDevolucion(devolucion.getId());

                if (productosDevueltos != null) {
                    for (DetallesDevolucion detallesd : productosDevueltos) {
                        devolucionesMap.merge(
                                detallesd.getIdProducto(),
                                detallesd.getCantidadDevuelta(),
                                BigDecimal::add // suma si ya existe
                        );
                    }
                }
            }

            // Ajustamos cantidades en productosVenta
            for (DetallesVenta detallesv : productosVenta) {
                BigDecimal cantidadDevuelta = devolucionesMap.get(detallesv.getIdProducto());
                if (cantidadDevuelta != null) {
                    detallesv.setCantidad(detallesv.getCantidad().subtract(cantidadDevuelta));
                }
            }
        }

        Dto_infoDevoluciones infoDevoluciones = new Dto_infoDevoluciones();
        infoDevoluciones.setIdVenta(venta.getId());
        infoDevoluciones.setDetalleRecibo1("Recibo No." + venta.getId() + " - " + venta.getNombreCliente()
                + " - Q " + venta.getTotal().setScale(2, RoundingMode.HALF_UP).toPlainString());
        infoDevoluciones.setDetalleRecibo2("Fecha: " + venta.getFecha()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm a")) + " - Método: "
                + venta.getMetodoPago() + " - " + venta.getEstado());

        infoDevoluciones.setEstado(venta.getEstado());
        infoDevoluciones.setListaProductos(toDtoItemProducto(productosVenta));

        return new DtoResponseOb<>(true, "Ok", infoDevoluciones);
    }

    private List<ItemProductosDevoluciones> toDtoItemProducto(List<DetallesVenta> productovendidos) {
        return productovendidos
                .stream()
                .map(ItemProductosDevoluciones::new)
                .toList();
    }

    public DtoResumenDevolucion resumenDevolucion(Dto_infoDevoluciones info) {
        Venta venta = ventaRepository.findById(info.getIdVenta()).orElse(null);

        if (venta == null) {
            return null;
        }

        BigDecimal TotalActualRecibo = BigDecimal.ZERO;
        BigDecimal TotalADevolver = BigDecimal.ZERO;
        String metodoPago = venta.getMetodoPago();
        String metodoDevolucion = "";
        List<ItemProductosDevoluciones> productos = info.getListaProductos();

        for (ItemProductosDevoluciones item : productos) {
            TotalActualRecibo = TotalActualRecibo.add(
                    item.getCantidad().multiply(item.getPrecio()));

            TotalADevolver = TotalADevolver.add(
                    item.getCantidadadevolver().multiply(item.getPrecio()));

        }

        if (metodoPago.equalsIgnoreCase("Credito")) {
            Credito credito = creditoRepository.findByIdVenta(info.getIdVenta()).orElse(null);

            BigDecimal totalRecibo = credito.getMontoTotal();
            BigDecimal saldoPendiente = credito.getSaldoPendiente();

            if (TotalADevolver.compareTo(saldoPendiente) <= 0) {
                // Caso: la devolución cubre parte o todo el saldo pendiente
                metodoDevolucion = "Liquidación de saldo pendiente - Q "
                        + TotalADevolver.setScale(2, RoundingMode.HALF_UP).toPlainString();
            } else {
                // Caso: la devolución excede el saldo pendiente
                BigDecimal excedente = TotalADevolver.subtract(saldoPendiente);
                metodoDevolucion = "Liquidación de saldo pendiente - Q "
                        + saldoPendiente.setScale(2, RoundingMode.HALF_UP).toPlainString()
                        + " | En efectivo - Q "
                        + excedente.setScale(2, RoundingMode.HALF_UP).toPlainString();
            }
        } else {
            metodoDevolucion = "En efectivo - Q " + TotalADevolver
                    .setScale(2, RoundingMode.HALF_UP).toPlainString();
        }

        return new DtoResumenDevolucion(
                "Q " + TotalActualRecibo.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                metodoPago,
                "Q " + TotalADevolver.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                metodoDevolucion);
    }

    private void RegistrarDevolucion() {
        /*List<DetallesVenta> listaDevueltos = detallesventaRepository
                .findByIdVentaAndIdProductoIn(dtoId, idsProductos)
         */
    }

    private boolean dentroDePlazo(LocalDate fecha, int limiteDias) {
        return ChronoUnit.DAYS.between(fecha, LocalDate.now()) <= limiteDias;
    }
}
