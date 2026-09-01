package com.example.services;

import com.example.DTO.DtoDevolverStock;
import com.example.DTO.DtoEfectivoCaja;
import com.example.DTO.DtoId;
import com.example.DTO.DtoRegistrarDevolucion;
import com.example.DTO.DtoResponse;
import com.example.DTO.DtoResponseDevolucion;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.DtoResumenDevolucion;
import com.example.DTO.Dto_infoDevoluciones;
import com.example.DTO.ItemProductosDevoluciones;
import com.example.Repository.CreditoRepository;
import com.example.Repository.DetallesDevolucionRepository;
import com.example.Repository.DetallesDevolucion_lotesRepository;
import com.example.Repository.DetallesVentaRepository;
import com.example.Repository.DetallesVenta_lotesRepository;
import com.example.Repository.DevolucionRepository;
import com.example.Repository.VentaRepository;
import com.example.domain.Credito;
import com.example.domain.DetallesDevolucion;
import com.example.domain.DetallesVenta;
import com.example.domain.Detallesdevolucion_lotes;
import com.example.domain.Detallesventa_lotes;
import com.example.domain.Devolucion;
import com.example.domain.Venta;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DevolucionServices {

    @Autowired
    @Lazy
    private DevolucionServices self;

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

    @Autowired
    private CreditoServices creditoService;

    @Autowired
    private LoteStockServices loteServices;

    @Autowired
    private DetallesVenta_lotesRepository dv_lotesRepository;

    @Autowired
    private DetallesDevolucion_lotesRepository dd_lotesRepository;

    @Autowired
    private TurnoServices turnoServices;

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
                        + " <br> En efectivo - Q "
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

    @Transactional(rollbackFor = Exception.class)
    public DtoResponseDevolucion registrarDevolucion(DtoRegistrarDevolucion dto) {
        try {
            return procesarDevolucion(dto);
        } catch (Exception e) {
            return new DtoResponseDevolucion(false, e.getMessage(), false);
        }
    }

    private DtoResponseDevolucion procesarDevolucion(DtoRegistrarDevolucion devolucion) {
        Venta venta = validarVenta(devolucion.getIdVenta());

        DtoResponseOb<Dto_infoDevoluciones> info = infoDevolucion(new DtoId(devolucion.getIdVenta()));
        if (!info.isSuccess()) {
            throw new RuntimeException("Error al obtener información de devolución");
        }

        List<ItemProductosDevoluciones> listaBD = prepararListaDevoluciones(info, devolucion);
        String estadoRecibo = determinarEstadoRecibo(listaBD);

        BigDecimal totalADevolver = calcularTotal(listaBD);

        String metodoDevolucion = procesarMetodoDevolucion(venta, devolucion, totalADevolver, estadoRecibo);

        procesarStock(venta, listaBD);

        registrarDevolucionYDetalles(devolucion, listaBD, estadoRecibo);

        actualizarEstadoVenta(venta, estadoRecibo);

        boolean devolucionTotal = estadoRecibo.equals("DEVUELTA");
        return new DtoResponseDevolucion(true, "Devolución exitosa. \nMétodo de devolución: " +
                metodoDevolucion, devolucionTotal);
    }

    private Venta validarVenta(Long idVenta) {
        return ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Recibo no encontrado"));
    }

    private List<ItemProductosDevoluciones> prepararListaDevoluciones(
            DtoResponseOb<Dto_infoDevoluciones> info, DtoRegistrarDevolucion devolucion) {

        List<ItemProductosDevoluciones> listaBD = info.getData().getListaProductos();
        Map<Long, ItemProductosDevoluciones> mapaCliente = devolucion.getProductosDevueltos()
                .stream().collect(Collectors.toMap(ItemProductosDevoluciones::getIdProducto, Function.identity()));

        listaBD.forEach(itemBD -> {
            ItemProductosDevoluciones itemCliente = mapaCliente.get(itemBD.getIdProducto());
            if (itemCliente != null) {
                itemBD.setCantidadadevolver(itemCliente.getCantidadadevolver());
            }
        });
        return listaBD;
    }

    private String determinarEstadoRecibo(List<ItemProductosDevoluciones> listaBD) {
        String estado = "DEVUELTA";
        for (ItemProductosDevoluciones item : listaBD) {
            if (item.getCantidadadevolver().compareTo(item.getCantidad()) < 0) {
                estado = "PARCIALMENTE_DEVUELTA";
            }
            if (item.getCantidad().compareTo(item.getCantidadadevolver()) < 0) {
                throw new RuntimeException("Error: devolución mayor a cantidad vendida");
            }
        }
        return estado;
    }

    private BigDecimal calcularTotal(List<ItemProductosDevoluciones> listaBD) {
        return listaBD.stream()
                .map(item -> item.getCantidadadevolver().multiply(item.getPrecio()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String procesarMetodoDevolucion(Venta venta, DtoRegistrarDevolucion devolucion,
            BigDecimal totalADevolver, String estadoRecibo) {

        String metodoDevolucion;

        if (venta.getMetodoPago().equalsIgnoreCase("Credito")) {
            Credito credito = creditoRepository.findByIdVenta(devolucion.getIdVenta())
                    .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));

            DtoResponseOb<DtoId> abonoResponse = creditoService.registrarAbonoPorDevolucion(
                    credito.getId(),
                    totalADevolver,
                    devolucion.getIdTurno(),
                    devolucion.getIdVendedor()
            );

            if (!abonoResponse.isSuccess()) {
                throw new RuntimeException(abonoResponse.getMessage());
            }

            if (totalADevolver.compareTo(credito.getSaldoPendiente()) <= 0) {
                metodoDevolucion = "Liquidación de saldo pendiente - Q "
                        + totalADevolver.setScale(2, RoundingMode.HALF_UP).toPlainString();
            } else {
                BigDecimal excedente = totalADevolver.subtract(credito.getSaldoPendiente());
                metodoDevolucion = "Liquidación de saldo pendiente - Q "
                        + credito.getSaldoPendiente().setScale(2, RoundingMode.HALF_UP).toPlainString()
                        + " | En efectivo - Q "
                        + excedente.setScale(2, RoundingMode.HALF_UP).toPlainString();
            }

        } else {
            DtoResponse turnoResponse = turnoServices.ingresoRetiroEfectivo(new DtoEfectivoCaja(
                    devolucion.getIdTurno(),
                    "Reembolso",
                    totalADevolver,
                    "Reembolso por devolución " + (estadoRecibo.equals("DEVUELTA") ? "total" : "parcial")
                    + " del Recibo No." + devolucion.getIdVenta()
            ));

            if (!turnoResponse.isSuccess()) {
                throw new RuntimeException(turnoResponse.getMessage());
            }

            metodoDevolucion = "En efectivo - Q " + totalADevolver.setScale(2, RoundingMode.HALF_UP).toPlainString();
        }

        return metodoDevolucion;
    }

    private void procesarStock(Venta venta, List<ItemProductosDevoluciones> listaBD) {
        for (ItemProductosDevoluciones item : listaBD) {
            DtoResponse resp = devolucionStock(venta.getId(), item.getIdProducto(), item.getCantidadadevolver());
            if (!resp.isSuccess()) {
                throw new RuntimeException(resp.getMessage());
            }
        }
    }

    private DtoResponse devolucionStock(Long idVenta, Long idProducto, BigDecimal cantidad) {
        List<Detallesventa_lotes> lotesVenta = dv_lotesRepository.findByIdVentaAndIdProducto(idVenta, idProducto);
        BigDecimal cantidadPendiente = cantidad;

        for (Detallesventa_lotes dv_lote : lotesVenta) {
            if (cantidadPendiente.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            // Buscar si ya existe devolución previa para este lote
            Detallesdevolucion_lotes dd_lote = dd_lotesRepository.findByIdDetallesVentaLote(dv_lote.getId());

            if (dd_lote == null) {
                // Caso: primera devolución sobre este lote
                BigDecimal devolver = cantidadPendiente.min(dv_lote.getCantidad());

                DtoResponse response = loteServices.devolverStock(
                        new DtoDevolverStock(dv_lote.getIdLote(), devolver, "Devolución de producto")
                );
                if (!response.isSuccess()) {
                    return response;
                }

                cantidadPendiente = cantidadPendiente.subtract(devolver);

                DtoResponse response2 = guardarLoteDevolucion(null, dv_lote.getId(), dv_lote.getIdLote(), idProducto, devolver);
                if (!response2.isSuccess()) {
                    return response2;
                }

            } else {
                // Caso: ya hubo devoluciones previas en este lote
                BigDecimal disponible = dv_lote.getCantidad().subtract(dd_lote.getCantidad());

                if (disponible.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal devolver = cantidadPendiente.min(disponible);

                    DtoResponse response = loteServices.devolverStock(
                            new DtoDevolverStock(dd_lote.getIdLote(), devolver, "Devolución de producto")
                    );
                    if (!response.isSuccess()) {
                        return response;
                    }

                    cantidadPendiente = cantidadPendiente.subtract(devolver);

                    DtoResponse response2 = guardarLoteDevolucion(dd_lote.getId(), dv_lote.getId(), dv_lote.getIdLote(), idProducto, devolver);
                    if (!response2.isSuccess()) {
                        return response2;
                    }
                }
            }
        }

        return new DtoResponse(true, "ok");
    }

    private DtoResponse guardarLoteDevolucion(Long idDetalleDevolucionLote,
            Long idDetalleVentaLote,
            Long idLote,
            Long idProducto,
            BigDecimal cantidad) {
        // Validaciones iniciales
        if (idDetalleVentaLote == null || !dv_lotesRepository.existsById(idDetalleVentaLote)) {
            return new DtoResponse(false, "El detalle de venta lote no existe: " + idDetalleVentaLote);
        }
        if (idLote == null) {
            return new DtoResponse(false, "El idLote no puede ser nulo");
        }
        if (idProducto == null) {
            return new DtoResponse(false, "El idProducto no puede ser nulo");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return new DtoResponse(false, "La cantidad a devolver debe ser mayor a 0");
        }

        // Caso 1: primera devolución sobre este detalle de venta
        if (idDetalleDevolucionLote == null) {
            Detallesdevolucion_lotes dd_lotes = new Detallesdevolucion_lotes();
            dd_lotes.setIdDetallesVentaLote(idDetalleVentaLote);
            dd_lotes.setIdLote(idLote);
            dd_lotes.setIdProducto(idProducto);
            dd_lotes.setCantidad(cantidad);
            dd_lotesRepository.save(dd_lotes);

            return new DtoResponse(true, "Devolución lote registrada correctamente");
        }

        // Caso 2: ya existe devolución previa, se acumula
        Detallesdevolucion_lotes dd_lotes = dd_lotesRepository.findById(idDetalleDevolucionLote).orElse(null);
        if (dd_lotes == null) {
            return new DtoResponse(false, "El detalle de devolución lote no existe: " + idDetalleDevolucionLote);
        }

        dd_lotes.setCantidad(dd_lotes.getCantidad().add(cantidad));
        dd_lotesRepository.save(dd_lotes);

        return new DtoResponse(true, "Devolución lote actualizada correctamente");
    }

    private void registrarDevolucionYDetalles(DtoRegistrarDevolucion devolucion,
            List<ItemProductosDevoluciones> listaBD, String estadoRecibo) {

        Devolucion dev = new Devolucion();
        dev.setIdVenta(devolucion.getIdVenta());
        dev.setIdVendedor(devolucion.getIdVendedor());
        dev.setFecha(LocalDateTime.now());
        dev.setMotivo(devolucion.getMotivo());
        dev.setTipo(estadoRecibo.toUpperCase());
        dev = devolucionRepository.save(dev);

        for (ItemProductosDevoluciones itemBD : listaBD) {
            DetallesDevolucion detallesDevolucion = new DetallesDevolucion();
            detallesDevolucion.setIdDevolucion(dev.getId());
            detallesDevolucion.setIdProducto(itemBD.getIdProducto());
            detallesDevolucion.setCodigo(itemBD.getDetallesVenta().getCodigo());
            detallesDevolucion.setDescripcion(itemBD.getDetallesVenta().getDescripcion());
            detallesDevolucion.setDescuento(itemBD.getDetallesVenta().getDescuentos());
            detallesDevolucion.setCantidadDevuelta(itemBD.getCantidadadevolver());
            detallesDevolucion.setPrecio(itemBD.getDetallesVenta().getPrecio());
            detallesDevolucion.setPrecioFinal(itemBD.getDetallesVenta().getPrecioFinal());
            detallesDevolucion.setSubtotalDevuelto(
                    detallesDevolucion.getCantidadDevuelta().multiply(detallesDevolucion.getPrecioFinal())
            );
            detallesdevolucionRepository.save(detallesDevolucion);
        }
    }

    private void actualizarEstadoVenta(Venta venta, String estadoRecibo) {
        venta.setEstado(estadoRecibo);
        ventaRepository.save(venta);
    }

    private boolean dentroDePlazo(LocalDate fecha, int limiteDias) {
        return ChronoUnit.DAYS.between(fecha, LocalDate.now()) <= limiteDias;
    }
}
