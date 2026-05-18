package cl.feriando.feriante.dto;

import java.math.BigDecimal;
/**
 * DTO de salida del feriante.
 * el consumidor del API necesita estos datos para pintar el puesto.
 * solo aparecen en la respuesta para que sea claro que son administrados
 *   internamente y no se pueden setear desde fuera.
 */
public record FerianteResponseDTO(
        Long idFeriante,
        Long idUsuario,
        String nombrePuesto,
        String descripcion,
        String telefono,
        BigDecimal calificacionProm,
        Short activo
) { }
