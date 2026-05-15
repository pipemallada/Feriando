package cl.feriando.feriante.dto;

import java.math.BigDecimal;

public record FerianteResponseDTO(
        Long idFeriante,
        Long idUsuario,
        String nombrePuesto,
        String descripcion,
        String telefono,
        BigDecimal calificacionProm,
        Short activo
) { }
