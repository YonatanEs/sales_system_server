package com.example.services;

import com.example.DTO.DtoAnulacionRecibo;
import com.example.DTO.DtoDetallesDevolucion;
import com.example.DTO.DtoDevolucion;
import com.example.DTO.DtoDevolverStock;
import com.example.DTO.DtoEfectivoCaja;
import com.example.DTO.DtoFiltroHistorialDevolucion;
import com.example.DTO.DtoHistorialDevoluciones;
import com.example.DTO.DtoId;
import com.example.DTO.DtoRegistrarDevolucion;
import com.example.DTO.DtoResponse;
import com.example.DTO.DtoResponseDevolucion;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.DtoResumenDevolucion;
import com.example.DTO.Dto_infoDevoluciones;
import com.example.DTO.ItemProductosDevoluciones;
import com.example.Repository.AbonoRepository;
import com.example.Repository.CreditoRepository;
import com.example.Repository.DetallesDevolucionRepository;
import com.example.Repository.DetallesDevolucion_lotesRepository;
import com.example.Repository.DetallesVentaRepository;
import com.example.Repository.DetallesVenta_lotesRepository;
import com.example.Repository.DevolucionRepository;
import com.example.Repository.VentaRepository;
import com.example.domain.Abono;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    
    @Autowired
    private AbonoRepository abonoRepository;

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
    public DtoResponse anularRecibo(DtoAnulacionRecibo dto) {
        try {
            return procesarAnulacion(dto);
        } catch (Exception e) {
            return new DtoResponse(false, e.getMessage());
        }
    }

    private DtoResponse procesarAnulacion(DtoAnulacionRecibo dto) {
        Venta venta = validarVenta(dto.getIdVenta());

        //Validar la venta
        if (!venta.getEstado().equals("Activa")) {
            throw new RuntimeException("No se puede anular el Recibo");
        }

        //devolver el stock
        DtoResponse respStock = anularStock(venta.getId());
        if (!respStock.isSuccess()) {
            throw new RuntimeException(respStock.getMessage());
        }

        //anular el ingreso de efectivo dependiendo del método de pago
        DtoResponse respPago = anularMetodoPago(venta, dto.getIdTurno());
        if (!respPago.isSuccess()) {
            throw new RuntimeException(respPago.getMessage());
        }

        //cambiar el estado de las venta 
        venta.setEstado("ANULADA");
        ventaRepository.save(venta);
        return new DtoResponse(true, "¡Anulacion del Recibo No."+venta.getId()+" realizado exitosamente!");
    }

    private DtoResponse anularStock(Long idVenta) {
        List<Detallesventa_lotes> lotesVenta = dv_lotesRepository.findByIdVenta(idVenta);

        if (lotesVenta.isEmpty()) {
            return new DtoResponse(false, "No se encontraron lotes para la venta: " + idVenta);
        }

        for (Detallesventa_lotes dv_lote : lotesVenta) {
            BigDecimal cantidad = dv_lote.getCantidad();

            if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // nada que devolver
            }

            // Revertir stock al mismo lote de donde salió
            DtoResponse response = loteServices.devolverStock(
                    new DtoDevolverStock(dv_lote.getIdLote(), cantidad, "Anulación de factura")
            );

            if (!response.isSuccess()) {
                return response; // si falla, corto el proceso
            }
        }

        return new DtoResponse(true, "Stock revertido correctamente por anulación");
    }

    private DtoResponse anularMetodoPago(Venta venta, Long idTurno) {
        String metodo = venta.getMetodoPago();
        DtoResponse response;
        if (metodo.equals("Contado")) {
            response = turnoServices.ingresoRetiroEfectivo(
                    new DtoEfectivoCaja(idTurno, "Anulacion", venta.getTotal(),
                            "Reembolso de efectivo por anulación de Recibo No." + venta.getId()));
        } else if (metodo.equals("Credito")) {
            Credito credito = creditoRepository.findByIdVenta(venta.getId()).orElse(null);
            if(credito==null){
                return new DtoResponse(false, "Ha ocurrido un error con el crédito");
            }
            
            List<Abono> abonos = abonoRepository.findByIdCredito(credito.getId());
            if(!abonos.isEmpty()){
                return new DtoResponse(false, "La anulación no es posible: el recibo ya tiene abonos registrados."
                        + " Debe realizar una devolución total.");
            }
            
            credito.setSaldoPendiente(new BigDecimal("0.00"));
            credito.setEstado("ANULADO");
            creditoRepository.save(credito);
            turnoServices.MovientoAnulacionCredito(idTurno, venta.getId(), venta.getTotal());
            response = new DtoResponse(true, "Credito anulacion con exito");
        } else {
            response = turnoServices.AnularDepositoVenta(venta.getIdTurno(), venta.getTotal());
        }
        return response;
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

        registrarDevolucionYDetalles(totalADevolver, devolucion, listaBD, estadoRecibo, venta.getNombreCliente(), venta.getNitCliente());

        actualizarEstadoVenta(venta, estadoRecibo);

        boolean devolucionTotal = estadoRecibo.equals("DEVUELTA");
        return new DtoResponseDevolucion(true, "Devolución exitosa. \nMétodo de devolución: "
                + metodoDevolucion, devolucionTotal);
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

    private void registrarDevolucionYDetalles(BigDecimal totalADevolver, DtoRegistrarDevolucion devolucion,
            List<ItemProductosDevoluciones> listaBD, String estadoRecibo, String nombreCliente, String nitCliente) {

        Devolucion dev = new Devolucion();
        dev.setIdVenta(devolucion.getIdVenta());
        dev.setIdVendedor(devolucion.getIdVendedor());
        dev.setFecha(LocalDateTime.now());
        dev.setNombreCliente(nombreCliente);
        dev.setNitCliente(nitCliente);
        dev.setMotivo(devolucion.getMotivo());
        dev.setTipo(estadoRecibo.toUpperCase());
        dev.setTotalDevuelto(totalADevolver);
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

    public Page<DtoHistorialDevoluciones> listarHistorialGeneralDevoluciones(DtoFiltroHistorialDevolucion filtro) {
        Pageable pageable = buildPageable(filtro);

        switch (filtro.getBuscador().toUpperCase()) {
            case "NIT":
                if (!filtro.getBusqueda().isEmpty()) {
                    return toDtoHistorial(
                            devolucionRepository.findByNombreClienteContainingIgnoreCaseOrNitClienteContainingIgnoreCase(
                                    filtro.getBusqueda(), filtro.getBusqueda(), pageable));
                }
                break;
            case "FECHA":
                LocalDate fecha = LocalDate.parse(filtro.getBusqueda(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return toDtoHistorial(
                        devolucionRepository.findByFechaBetween(fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX), pageable));
            case "RECIBO":
                if (!filtro.getBusqueda().isEmpty()) {
                    return toDtoHistorial(
                            devolucionRepository.findByIdVenta(Long.valueOf(filtro.getBusqueda()), pageable));
                }
                break;
        }
        return toDtoHistorial(devolucionRepository.findAll(pageable));
    }

    public Page<DtoHistorialDevoluciones> listarHistorialTurnoDevoluciones(DtoFiltroHistorialDevolucion filtro) {
        Pageable pageable = buildPageable(filtro);

        switch (filtro.getBuscador().toUpperCase()) {
            case "NIT":
                if (!filtro.getBusqueda().isEmpty()) {
                    return toDtoHistorial(
                            devolucionRepository.findByIdTurnoAndNombreClienteContainingIgnoreCaseOrIdTurnoAndNitClienteContainingIgnoreCase(
                                    Long.valueOf(filtro.getVariableWindow()), filtro.getBusqueda(),
                                    Long.valueOf(filtro.getVariableWindow()), filtro.getBusqueda(), pageable));
                }
                break;
            case "FECHA":
                LocalDate fecha = LocalDate.parse(filtro.getBusqueda(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return toDtoHistorial(
                        devolucionRepository.findByIdTurnoAndFechaBetween(
                                Long.valueOf(filtro.getVariableWindow()), fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX), pageable));
            case "RECIBO":
                if (!filtro.getBusqueda().isEmpty()) {
                    return toDtoHistorial(
                            devolucionRepository.findByIdTurnoAndId(
                                    Long.valueOf(filtro.getVariableWindow()), Long.valueOf(filtro.getBusqueda()), pageable));
                }
                break;
        }

        return toDtoHistorial(
                devolucionRepository.findByIdTurno(Long.valueOf(filtro.getVariableWindow()), pageable));
    }

    public Page<DtoHistorialDevoluciones> listarHistorialClienteDevoluciones(DtoFiltroHistorialDevolucion filtro) {
        Pageable pageable = buildPageable(filtro);

        switch (filtro.getBuscador().toUpperCase()) {
            case "FECHA":
                LocalDate fecha = LocalDate.parse(filtro.getBusqueda(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return toDtoHistorial(
                        devolucionRepository.findByNitClienteAndFechaBetween(
                                filtro.getVariableWindow(), fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX), pageable));
            case "RECIBO":
                if (!filtro.getBusqueda().isEmpty()) {
                    return toDtoHistorial(
                            devolucionRepository.findByNitClienteAndId(
                                    filtro.getVariableWindow(), Long.valueOf(filtro.getBusqueda()), pageable));
                }
                break;
        }

        return toDtoHistorial(
                devolucionRepository.findByNitCliente(filtro.getVariableWindow(), pageable));
    }

    private Pageable buildPageable(DtoFiltroHistorialDevolucion filtro) {
        return PageRequest.of(
                filtro.getPage(),
                filtro.getSize(),
                Sort.by(Sort.Direction.DESC, "fecha")
        );
    }

    public Page<DtoHistorialDevoluciones> toDtoHistorial(Page<Devolucion> devoluciones) {
        return devoluciones.map(DtoHistorialDevoluciones::new);
    }

    private boolean dentroDePlazo(LocalDate fecha, int limiteDias) {
        return ChronoUnit.DAYS.between(fecha, LocalDate.now()) <= limiteDias;
    }

    public DtoDevolucion devolucionDetalles(DtoId dto) {
        Optional<Devolucion> OptDev = devolucionRepository.findById(dto.getId());
        if (OptDev.isEmpty()) {
            return null;
        }
        Devolucion devolucion = OptDev.get();

        DtoDevolucion dtoDevolucion = new DtoDevolucion();

        dtoDevolucion.setIdDevolucion(devolucion.getId());
        dtoDevolucion.setIdVenta(devolucion.getIdVenta());
        dtoDevolucion.setCliente(devolucion.getNombreCliente());
        dtoDevolucion.setNit(devolucion.getNitCliente());
        dtoDevolucion.setFecha(devolucion.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm")));
        dtoDevolucion.setTotalDevolver(devolucion.getTotalDevuelto());
        dtoDevolucion.setMotivo(devolucion.getMotivo());

        List<DetallesDevolucion> detallesDevolucion = detallesdevolucionRepository.findByIdDevolucion(dto.getId());
        dtoDevolucion.setDetallesDevolucion(toDtoDetallesDevolucion(detallesDevolucion));

        return dtoDevolucion;
    }

    private List<DtoDetallesDevolucion> toDtoDetallesDevolucion(List<DetallesDevolucion> detalles) {
        return detalles.stream()
                .map(DtoDetallesDevolucion::new) // usa el constructor directamente
                .toList();
    }
}
