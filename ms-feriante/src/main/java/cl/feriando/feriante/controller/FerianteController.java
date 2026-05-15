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

@RestController
@RequestMapping("/feriantes")
public class FerianteController {

    private final FerianteService service;

    public FerianteController(FerianteService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<FerianteResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FerianteResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<FerianteResponseDTO> crear(@Valid @RequestBody FerianteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FerianteResponseDTO> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody FerianteRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/calificacion-promedio")
    public ResponseEntity<FerianteResponseDTO> actualizarPromedio(
            @PathVariable Long id,
            @RequestBody Map<String, @NotNull BigDecimal> body) {
        BigDecimal promedio = body.get("promedio");
        return ResponseEntity.ok(service.actualizarCalificacionPromedio(id, promedio));
    }
}
