package com.example.services;

import com.example.DTO.DtoResponse;
import com.example.DTO.ModificarUsuario;
import com.example.DTO.RegistrarUsuario;
import com.example.DTO.Tabla_paginacion;
import com.example.DTO.Usuario_tab;
import com.example.Repository.UsuarioRepository;
import com.example.domain.Usuario;
import com.example.security.CustomUserDetails;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServices {

    public UsuarioRepository usuarioRepository;

    public UsuarioServices(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario_tab> listarUsuarios(String valor) {
        List<Usuario> usuarios;
        if (valor.isEmpty()) {
            usuarios = usuarioRepository.findAllByOrderByEstadoAsc();
        } else {
            usuarios = usuarioRepository.findByNombreContainingIgnoreCaseOrUsernameContainingIgnoreCase(valor, valor);
        }

        return usuarios.stream()
                .map(u -> new Usuario_tab(
                u.getId().intValue(),
                u.getNombre(),
                u.getTelefono(),
                u.getUsername(),
                u.getPermisos(),
                u.getEstado()
        ))
                .collect(Collectors.toList());
    }

    public DtoResponse registrarUsuario(RegistrarUsuario request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            return new DtoResponse(false, "¡El usuario ya existe!");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setTelefono(request.getTelefono());
        usuario.setUsername(request.getUsername());
        usuario.setPassword(request.getPassword());
        usuario.setPermisos(request.getPermisos());
        usuario.setEstado("Activo");
        usuarioRepository.save(usuario);

        return new DtoResponse(true, "¡El usuario ha sido registrado correctamente!");
    }

    public DtoResponse modificarUsuario(ModificarUsuario request) {
        if (usuarioRepository.existsByUsernameAndIdNot(request.getUsername(), request.getId())) {
            return new DtoResponse(false, "¡El usuario ya existe!");
        }
        if (usuarioRepository.countByPermisos("Administrador") == 1 && request.getPermisos().equals("empleado")) {
            return new DtoResponse(false,
                    "No puedes cambiar este usuario administrador a empleado porque es el único administrador restante");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long idAuth = userDetails.getId();

        // Bloquear si el usuario autenticado intenta degradarse a empleado
        if (request.getId().equals(idAuth)
                && request.getPermisos().equalsIgnoreCase("empleado")) {
            return new DtoResponse(false,
                    "No puedes degradar tu propio usuario a empleado");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(request.getId());
        if (usuarioOpt.isEmpty()) {
            return new DtoResponse(false, "Seleccione una fila");
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setNombre(request.getNombre());
        usuario.setTelefono(request.getTelefono());
        usuario.setUsername(request.getUsername());

        if (request.isActualizarPassword()) {
            usuario.setPassword(request.getPassword());
        }

        usuario.setPermisos(request.getPermisos());
        usuarioRepository.save(usuario);

        return new DtoResponse(true, "¡El usuario ha sido modificado correctamente!");
    }

    public Usuario_tab usuarioSeleccionado(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return new Usuario_tab(
                usuario.getId().intValue(),
                usuario.getNombre(),
                usuario.getTelefono(),
                usuario.getUsername(),
                usuario.getPermisos(),
                usuario.getEstado()
        );
    }

    public DtoResponse inactivarUsuario(Long id) {
        Optional <Usuario> usuarioOp = usuarioRepository.findById(id);
        
        if(usuarioOp.isEmpty()){
            return new DtoResponse(false, "Selecciona una fila");
        }
        Usuario usuario = usuarioOp.get();
        
        if(usuario.getEstado().equals("Activo")){
            usuario.setEstado("Inactivo");
            usuarioRepository.save(usuario);
            return new DtoResponse(true, "¡Usuario Inactivado correctamente!");
        }else{
            usuario.setEstado("Activo");
            usuarioRepository.save(usuario);
            return new DtoResponse(true, "¡Usuario Activado correctamente!");
        }
    }
}
