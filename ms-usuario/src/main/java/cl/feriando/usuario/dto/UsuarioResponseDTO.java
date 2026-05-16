package cl.feriando.usuario.dto;

import java.time.LocalDateTime;
/**
 * DTO de salida (lo que devuelve el API al cliente).
 * El DTO de salida puede contener campos calculados o agregados que no
 *  existen en la entidad (aca no, pero la separacion nos deja la puerta
 *   abierta).
 */
public record UsuarioResponseDTO(
        Long idUsuario,
        String nombre,
        String apellido,
        String email,
        String rol,
        Short activo,
        LocalDateTime createdAt
) { }
