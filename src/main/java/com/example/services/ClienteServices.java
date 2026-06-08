package com.example.services;

import com.example.DTO.DtoResponse;
import com.example.DTO.ModificarCliente;
import com.example.DTO.RegistrarCliente;
import com.example.Repository.ClienteRepository;
import com.example.domain.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;


@Service
public class ClienteServices {
    
    public ClienteRepository clienteRepository;

    public ClienteServices(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listarClientes(String valor) {
        if (valor.isEmpty()) {
            return clienteRepository.findAllByOrderByEstadoAsc();
        } else {
            return clienteRepository.findByNombreContainingIgnoreCaseOrNitContainingIgnoreCase(valor, valor);
        }
    }

    public DtoResponse registrarCliente(RegistrarCliente request) {
        if (clienteRepository.existsByNit(request.getNit())) {
            return new DtoResponse(false, "¡ya existe un cliente con el mismo NIT!");
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre());
        cliente.setTelefono(request.getTelefono());
        cliente.setNit(request.getNit());
        cliente.setDireccion(request.getDireccion());
        cliente.setEstado("Activo");
        clienteRepository.save(cliente);

        return new DtoResponse(true, "¡El cliente ha sido registrado correctamente!");
    }

    public DtoResponse modificarCliente(ModificarCliente request) {
        if (clienteRepository.existsByNitAndIdNot(request.getNit(), request.getId())) {
            return new DtoResponse(false, "¡ya existe un cliente con el mismo NIT!");
        }
        
        Optional<Cliente> clienteOpt = clienteRepository.findById(request.getId());
        if (clienteOpt.isEmpty()) {
            return new DtoResponse(false, "Seleccione una fila");
        }

        Cliente cliente = clienteOpt.get();
        cliente.setNombre(request.getNombre());
        cliente.setTelefono(request.getTelefono());
        cliente.setNit(request.getNit());
        cliente.setDireccion(request.getDireccion());
        clienteRepository.save(cliente);

        return new DtoResponse(true, "¡El cliente ha sido modificado correctamente!");
    }

    public Cliente clienteSeleccionado(Long id) {
        return clienteRepository.findById(id).orElse(null);
    }

    public DtoResponse inactivarCliente(Long id) {
        Optional <Cliente> clienteOp = clienteRepository.findById(id);
        
        if(clienteOp.isEmpty()){
            return new DtoResponse(false, "Selecciona una fila");
        }
        Cliente cliente = clienteOp.get();
        
        if(cliente.getEstado().equals("Activo")){
            cliente.setEstado("Inactivo");
            clienteRepository.save(cliente);
            return new DtoResponse(true, "¡Cliente Inactivado correctamente!");
        }else{
            cliente.setEstado("Activo");
            clienteRepository.save(cliente);
            return new DtoResponse(true, "¡Cliente Activado correctamente!");
        }
    }
    
    public List<String> listarSugerencias(){
        return clienteRepository.listarSugerencias();
    }
}
