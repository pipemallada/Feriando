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

/**
 * controlador REST del carrito.
 * endpoints anidados /carritos/{id}/items: convencion REST para expresar
 * que los items son sub-recursos del carrito (no existen sin él).
 */
@RestController
@RequestMapping("/carritos")
public class CarritoController {

    private final CarritoService service;

    public CarritoController(CarritoService service) {
        this.service = service;
    }
    // GET /carritos
    @GetMapping
    public ResponseEntity<List<CarritoResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }
    // GET /carritos/{id}: incluye los detalles (relacion EAGER).
    @GetMapping("/{id}")
    public ResponseEntity<CarritoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }
    // POST /carritos: crea un carrito vacío. Los ítems se agregan después.
    @PostMapping
    public ResponseEntity<CarritoResponseDTO> crear(@Valid @RequestBody CarritoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }
    // POST /carritos/{id}/items: agrega un item al carrito existente.
    // 201 porque crea un sub-recurso (el detalle).
    @PostMapping("/{id}/items")
    public ResponseEntity<CarritoResponseDTO> agregarItem(@PathVariable Long id,
                                                          @Valid @RequestBody DetalleCarritoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.agregarItem(id, dto));
    }
    // DELETE /carritos/{id}/items/{idDetalle}: saca un item especifico.
    // devuelve 200 con el carrito actualizado (no 204) porque el cliente
    // suele querer el nuevo total inmediatamente.
    @DeleteMapping("/{id}/items/{idDetalle}")
    public ResponseEntity<CarritoResponseDTO> removerItem(@PathVariable Long id,
                                                          @PathVariable Long idDetalle) {
        return ResponseEntity.ok(service.removerItem(id, idDetalle));
    }
    // PATCH /carritos/{id}/cerrar: transiciona el carrito a cerrado.
    // PATCH = cambio parcial (solo el estado).
    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<CarritoResponseDTO> cerrar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cerrar(id));
    }
    // DELETE /carritos/{id}: borra completamente. orphanRemoval se encarga
    // de los detalles.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
