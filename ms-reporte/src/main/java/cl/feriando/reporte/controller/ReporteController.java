package cl.feriando.reporte.controller;

import cl.feriando.reporte.dto.ReporteVentasRequestDTO;
import cl.feriando.reporte.dto.ReporteVentasResponseDTO;
import cl.feriando.reporte.service.ReporteService;

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

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ReporteVentasResponseDTO>> listar(@RequestParam(required = false) Long idFeriante) {
        if (idFeriante != null) {
            return ResponseEntity.ok(service.listarPorFeriante(idFeriante));
        }
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReporteVentasResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ReporteVentasResponseDTO> crear(@Valid @RequestBody ReporteVentasRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PostMapping("/generar/feriante/{idFeriante}")
    public ResponseEntity<ReporteVentasResponseDTO> generar(@PathVariable Long idFeriante) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.generarParaFeriante(idFeriante));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
