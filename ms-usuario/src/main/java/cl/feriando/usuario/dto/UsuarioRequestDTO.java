package cl.feriando.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100)
        String apellido,

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
