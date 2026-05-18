package cl.feriando.calificacion.controller;

import cl.feriando.calificacion.dto.CalificacionRequestDTO;
import cl.feriando.calificacion.dto.CalificacionResponseDTO;
import cl.feriando.calificacion.service.CalificacionService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * controlador REST de calificaciones.
 *
 * no expone PUT: una calificacion no se edita. si el cliente quiere
 * cambiar su nota, debe DELETE + POST nuevamente.
 */
@RestController
@RequestMapping("/calificaciones")
public class CalificacionController {

    private final CalificacionService service;

    public CalificacionController(CalificacionService service) {
        this.service = service;
    }

    // GET /calificaciones                   -> todas
    // GET /calificaciones?idFeriante=N      -> solo las del feriante

    @GetMapping
    public ResponseEntity<List<CalificacionResponseDTO>> listar(@RequestParam(required = false) Long idFeriante) {
        if (idFeriante != null) {
            return ResponseEntity.ok(service.listarPorFeriante(idFeriante));
        }
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalificacionResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }
    // POST: crea la calificacion y lanza el recalculo del promedio.

    @PostMapping
    public ResponseEntity<CalificacionResponseDTO> crear(@Valid @RequestBody CalificacionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }
    // DELETE: borra y lanza el recalculo del promedio.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
