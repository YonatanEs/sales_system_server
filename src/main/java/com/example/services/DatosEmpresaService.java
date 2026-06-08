package com.example.services;

import com.example.DTO.DtoResponse;
import com.example.DTO.Dto_datosEmpresa;
import com.example.Repository.DatosEmpresaRepository;
import com.example.domain.DatosEmpresa;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DatosEmpresaService {

    @Autowired
    private DatosEmpresaRepository datosRepository;

    @Value("${storage.location}")
    private String storageLocation;

    public DtoResponse modificarDatosEmpresa(Dto_datosEmpresa empresa, MultipartFile logo) {
        DatosEmpresa datos = datosRepository.findById(1L).orElse(null);
        if (datos == null) {
            return new DtoResponse(false, "ha ocurrido un error al actualizar la informacion de la empresa");
        }
        try {
            datos.setNombre(empresa.getNombre());
            datos.setNit(empresa.getNit());
            datos.setTelefono(empresa.getTelefono());
            datos.setDireccion(empresa.getDireccion());
            datos.setSlogan(empresa.getSlogan());

            if (logo != null && !logo.isEmpty()) {
                Path rutaCarpeta = Paths.get(storageLocation).toAbsolutePath().normalize();
                Files.createDirectories(rutaCarpeta);

                // Opcional: Si ya había un logo viejo guardado, lo borramos del disco para no dejar basura
                if (datos.getLogo() != null && !datos.getLogo().isEmpty()) {
                    Path rutaLogoViejo = rutaCarpeta.resolve(datos.getLogo());
                    Files.deleteIfExists(rutaLogoViejo);
                }

                // Generar un nombre único para el nuevo archivo (evita colisiones de nombres)
                String nombreOriginal = logo.getOriginalFilename();
                String nombreArchivoFinal = System.currentTimeMillis() + "_"
                        + (nombreOriginal != null ? nombreOriginal.replaceAll("\\s+", "_") : "logo.png");

                // Guardar el archivo físicamente en el servidor/PC de la API
                Path rutaDestino = rutaCarpeta.resolve(nombreArchivoFinal);
                Files.copy(logo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

                // Guardamos el NOMBRE del archivo en el campo de la base de datos
                datos.setLogo(nombreArchivoFinal);
                System.out.println("Nombre de logo: " + logo);
            }
            datosRepository.save(datos);
            return new DtoResponse(true, "Información de la empresa actualizada correctamente.");
        } catch (IOException e) {
            // Captura errores específicos de escritura de archivos (ej: sin permisos en el disco)
            return new DtoResponse(false, "Error al guardar el archivo del logo: " + e.getMessage());
        } catch (Exception e) {
            // Captura cualquier otro error inesperado (ej: fallo de conexión a MySQL)
            return new DtoResponse(false, "Error inesperado al modificar los datos: " + e.getMessage());
        }
    }

    public DatosEmpresa obtenerDatosEmpresa() {
        return datosRepository.findById(1L).orElse(null);
    }

    public Resource cargarLogo(String nombreLogo) throws MalformedURLException {
        Path rutaArchivo = Paths.get(storageLocation).resolve(nombreLogo).toAbsolutePath();
        Resource recurso = new UrlResource(rutaArchivo.toUri());

        if (recurso.exists() || recurso.isReadable()) {
            return recurso;
        } else {
            Path rutaDefault = Paths.get(storageLocation).resolve("default.png").toAbsolutePath();
            return new UrlResource(rutaDefault.toUri());
        }
    }
}
