package com.example.controllerRest;

import com.example.DTO.DtoResponse;
import com.example.DTO.ModificarUsuario;
import com.example.DTO.RegistrarUsuario;
import com.example.DTO.Tabla_paginacion;
import com.example.DTO.Usuario_tab;
import com.example.DTO.ValorRequest;
import com.example.services.UsuarioServices;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/usuarios")
public class UsuarioController {

   private UsuarioServices usuarioServices;
   
   public UsuarioController(UsuarioServices usuarioServices){
       this.usuarioServices=usuarioServices;
   }
   
   @PostMapping("/ListaUsuarios")
   public List<Usuario_tab> listarUsuarios(@RequestBody ValorRequest valor){
       
       List<Usuario_tab> listaUsuarios = usuarioServices.listarUsuarios(valor.getValor());
       
       return listaUsuarios;
   }
    
   @PostMapping("/registrar")
   public ResponseEntity<DtoResponse> registrarUsuario(@RequestBody RegistrarUsuario request){
       
        DtoResponse response = usuarioServices.registrarUsuario(request);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response); 
   } 
   
   @PostMapping("/modificar")
   public ResponseEntity<DtoResponse> modificarUsuario(@RequestBody ModificarUsuario request){
       
        DtoResponse response = usuarioServices.modificarUsuario(request);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response); 
   } 
   
   @PostMapping("/inactivar")
   public ResponseEntity<DtoResponse> inactivarUsuario(@RequestBody Long id){
       
        DtoResponse response = usuarioServices.inactivarUsuario(id);
        
        if(!response.isSuccess()){
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response); 
   } 
   
   @PostMapping("/UsuarioSeleccionado")
   public Usuario_tab usuarioSeleccionado(@RequestBody Long id){
       return usuarioServices.usuarioSeleccionado(id);
   }
}
