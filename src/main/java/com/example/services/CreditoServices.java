package com.example.services;

import com.example.DTO.DtoAbonoComprobante;
import com.example.DTO.DtoCredito;
import com.example.DTO.DtoEfectivoCaja;
import com.example.DTO.DtoFiltroHistorialabonos;
import com.example.DTO.DtoId;
import com.example.DTO.DtoIdPage;
import com.example.DTO.DtoIdVenta;
import com.example.DTO.DtoItemCredito;
import com.example.DTO.DtoItemHistorialabonos;
import com.example.DTO.DtoRegistrarAbono;
import com.example.DTO.DtoResponse;
import com.example.DTO.DtoResponseOb;
import com.example.DTO.RespuestaPaginada;
import com.example.Repository.AbonoRepository;
import com.example.Repository.ClienteRepository;
import com.example.Repository.CreditoRepository;
import com.example.Repository.UsuarioRepository;
import com.example.domain.Abono;
import com.example.domain.Cliente;
import com.example.domain.Credito;
import com.example.domain.Usuario;
import com.example.domain.Venta;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CreditoServices {

    @Autowired
    public CreditoRepository creditoRepository;

    @Autowired
    public ClienteRepository clienteRepository;

    @Autowired
    public AbonoRepository abonoRepository;

    @Autowired
    public TurnoServices turnoServices;

    @Autowired
    public UsuarioRepository usuarioRepository;

    @Value("${storage.location.comprobantes.abonos}")
    private String storageLocation;

    public DtoResponse RegistrarCredito(Long idVenta, Long idCliente, LocalDate plazoPago, BigDecimal montoTotal) {

        if (creditoRepository.existsByIdClienteAndEstado(idCliente, "Vencido")) {
            return new DtoResponse(false, "El cliente tiene un recibo vencido. Debe cancelarlo antes de poder comprar a crédito.");
        }

        if (plazoPago.isBefore(LocalDate.now())) {
            return new DtoResponse(false, "El plazo de pago no puede ser anterior a la fecha de emisión.");
        }

        Credito credito = new Credito();
        credito.setIdVenta(idVenta);
        credito.setIdCliente(idCliente);
        credito.setFechaEmision(LocalDate.now());
        credito.setPlazoPago(plazoPago);
        credito.setMontoTotal(montoTotal);
        credito.setSaldoPendiente(montoTotal);
        credito.setEstado("PENDIENTE");
        creditoRepository.save(credito);
        return new DtoResponse(true, "Credito registrado exitosamente");
    }

    public DtoCredito datosCredito(DtoIdPage dto) {
        Cliente cliente = clienteRepository.findById(dto.getId()).orElse(null);

        if (cliente == null) {
            return null;
        }

        Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize());
        String estadoGeneral = "AL DIA";
        BigDecimal deudaTotal = new BigDecimal(0.00);

        Page<Credito> pagCreditos = creditoRepository.findByIdClienteOrderByEstado(cliente.getId(), pageable);

        List<Credito> creditos = creditoRepository.findByIdClienteAndEstadoNot(cliente.getId(), "PAGADO");

        for (Credito credito : creditos) {

            deudaTotal = deudaTotal.add(credito.getSaldoPendiente());
            if ("VENCIDO".equalsIgnoreCase(credito.getEstado())) {
                estadoGeneral = "VENCIDO";
            } else if ("PENDIENTE".equalsIgnoreCase(credito.getEstado())
                    && !estadoGeneral.equalsIgnoreCase("VENCIDO")) {
                estadoGeneral = "PENDIENTE";
            }
        }

        DtoCredito dtoCredito = new DtoCredito(cliente.getNombre(), cliente.getNit(),
                deudaTotal, estadoGeneral, toItemCredito(pagCreditos));
        System.out.println("id: " + dtoCredito.getNombreCliente() + " Saldo a pagar: " + deudaTotal + " Estado: " + estadoGeneral);
        return dtoCredito;
    }

    private RespuestaPaginada<DtoItemCredito> toItemCredito(Page<Credito> creditos) {
        return new RespuestaPaginada<>(creditos.map(DtoItemCredito::new));
    }

    public Map<Long, String> getEstadosClientes(List<Long> idsClientes) {
        List<Object[]> resultados = creditoRepository.calcularEstadosClientes(idsClientes);
        return resultados.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (String) row[1]
                ));
    }

    public DtoResponseOb<DtoId> registrarAbonoEfectivo(DtoRegistrarAbono dtoAbono) {
        try {
            return registrarAbonoEfectivoTransactional(dtoAbono);
        } catch (Exception e) {
            return new DtoResponseOb<>(false, e.getMessage(), null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public DtoResponseOb<DtoId> registrarAbonoEfectivoTransactional(DtoRegistrarAbono dtoAbono) {
        Credito credito = creditoRepository.findById(dtoAbono.getIdCredito())
                .orElseThrow(() -> new RuntimeException("¡El crédito seleccionado no existe!"));

        BigDecimal saldoPendiente = credito.getSaldoPendiente();
        BigDecimal montoAbono = dtoAbono.getMontoAbonado();

        if (credito.getEstado().equalsIgnoreCase("PAGADO")
                && credito.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Inconsistencia: crédito marcado como pagado pero aún tiene saldo.");
        }

        if (!credito.getEstado().equalsIgnoreCase("PAGADO")
                && credito.getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("Inconsistencia: crédito con saldo 0 pero no está marcado como pagado.");
        }

        if (credito.getEstado().equalsIgnoreCase("PAGADO")) {
            throw new RuntimeException("¡El credito ya ha sido pagado!");
        }

        if (saldoPendiente.compareTo(montoAbono) < 0) {
            throw new RuntimeException("¡El monto a abonar no puede ser mayor al saldo pendiente!");
        }

        if (montoAbono.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto a abonar debe ser mayor que 0");
        }

        BigDecimal saldoAnterior = saldoPendiente;

        saldoPendiente = saldoPendiente.subtract(montoAbono);
        BigDecimal nuevoSaldo = saldoPendiente;
        credito.setSaldoPendiente(saldoPendiente);

        String estadoCredito = credito.getEstado();

        if (saldoPendiente.compareTo(BigDecimal.ZERO) == 0) {
            estadoCredito = "PAGADO";
        }

        Abono abono = new Abono();
        abono.setIdCredito(dtoAbono.getIdCredito());
        abono.setIdCliente(credito.getIdCliente());
        abono.setIdVenta(credito.getIdVenta());
        abono.setFechaAbono(LocalDateTime.now());
        abono.setMontoAbonado(montoAbono);
        abono.setSaldoAnterior(saldoAnterior);
        abono.setNuevoSaldo(nuevoSaldo);
        abono.setEstadoCredito(estadoCredito);
        abono.setIdVendedor(dtoAbono.getIdVendedor());
        abono.setFormaPago(dtoAbono.getFormaPago());
        abono = abonoRepository.save(abono);

        credito.setEstado(estadoCredito);
        creditoRepository.save(credito);

        DtoResponse turno = turnoServices.ingresoRetiroEfectivo(
                new DtoEfectivoCaja(dtoAbono.getIdTurno(), "Cobro de credito en efectivo",
                        montoAbono, "Abono No." + abono.getId() + " (Efectivo) - Recibo No." + credito.getIdVenta()));

        if (!turno.isSuccess()) {
            throw new RuntimeException(turno.getMessage());
        }

        return new DtoResponseOb<>(true, "Abono registrado exitosamente",
                new DtoId(abono.getId()));
    }

    public DtoResponseOb<DtoId> registrarAbonoDeposito(DtoRegistrarAbono dtoAbono, MultipartFile comprobante) {
        try {
            return registrarAbonoDepositoTransactional(dtoAbono, comprobante);
        } catch (Exception e) {
            return new DtoResponseOb<>(false, e.getMessage(), null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public DtoResponseOb<DtoId> registrarAbonoDepositoTransactional(DtoRegistrarAbono dtoAbono, MultipartFile comprobante) throws IOException {
        Credito credito = creditoRepository.findById(dtoAbono.getIdCredito())
                .orElseThrow(() -> new RuntimeException("¡El crédito seleccionado no existe!"));

        BigDecimal saldoPendiente = credito.getSaldoPendiente();
        BigDecimal montoAbono = dtoAbono.getMontoAbonado();

        if (credito.getEstado().equalsIgnoreCase("PAGADO")
                && credito.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Inconsistencia: crédito marcado como pagado pero aún tiene saldo.");
        }

        if (!credito.getEstado().equalsIgnoreCase("PAGADO")
                && credito.getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("Inconsistencia: crédito con saldo 0 pero no está marcado como pagado.");
        }

        if (credito.getEstado().equalsIgnoreCase("PAGADO")) {
            throw new RuntimeException("¡El credito ya ha sido pagado!");
        }

        if (saldoPendiente.compareTo(montoAbono) < 0) {
            throw new RuntimeException("¡El monto a abonar no puede ser mayor al saldo pendiente!");
        }

        if (montoAbono.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto a abonar debe ser mayor que 0");
        }

        BigDecimal saldoAnterior = saldoPendiente;

        saldoPendiente = saldoPendiente.subtract(montoAbono);
        BigDecimal nuevoSaldo = saldoPendiente;
        credito.setSaldoPendiente(saldoPendiente);

        String estadoCredito = credito.getEstado();

        if (saldoPendiente.compareTo(BigDecimal.ZERO) == 0) {
            estadoCredito = "PAGADO";
        }

        Abono abono = new Abono();
        abono.setIdCredito(dtoAbono.getIdCredito());
        abono.setIdCliente(credito.getIdCliente());
        abono.setIdVenta(credito.getIdVenta());
        abono.setFechaAbono(LocalDateTime.now());
        abono.setMontoAbonado(montoAbono);
        abono.setSaldoAnterior(saldoAnterior);
        abono.setNuevoSaldo(nuevoSaldo);
        abono.setEstadoCredito(estadoCredito);
        abono.setIdVendedor(dtoAbono.getIdVendedor());
        abono.setFormaPago(dtoAbono.getFormaPago());

        if (comprobante != null && !comprobante.isEmpty()) {
            Path rutaCarpeta = Paths.get(storageLocation).toAbsolutePath().normalize();
            Files.createDirectories(rutaCarpeta);

            // Generar nombre único para el comprobante
            String nombreOriginal = comprobante.getOriginalFilename();
            String nombreArchivoFinal = System.currentTimeMillis() + "_"
                    + (nombreOriginal != null ? nombreOriginal.replaceAll("\\s+", "_") : "abono.png");

            // Guardar físicamente el archivo
            Path rutaDestino = rutaCarpeta.resolve(nombreArchivoFinal);
            Files.copy(comprobante.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

            // Guardar la ruta/nombre en la BD
            abono.setUrlDeposito(nombreArchivoFinal);
            abono.setNoDeposito(dtoAbono.getNoDeposito()); // este lo recibes del formulario
            System.out.println("Comprobante guardado: " + nombreArchivoFinal);
        } else {
            throw new RuntimeException("Debe adjuntar comprobante para registrar un abono con depósito.");
        }

        abono = abonoRepository.save(abono);

        if (saldoPendiente.compareTo(BigDecimal.ZERO) == 0) {
            credito.setEstado("PAGADO");
        }
        credito.setEstado(estadoCredito);
        creditoRepository.save(credito);

        DtoResponse turno = turnoServices.ingresoRetiroEfectivo(
                new DtoEfectivoCaja(dtoAbono.getIdTurno(), "Cobro de credito con deposito",
                        montoAbono, "Abono No." + abono.getId() + " (Depósito) - Crédito de Recibo No." + credito.getIdVenta()));

        if (!turno.isSuccess()) {
            throw new RuntimeException(turno.getMessage());
        }

        return new DtoResponseOb<>(true, "Abono registrado exitosamente",
                new DtoId(abono.getId()));
    }

    public DtoAbonoComprobante obtenerAbonoComprobante(DtoId dtoId) {
        if (dtoId.getId() <= 0) {
            return null;
        }

        Abono abono = abonoRepository.findById(dtoId.getId()).orElse(null);
        Credito credito = creditoRepository.findById(abono.getIdCredito()).orElse(null);
        Usuario usuario = usuarioRepository.findById(abono.getIdVendedor()).orElse(null);

        Cliente cliente = clienteRepository.findById(credito.getIdCliente()).orElse(null);

        DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern(" HH:mm a");
        String fecha = abono.getFechaAbono().format(formatterFecha);
        String hora = abono.getFechaAbono().format(formatterHora);

        DtoAbonoComprobante dtoAbono = new DtoAbonoComprobante();
        dtoAbono.setId(abono.getId());
        dtoAbono.setFechaAbono(fecha);
        dtoAbono.setHoraAbono(hora);
        dtoAbono.setNombreVendedor(usuario.getNombre());
        dtoAbono.setNombreCliente(cliente.getNombre());
        dtoAbono.setNitCliente(cliente.getNit());
        dtoAbono.setNoDocumento("Recibo No." + credito.getIdVenta());
        dtoAbono.setMontoTotalCredito("Q " + credito.getMontoTotal()
                .setScale(2, RoundingMode.HALF_UP).toPlainString());
        dtoAbono.setMontoAbonado("Q " + abono.getMontoAbonado()
                .setScale(2, RoundingMode.HALF_UP).toPlainString());
        dtoAbono.setSaldoAnterior("Q " + abono.getSaldoAnterior()
                .setScale(2, RoundingMode.HALF_UP).toPlainString());
        dtoAbono.setNuevoSaldo("Q " + abono.getNuevoSaldo()
                .setScale(2, RoundingMode.HALF_UP).toPlainString());
        dtoAbono.setEstadoCredito(abono.getEstadoCredito());
        if (abono.getFormaPago().equalsIgnoreCase("Efectivo")) {
            dtoAbono.setObservaciones("Pago en efectivo");
        } else {
            dtoAbono.setObservaciones("Pago con depósito");
        }
        return dtoAbono;
    }

    public Page<DtoItemHistorialabonos> listarAbonos(DtoFiltroHistorialabonos filtro) {

        Pageable pageable = PageRequest.of(filtro.getPage(), filtro.getSize());
        Page<Abono> listaAbonos = null;

        Long idBusqueda = 0L;
        try {
            idBusqueda = Long.valueOf(filtro.getContentBusqueda());
        } catch (NumberFormatException e) {
            idBusqueda = 0L;
        }

        if (filtro.getContextoBusqueda().equalsIgnoreCase("No. Recibo")) {
            if (filtro.getContentBusqueda().trim().isEmpty()) {
                listaAbonos = abonoRepository.findByIdClienteOrderByFechaAbonoDesc(
                        filtro.getIdCliente(), pageable);
            } else {
                listaAbonos = abonoRepository.findByIdClienteAndIdVentaOrderByFechaAbonoDesc(
                        filtro.getIdCliente(), idBusqueda, pageable);
            }
        } else {
            if (filtro.getContentBusqueda().trim().isEmpty()) {
                listaAbonos = abonoRepository.findByIdClienteOrderByFechaAbonoDesc(
                        filtro.getIdCliente(), pageable);
            } else {
                listaAbonos = abonoRepository.findByIdClienteAndIdOrderByFechaAbonoDesc(
                        filtro.getIdCliente(), idBusqueda, pageable);
            }
        }
        return toDtoHistorialAbono(listaAbonos);
    }

    private Page<DtoItemHistorialabonos> toDtoHistorialAbono(Page<Abono> abonos) {
        return abonos.map(DtoItemHistorialabonos::new);
    }
    
    public ResponseEntity<Resource> obtenerComprobanteDeposito(DtoId idAbono) {
        Abono abono = abonoRepository.findById(idAbono.getId()).orElse(null);

        if (abono == null || abono.getUrlDeposito()== null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path ruta = Paths.get(storageLocation).resolve(abono.getUrlDeposito()).normalize();
            Resource resource = new UrlResource(ruta.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Detectar content-type
                String contentType = Files.probeContentType(ruta);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header("X-Numero-Comprobante", abono.getNoDeposito()) // cabecera personalizada
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
