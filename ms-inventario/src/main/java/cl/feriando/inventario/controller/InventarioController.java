package cl.feriando.inventario.controller;

import cl.feriando.inventario.dto.AjusteStockDTO;
import cl.feriando.inventario.dto.InventarioRequestDTO;
import cl.feriando.inventario.dto.InventarioResponseDTO;
import cl.feriando.inventario.service.InventarioService;

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

@RestController
@RequestMapping("/inventario")
public class InventarioController {

    private final InventarioService service;

    public InventarioController(InventarioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<InventarioResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<InventarioResponseDTO> obtenerPorProducto(@PathVariable Long idProducto) {
        return ResponseEntity.ok(service.obtenerPorProducto(idProducto));
    }

    @PostMapping
    public ResponseEntity<InventarioResponseDTO> crear(@Valid @RequestBody InventarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventarioResponseDTO> actualizar(@PathVariable Long id,
                                                            @Valid @RequestBody InventarioRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PatchMapping("/producto/{idProducto}/descontar")
    public ResponseEntity<InventarioResponseDTO> descontar(@PathVariable Long idProducto,
                                                           @Valid @RequestBody AjusteStockDTO dto) {
        return ResponseEntity.ok(service.descontarStock(idProducto, dto));
    }

    @PatchMapping("/producto/{idProducto}/reponer")
    public ResponseEntity<InventarioResponseDTO> reponer(@PathVariable Long idProducto,
                                                         @Valid @RequestBody AjusteStockDTO dto) {
        return ResponseEntity.ok(service.reponerStock(idProducto, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
