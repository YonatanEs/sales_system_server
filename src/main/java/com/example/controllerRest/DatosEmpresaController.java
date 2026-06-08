package com.example.controllerRest;

import com.example.DTO.DtoResponse;
import com.example.DTO.Dto_datosEmpresa;
import com.example.domain.DatosEmpresa;
import com.example.services.DatosEmpresaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/empresa")
public class DatosEmpresaController {

    @Autowired
    private DatosEmpresaService empresaServices;

    @PostMapping("/guardar")
    public ResponseEntity<DtoResponse> guardarDatosEmpresa(
            @RequestParam(value = "datosJson") String datosJson,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Dto_datosEmpresa dto_empresa = mapper.readValue(datosJson, Dto_datosEmpresa.class);

            DtoResponse response = empresaServices.modificarDatosEmpresa(dto_empresa, logo);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new DtoResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/obtener")
    public ResponseEntity<Dto_datosEmpresa> obtenerEmpresa() {
        DatosEmpresa empresa = empresaServices.obtenerDatosEmpresa();
        if (empresa == null) {
            return ResponseEntity.notFound().build();
        }
        // Mapeas a tu DTO para enviar a Swing
        Dto_datosEmpresa dto = new Dto_datosEmpresa(  
                empresa.getNombre(), empresa.getNit(),
                empresa.getTelefono(), empresa.getDireccion(), empresa.getSlogan(),
                empresa.getLogo()
        );

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/logo/{nombreLogo:.+}")
    public ResponseEntity<Resource> verLogo(@PathVariable String nombreLogo, HttpServletRequest request) {
        try {
            Resource recurso = empresaServices.cargarLogo(nombreLogo);

            // Detectar el content type del archivo dinámicamente
            String contentType = request.getServletContext().getMimeType(recurso.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getFilename() + "\"")
                    .body(recurso);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
