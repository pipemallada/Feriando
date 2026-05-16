package cl.feriando.usuario.controller;

import cl.feriando.usuario.dto.UsuarioRequestDTO;
import cl.feriando.usuario.dto.UsuarioResponseDTO;
import cl.feriando.usuario.service.UsuarioService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * controlador REST de usuarios.
 * su unico trabajo es traducir HTTP a llamadas al service y mapear los
 *   codigos de respuesta.
 *  esto hace el codigo más facil de leer y mantiene la regla CSR
 *   (Controller Service Repository) sin mezclar responsabilidades.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    // una sola dependencia, el service. el controller no conoce ni el
    // repositorio ni el mapper.
    private final UsuarioService service;

    // Inyección por constructor
    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    //  ResponseEntity nos permite controlar status, headers y body de forma uniforme.
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    // GET /usuarios/{id}. @PathVariable extrae el id de la URL.
    // si no existe, el service lanza ResourceNotFoundException -> 404
    // (manejado por GlobalExceptionHandler).
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }
    // POST /usuarios. @Valid arroja las validaciones de UsuarioRequestDTO.
    // sii fallan -> MethodArgumentNotValidException -> 400 con detalle por campo.
    //  devolvemos 201 Created (no 200) porque la convención REST lo exige.
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }
    // PUT /usuarios/{id}. el mismo DTO de entrada (UsuarioRequestDTO)
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(@PathVariable Long id,
                                                         @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }
    // DELETE /usuarios/{id} -> 204 No Content. no devolvemos body porque
    // "ya no hay un recurso que mostrar".
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
