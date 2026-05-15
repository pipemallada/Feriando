package cl.feriando.feriante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record FerianteRequestDTO(

        @NotNull(message = "El id_usuario es obligatorio")
        @Positive(message = "El id_usuario debe ser positivo")
        Long idUsuario,

        @NotBlank(message = "El nombre del puesto es obligatorio")
        @Size(max = 150)
        String nombrePuesto,

        @Size(max = 1000)
        String descripcion,

        @Size(max = 20)
        String telefono
) { }
