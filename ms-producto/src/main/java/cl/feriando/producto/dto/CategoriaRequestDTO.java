package cl.feriando.producto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequestDTO(

        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(max = 80)
        String nombre
) { }
