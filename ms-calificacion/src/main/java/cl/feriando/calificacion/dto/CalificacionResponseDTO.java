package cl.feriando.calificacion.dto;

import java.time.LocalDateTime;
/**
 * DTO de salida de la calificacion. la entidad se devuelve
 * al cliente sin exponer la entidad JPA directamente.
 */
public record CalificacionResponseDTO(
        Long idCalificacion,
        Long idPedido,
        Long idCliente,
        Long idFeriante,
        Short puntaje,
        String comentario,
        LocalDateTime fecha
) { }
