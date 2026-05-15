package cl.feriando.carrito.controller;

import cl.feriando.carrito.dto.CarritoRequestDTO;
import cl.feriando.carrito.dto.CarritoResponseDTO;
import cl.feriando.carrito.dto.DetalleCarritoRequestDTO;
import cl.feriando.carrito.service.CarritoService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/carritos")
public class CarritoController {

    private final CarritoService service;

    public CarritoController(CarritoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CarritoResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarritoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<CarritoResponseDTO> crear(@Valid @RequestBody CarritoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<CarritoResponseDTO> agregarItem(@PathVariable Long id,
                                                          @Valid @RequestBody DetalleCarritoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.agregarItem(id, dto));
    }

    @DeleteMapping("/{id}/items/{idDetalle}")
    public ResponseEntity<CarritoResponseDTO> removerItem(@PathVariable Long id,
                                                          @PathVariable Long idDetalle) {
        return ResponseEntity.ok(service.removerItem(id, idDetalle));
    }

    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<CarritoResponseDTO> cerrar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cerrar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
