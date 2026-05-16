package cl.feriando.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
/**
 * DTO de entrada para crear o actualizar un usuario.

 * el DTO no se modifica accidentalmente entre capas.
 * genera automaticamente equals, hashCode, toString y los accesores
 *   (nombre(), apellido()...), reduciendo el codigo.
 *
 * las validaciones del API no son las mismas que las restricciones de la BD.
 * aqui podemos pedir un email con formato valido (@Email) sin que esa logica
 *   contamine la entidad JPA.
 */
public record UsuarioRequestDTO(
         // @NotBlank: la cadena no puede ser null, ni vacía, ni solo espacios.
        // Distinto de @NotNull, que si aceptaria una cadena vacía "".
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100)
        String apellido,
         // @Email valida la sintaxis del correo (no su existencia real).
         // el @NotBlank va antes porque @Email no valida null/vacio.
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 150)
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 255)
        String passwordHash,

        @NotBlank(message = "El rol es obligatorio (CLIENTE o FERIANTE)")
        @Size(max = 20)
        String rol
) { }
