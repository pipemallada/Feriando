package cl.feriando.calificacion.dto;

import java.time.LocalDateTime;

public record CalificacionResponseDTO(
        Long idCalificacion,
        Long idPedido,
        Long idCliente,
        Long idFeriante,
        Short puntaje,
        String comentario,
        LocalDateTime fecha
) { }
