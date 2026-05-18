package cl.feriando.feriante.controller;

import cl.feriando.feriante.dto.FerianteRequestDTO;
import cl.feriando.feriante.dto.FerianteResponseDTO;
import cl.feriando.feriante.service.FerianteService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
/**
 * controlador REST de feriantes.
 * sigue el mismo patron delgado que UsuarioController: solo traduce HTTP a
 * llamadas al service y selecciona el codigo de respuesta adecuado.
 */
@RestController
@RequestMapping("/feriantes")
public class FerianteController {

    private final FerianteService service;

    public FerianteController(FerianteService service) {
        this.service = service;
    }
    // GET /feriantes
    @GetMapping
    public ResponseEntity<List<FerianteResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }
    // GET /feriantes/{id}. si no existe el service lanza 404.
    @GetMapping("/{id}")
    public ResponseEntity<FerianteResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }
    // POST /feriantes con @Valid para activar las restricciones del DTO.
    // 201 Created indica que se creo un recurso nuevo.
    @PostMapping
    public ResponseEntity<FerianteResponseDTO> crear(@Valid @RequestBody FerianteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }
    // PUT /feriantes/{id}: reemplaza el estado modificable del feriante.
    @PutMapping("/{id}")
    public ResponseEntity<FerianteResponseDTO> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody FerianteRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }
    // DELETE -> 204 No Content (semantica REST).
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * endpoint usado internamente por ms-calificacion.
     */
    @PatchMapping("/{id}/calificacion-promedio")
    public ResponseEntity<FerianteResponseDTO> actualizarPromedio(
            @PathVariable Long id,
            @RequestBody Map<String, @NotNull BigDecimal> body) {
        BigDecimal promedio = body.get("promedio");
        return ResponseEntity.ok(service.actualizarCalificacionPromedio(id, promedio));
    }
}
