package com.example.controllerRest;

import com.example.DTO.DtoResponse;
import com.example.DTO.ModificarCliente;
import com.example.DTO.RegistrarCliente;
import com.example.DTO.ValorRequest;
import com.example.services.ClienteServices;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.domain.Cliente;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("api/clientes")
public class ClienteController {
    
    private ClienteServices clienteServices;
    
    public ClienteController(ClienteServices clienteServices){
        this.clienteServices=clienteServices;
    }
    
    @PostMapping("/ListaClientes")
   public List<Cliente> listarCliente(@RequestBody ValorRequest valor){
       
       List<Cliente> listaCliente = clienteServices.listarClientes(valor.getValor());
       
       return listaCliente;
   }
    
   @PostMapping("/registrar")
   public ResponseEntity<DtoResponse> registrarCliente(@RequestBody RegistrarCliente request){
       
        DtoResponse response = clienteServices.registrarCliente(request);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response); 
   } 
   
   @PostMapping("/modificar")
   public ResponseEntity<DtoResponse> modificarCliente(@RequestBody ModificarCliente request){
       
        DtoResponse response = clienteServices.modificarCliente(request);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response); 
   } 
   
   @PostMapping("/inactivar")
   public ResponseEntity<DtoResponse> inactivarCliente(@RequestBody Long id){
       
        DtoResponse response = clienteServices.inactivarCliente(id);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response); 
   } 
   
   @PostMapping("/ClienteSeleccionado")
   public Cliente clienteSeleccionado(@RequestBody Long id){
       return clienteServices.clienteSeleccionado(id);
   }
   
   @GetMapping("/listarSugerencias")
    public List<String> listarSugerencias(){
        return clienteServices.listarSugerencias();
    }
    
}
