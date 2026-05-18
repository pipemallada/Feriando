package cl.feriando.carrito.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
/**
 * DTO para crear un carrito (solo necesita el id del cliente).
 * los detalles del carrito se agregan despues por separado (POST /items).
 */
public record CarritoRequestDTO(
// el cliente dueño del carrito. id_cliente debe existir en ms-usuario.
        @NotNull(message = "El id_cliente es obligatorio")
        @Positive
        Long idCliente
) { }
