package cl.feriando.usuario.dto;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long idUsuario,
        String nombre,
        String apellido,
        String email,
        String rol,
        Short activo,
        LocalDateTime createdAt
) { }
