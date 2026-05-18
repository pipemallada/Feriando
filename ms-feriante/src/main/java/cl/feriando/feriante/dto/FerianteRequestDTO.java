package cl.feriando.feriante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
/**
 * dTO de entrada para crear o actualizar un feriante.
 * calificacion_prom se actualiza automaticamente por ms-calificacion.
 * activo se controla por endpoints especificos (no por el body de update).
 */
public record FerianteRequestDTO(
         // @NotNull + @Positive: garantizamos un Long > 0. el service despues
        // valida que ese id_usuario exista realmente en ms-usuario.
        @NotNull(message = "El id_usuario es obligatorio")
        @Positive(message = "El id_usuario debe ser positivo")
        Long idUsuario,
       // el nombre del puesto es lo que ve el cliente al navegar la feria.
        @NotBlank(message = "El nombre del puesto es obligatorio")
        @Size(max = 150)
        String nombrePuesto,
         // descripcion opcional. sin @NotBlank: puede venir null o vacia.
        @Size(max = 1000)
        String descripcion,
        // telefono opcional, maximo 20 caracteres (suficiente para +56 9 xxx).
        @Size(max = 20)
        String telefono
) { }
