package com.example.services;

import com.example.DTO.DtoEfectivoCaja;
import com.example.DTO.DtoFiltroHistorialVentas;
import com.example.DTO.DtoHistorialVentas;
import com.example.DTO.DtoIdVenta;
import com.example.DTO.DtoPedido;
import com.example.DTO.DtoRecibo;
import com.example.DTO.DtoVentaContado;
import com.example.DTO.DtoRemoveStock;
import com.example.DTO.DtoResponse;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.DtoVentaCredito;
import com.example.DTO.DtoVentaDeposito;
import com.example.DTO.DtoVentaStock;
import com.example.DTO.Producto_tab;
import com.example.DTO.Usuario_tab;
import com.example.Repository.ClienteRepository;
import com.example.Repository.DetallesVentaRepository;
import com.example.Repository.DetallesVenta_lotesRepository;
import com.example.Repository.UsuarioRepository;
import com.example.Repository.VentaRepository;
import com.example.domain.Cliente;
import com.example.domain.DetallesVenta;
import com.example.domain.Venta;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VentaServices {

    @Autowired
    @Lazy
    private VentaServices self;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TurnoServices turnoServices;

    @Autowired
    private LoteStockServices stockService;

    @Autowired
    private ProductoServices productoServices;

    @Autowired
    private DetallesVentaRepository detallesVentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CreditoServices CreditoServices;
    
    @Autowired
    private DetallesVenta_lotesRepository ddv_lotesRepository;

    @Value("${storage.location.comprobantes.ventas}") 
    private String storageLocation;

    ///Registros para ventas al contado
    public DtoResponseOb<DtoIdVenta> RegistrarVentaAlContado(DtoVentaContado dtoregistrar) {
        //verifica que la venta con nit cf no sea mayor a 2500
        if (dtoregistrar.getTotalApagar().compareTo(new BigDecimal("2500")) >= 0
                && (dtoregistrar.getNitCliente().equals("cf") || dtoregistrar.getNitCliente().equalsIgnoreCase("c/f"))) {
            return new DtoResponseOb(false, "Para realizar una venta mayor a Q 2,500 es necesario un NIT", null);
        }

        try {
            return self.RegistrarVentaContadoTransactional(dtoregistrar);
        } catch (Exception e) {
            return new DtoResponseOb(false, e.getMessage(), null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public DtoResponseOb<DtoIdVenta> RegistrarVentaContadoTransactional(DtoVentaContado dtoregistrar) {
        String metodopago = dtoregistrar.getMetodoPago();

        //regitrar la venta
        Venta venta = registarVenta(dtoregistrar.getIdTurno(), dtoregistrar.getTotalApagar(),
                dtoregistrar.getNitCliente(), dtoregistrar.getNombreCliente(),
                dtoregistrar.getIdVendedor(), metodopago);

        String concepto = "Venta al contado - Recibo No. " + venta.getId();

        //manejo de los detalles de la venta
        DtoResponse detallesResponse = registrarDetallesVenta(dtoregistrar.getListaPedidos(), venta.getId(), concepto);
        if (!detallesResponse.isSuccess()) {
            throw new RuntimeException(detallesResponse.getMessage());
        }

        //manejo de detalles del turno actual
        DtoResponse mturnoResponse = turnoServices.ingresoRetiroEfectivo(
                new DtoEfectivoCaja(dtoregistrar.getIdTurno(), "Venta al contado", dtoregistrar.getTotalApagar(),
                        concepto));
        if (!mturnoResponse.isSuccess()) {
            throw new RuntimeException(mturnoResponse.getMessage());
        }

        return new DtoResponseOb(true, "Venta realizada exitosamente ", new DtoIdVenta(venta.getId()));
    }

    private Venta registarVenta(Long idTurno, BigDecimal total, String nit, String nombre, Long idVendedor, String metodo) {
        Venta venta = new Venta();
        venta.setIdTurno(idTurno);
        venta.setFecha(LocalDateTime.now());
        venta.setTotal(total);
        venta.setNitCliente(nit);
        venta.setNombreCliente(nombre);
        venta.setVendedor(usuarioRepository.findById(idVendedor).orElse(null));
        venta.setMetodoPago(metodo);
        venta.setEstado("Activa");
        venta.setNoDeposito(null);
        venta.setUrlComprobante(null);
        return ventaRepository.save(venta);
    }

    private DtoResponse registrarDetallesVenta(List<DtoPedido> pedidos, Long idVenta, String concepto) {
        for (DtoPedido pedido : pedidos) {
            Producto_tab producto = productoServices.productoSelected(pedido.getId_producto());

            if (pedido.getPreciofinal().compareTo(producto.getPrecio_compra()) < 0) {
                String mensaje = String.format("La venta del producto '%s' no puede realizarse: el precio final (%.2f) es menor al costo de compra (%.2f).",
                        pedido.getDescripcion(),
                        pedido.getPreciofinal(),
                        producto.getPrecio_compra());
                return new DtoResponse(false, mensaje);
            }

            DetallesVenta detalles = new DetallesVenta();
            detalles.setIdVenta(idVenta);
            detalles.setIdProducto(pedido.getId_producto());
            detalles.setCodigo(pedido.getCodigo());
            detalles.setDescripcion(pedido.getDescripcion());
            detalles.setCantidad(pedido.getCantidad());
            detalles.setPrecio(pedido.getPrecio());
            detalles.setDescuentos(pedido.getDescuentos());
            detalles.setPrecioFinal(pedido.getPreciofinal());
            detalles.setSubtotal(pedido.getSubtotal());
            detallesVentaRepository.save(detalles);

            DtoResponse responseStock = stockService.ventaStockMultilote(
                    new DtoVentaStock( 
                            pedido.getId_producto(),
                            idVenta,
                            LocalDateTime.now(),
                            pedido.getCantidad(),
                            concepto));
            
            if (!responseStock.isSuccess()) {
                return responseStock;
            }

        }
        return new DtoResponse(true, "Registro de detalles exitoso");
    }

    ///Registro para ventas con deposito o transeferencia
    public DtoResponseOb<DtoIdVenta> RegistrarVentaConDeposito(DtoVentaDeposito ventaDeposito, MultipartFile comprobante) {

        try {
            return self.RegistrarVentaDepositoTransactional(ventaDeposito, comprobante);
        } catch (Exception e) {
            return new DtoResponseOb(false, e.getMessage(), null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public DtoResponseOb<DtoIdVenta> RegistrarVentaDepositoTransactional(DtoVentaDeposito ventaDeposito, MultipartFile comprobante) {
        String metodopago = ventaDeposito.getMetodoPago();

        //regitrar la venta
        Venta venta;
        try {
            venta = registarVentaDeposito(ventaDeposito.getIdTurno(), ventaDeposito.getTotalApagar(),
                    ventaDeposito.getNitCliente(), ventaDeposito.getNombreCliente(),
                    ventaDeposito.getIdVendedor(), metodopago, ventaDeposito.getNoDeposito(), comprobante);
        } catch (Exception e) {
            return new DtoResponseOb(false, e.getMessage(), null);
        }

        if (venta == null) {
            throw new RuntimeException("¡Ha ocurrido un error con el comprobante!");
        }

        String concepto = "Venta con Deposito - Recibo No. " + venta.getId();

        //manejo de los detalles de la venta
        DtoResponse detallesResponse = registrarDetallesVenta(ventaDeposito.getListaPedidos(), venta.getId(), concepto);
        if (!detallesResponse.isSuccess()) {
            throw new RuntimeException(detallesResponse.getMessage());
        }

        //manejo de detalles del turno actual
        DtoResponse mturnoResponse = turnoServices.ingresoRetiroEfectivo(
                new DtoEfectivoCaja(ventaDeposito.getIdTurno(), "Venta por deposito", ventaDeposito.getTotalApagar(),
                        concepto));
        if (!mturnoResponse.isSuccess()) {
            throw new RuntimeException(mturnoResponse.getMessage());
        }

        return new DtoResponseOb(true, "Venta realizada exitosamente ", new DtoIdVenta(venta.getId()));
    }

    private Venta registarVentaDeposito(Long idTurno, BigDecimal total, String nit,
            String nombre, Long idVendedor, String metodo, String NoDeposito, MultipartFile comprobante) throws IOException {
        
        Venta venta = new Venta();
        venta.setIdTurno(idTurno);
        venta.setFecha(LocalDateTime.now());
        venta.setTotal(total);
        venta.setNitCliente(nit);
        venta.setNombreCliente(nombre);
        venta.setVendedor(usuarioRepository.findById(idVendedor).orElse(null));
        venta.setMetodoPago(metodo);
        venta.setEstado("Activa");
        
        if (comprobante != null && !comprobante.isEmpty()) {
            Path rutaCarpeta = Paths.get(storageLocation).toAbsolutePath().normalize();
            Files.createDirectories(rutaCarpeta);
            
            // Generar nombre único para el comprobante
            String nombreOriginal = comprobante.getOriginalFilename();
            String nombreArchivoFinal = System.currentTimeMillis() + "_"
                    + (nombreOriginal != null ? nombreOriginal.replaceAll("\\s+", "_") : "comprobante.png");

            // Guardar físicamente el archivo
            Path rutaDestino = rutaCarpeta.resolve(nombreArchivoFinal);
            Files.copy(comprobante.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

            // Guardar la ruta/nombre en la BD
            venta.setUrlComprobante(nombreArchivoFinal);
            venta.setNoDeposito(NoDeposito); // este lo recibes del formulario

        } else {
            throw new RuntimeException("Debe adjuntar un comprobante para registrar la venta.");
        }

        return ventaRepository.save(venta);
    }

    ///registro para ventas con credito
    public DtoResponseOb<DtoIdVenta> RegistrarVentasCredito(DtoVentaCredito ventaCredito) {
        try {
            return self.RegistrarVentaCreditoTransactional(ventaCredito);
        } catch (Exception e) {
            return new DtoResponseOb(false, e.getMessage(), null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public DtoResponseOb<DtoIdVenta> RegistrarVentaCreditoTransactional(DtoVentaCredito ventacredito) {
        String metodopago = ventacredito.getMetodoPago();
        Cliente cliente = clienteRepository.findByNit(ventacredito.getNitCliente()).orElse(null);
        if (cliente == null) {
            throw new RuntimeException("El cliente no está registrado en el sistema");
        }

        //regitrar la venta
        Venta venta = registarVentaCredito(ventacredito.getIdTurno(), ventacredito.getTotalApagar(),
                ventacredito.getNitCliente(), ventacredito.getNombreCliente(),
                ventacredito.getIdVendedor(), metodopago);

        //registrar el credito
        DtoResponse credito = CreditoServices.RegistrarCredito(venta.getId(), cliente.getId(),
                ventacredito.getPlazoPago(), ventacredito.getTotalApagar());

        if (!credito.isSuccess()) {
            throw new RuntimeException(credito.getMessage());
        }

        String concepto = "Venta a credito - Recibo No. " + venta.getId();

        ///////#### nota###  debes registrar otro campo en turno, que son ventas a credito y reporte tambien
        //manejo de los detalles de la venta
        DtoResponse detallesResponse = registrarDetallesVenta(ventacredito.getListaPedidos(), venta.getId(), concepto);
        if (!detallesResponse.isSuccess()) {
            throw new RuntimeException(detallesResponse.getMessage());
        }

        //manejo de detalles del turno actual
        DtoResponse mturnoResponse = turnoServices.ingresoRetiroEfectivo(
                new DtoEfectivoCaja(ventacredito.getIdTurno(), "Venta a credito", ventacredito.getTotalApagar(),
                        concepto));
        if (!mturnoResponse.isSuccess()) {
            throw new RuntimeException(mturnoResponse.getMessage());
        }

        return new DtoResponseOb(true, "Venta realizada exitosamente ", new DtoIdVenta(venta.getId()));
    }

    private Venta registarVentaCredito(Long idTurno, BigDecimal total, String nit,
            String nombre, Long idVendedor, String metodo) {
        Venta venta = new Venta();
        venta.setIdTurno(idTurno);
        venta.setFecha(LocalDateTime.now());
        venta.setTotal(total);
        venta.setNitCliente(nit);
        venta.setNombreCliente(nombre);
        venta.setVendedor(usuarioRepository.findById(idVendedor).orElse(null));
        venta.setMetodoPago(metodo);
        venta.setEstado("Activa");

        return ventaRepository.save(venta);
    }

    ///opciones de visuzalizacion de recibo
    public DtoRecibo obtenerRecibo(DtoIdVenta idVenta) {
        Venta venta = ventaRepository.findById(idVenta.getId()).orElse(null);

        if (venta == null) {
            return null;
        }

        DtoRecibo recibo = new DtoRecibo();
        recibo.setId(venta.getId());
        recibo.setFecha(venta.getFecha());
        recibo.setTotal(venta.getTotal());
        recibo.setNitCliente(venta.getNitCliente());
        recibo.setNombreCliente(venta.getNombreCliente());
        recibo.setMetodoPago(venta.getMetodoPago());
        recibo.setVendedor(new Usuario_tab(venta.getVendedor()));
        recibo.setDetallesVenta(detallesVentaRepository.findByIdVenta(idVenta.getId()));

        return recibo;
    }

    public Page<DtoHistorialVentas> listarHistorialGeneral(DtoFiltroHistorialVentas filtro) {
        Pageable pageable = PageRequest.of(
                filtro.getPage(),
                filtro.getPageSize(),
                Sort.by(Sort.Direction.DESC, "fecha"));

        System.out.println("Buscador: " + filtro.getContextoBusqueda() + " Busqueda " + filtro.getBusqueda());

        if (filtro.getContextoBusqueda() == DtoFiltroHistorialVentas.ContextoBusqueda.buscadornit
                && !filtro.getBusqueda().trim().isEmpty()) {

            Page<Venta> listaventas = ventaRepository.findByNombreClienteContainingIgnoreCaseOrNitClienteContainingIgnoreCase(
                    filtro.getBusqueda(), filtro.getBusqueda(), pageable);
            return toDtoHistorial(listaventas);
        } else if (filtro.getContextoBusqueda() == DtoFiltroHistorialVentas.ContextoBusqueda.buscadorfecha) {

            LocalDate fecha = LocalDate.parse(filtro.getFecha(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            LocalDateTime inicioDelDia = fecha.atStartOfDay();
            LocalDateTime finDelDia = fecha.atTime(LocalTime.MAX);

            System.out.println("Se está ejecutando la accion de buscar por fecha: " + filtro.getFecha());

            Page<Venta> listaventas = ventaRepository.findByFechaBetween(inicioDelDia, finDelDia, pageable);
            return toDtoHistorial(listaventas);
        } else if (filtro.getContextoBusqueda() == DtoFiltroHistorialVentas.ContextoBusqueda.buscadornorecibo
                && !filtro.getBusqueda().trim().isEmpty()) {

            Page<Venta> listaventas = ventaRepository.findById(
                    Long.valueOf(filtro.getBusqueda()), pageable);
            return toDtoHistorial(listaventas);
        } else {
            System.out.println("Se ejecuto la excepcion");
            Page<Venta> listaventas = ventaRepository.findAll(pageable);
            return toDtoHistorial(listaventas);
        }
    }

    public Page<DtoHistorialVentas> listarHistorialTurno(DtoFiltroHistorialVentas filtro) {
        Pageable pageable = PageRequest.of(
                filtro.getPage(),
                filtro.getPageSize(),
                Sort.by(Sort.Direction.DESC, "fecha"));

        if (filtro.getContextoBusqueda() == DtoFiltroHistorialVentas.ContextoBusqueda.buscadornit
                && !filtro.getBusqueda().trim().isEmpty()) {

            Page<Venta> listaventas = ventaRepository
                    .findByIdTurnoAndNombreClienteContainingIgnoreCaseOrIdTurnoAndNitClienteContainingIgnoreCase(
                            Long.valueOf(filtro.getWinParametro()), filtro.getBusqueda(),
                            Long.valueOf(filtro.getWinParametro()), filtro.getBusqueda(), pageable);
            return toDtoHistorial(listaventas);
        } else if (filtro.getContextoBusqueda() == DtoFiltroHistorialVentas.ContextoBusqueda.buscadorfecha) {
            LocalDate fecha = LocalDate.parse(filtro.getFecha(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            LocalDateTime inicioDelDia = fecha.atStartOfDay();
            LocalDateTime finDelDia = fecha.atTime(LocalTime.MAX);

            Page<Venta> listaventas = ventaRepository.findByIdTurnoAndFechaBetween(
                    Long.valueOf(filtro.getWinParametro()), inicioDelDia, finDelDia, pageable);
            return toDtoHistorial(listaventas);
        }
        if (filtro.getContextoBusqueda() == DtoFiltroHistorialVentas.ContextoBusqueda.buscadornorecibo
                && !filtro.getBusqueda().trim().isEmpty()) {
            Page<Venta> listaventas = ventaRepository.findByIdTurnoAndId(
                    Long.valueOf(filtro.getWinParametro()),
                    Long.valueOf(filtro.getBusqueda()), pageable);
            return toDtoHistorial(listaventas);
        } else {
            Page<Venta> listaventas = ventaRepository.findByIdTurno(
                    Long.valueOf(filtro.getWinParametro()), pageable);
            return toDtoHistorial(listaventas);
        }
    }

    public Page<DtoHistorialVentas> listarHistorialCliente(DtoFiltroHistorialVentas filtro) {
        Pageable pageable = PageRequest.of(
                filtro.getPage(),
                filtro.getPageSize(),
                Sort.by(Sort.Direction.DESC, "fecha"));

        if (filtro.getContextoBusqueda() == DtoFiltroHistorialVentas.ContextoBusqueda.buscadorfecha) {

            LocalDate fecha = LocalDate.parse(filtro.getFecha(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            LocalDateTime inicioDelDia = fecha.atStartOfDay();
            LocalDateTime finDelDia = fecha.atTime(LocalTime.MAX);

            System.out.println("Se está ejecutando la accion de buscar por fecha");

            Page<Venta> listaventas = ventaRepository.findByNitClienteAndFechaBetween(
                    filtro.getWinParametro(), inicioDelDia, finDelDia, pageable);
            return toDtoHistorial(listaventas);
        } else if (filtro.getContextoBusqueda() == DtoFiltroHistorialVentas.ContextoBusqueda.buscadornorecibo
                && !filtro.getBusqueda().trim().isEmpty()) {
            Page<Venta> listaventas = ventaRepository.findByNitClienteAndId(
                    filtro.getWinParametro(), Long.valueOf(filtro.getBusqueda()), pageable);
            return toDtoHistorial(listaventas);
        } else {
            Page<Venta> listaventas = ventaRepository.findByNitCliente(
                    filtro.getWinParametro(), pageable);
            return toDtoHistorial(listaventas);
        }
    }

    public Page<DtoHistorialVentas> toDtoHistorial(Page<Venta> ventas) {
        return ventas.map(venta -> {
            DtoHistorialVentas dto = new DtoHistorialVentas();
            dto.setId(venta.getId());
            dto.setFecha(venta.getFecha());
            dto.setNitCliente(venta.getNitCliente());
            dto.setNombreCliente(venta.getNombreCliente());
            dto.setTotal(venta.getTotal().toPlainString());
            dto.setMetodoPago(venta.getMetodoPago());
            dto.setEstado(venta.getEstado());
            return dto;
        });
    }

    public ResponseEntity<Resource> obtenerComprobanteDeposito(DtoIdVenta idVenta) {
        Venta venta = ventaRepository.findById(idVenta.getId()).orElse(null);

        if (venta == null || venta.getUrlComprobante() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path ruta = Paths.get(storageLocation).resolve(venta.getUrlComprobante()).normalize();
            Resource resource = new UrlResource(ruta.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Detectar content-type
                String contentType = Files.probeContentType(ruta);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header("X-Numero-Comprobante", venta.getNoDeposito()) // cabecera personalizada
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
