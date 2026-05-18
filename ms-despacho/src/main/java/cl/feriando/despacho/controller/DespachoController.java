package cl.feriando.despacho.controller;

import cl.feriando.despacho.dto.CambioEstadoDespachoDTO;
import cl.feriando.despacho.dto.DespachoRequestDTO;
import cl.feriando.despacho.dto.DespachoResponseDTO;
import cl.feriando.despacho.service.DespachoService;

import jakarta.validation.Valid;
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

import java.util.List;
/**
 * controlador REST del despacho.
 */
@RestController
@RequestMapping("/despachos")
public class DespachoController {

    private final DespachoService service;

    public DespachoController(DespachoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DespachoResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DespachoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<DespachoResponseDTO> obtenerPorPedido(@PathVariable Long idPedido) {
        return ResponseEntity.ok(service.obtenerPorPedido(idPedido));
    }

    @PostMapping
    public ResponseEntity<DespachoResponseDTO> crear(@Valid @RequestBody DespachoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DespachoResponseDTO> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody DespachoRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<DespachoResponseDTO> cambiarEstado(@PathVariable Long id,
                                                             @Valid @RequestBody CambioEstadoDespachoDTO dto) {
        return ResponseEntity.ok(service.cambiarEstado(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
