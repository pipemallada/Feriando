package cl.feriando.pedido.controller;

import cl.feriando.pedido.dto.CambioEstadoDTO;
import cl.feriando.pedido.dto.PedidoRequestDTO;
import cl.feriando.pedido.dto.PedidoResponseDTO;
import cl.feriando.pedido.service.PedidoService;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listar(@RequestParam(required = false) Long idCliente,
                                                          @RequestParam(required = false) Long idFeriante) {
        if (idCliente != null) {
            return ResponseEntity.ok(service.listarPorCliente(idCliente));
        }
        if (idFeriante != null) {
            return ResponseEntity.ok(service.listarPorFeriante(idFeriante));
        }
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crear(@Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(@PathVariable Long id,
                                                           @Valid @RequestBody CambioEstadoDTO dto) {
        return ResponseEntity.ok(service.cambiarEstado(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
