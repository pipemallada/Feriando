package cl.feriando.carrito.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
/**
 * DTO para agregar un item al carrito.
 *  el precio se consulta en tiempo real a ms-producto (vía WebClient).
 *   confiar en el precio que mande el cliente seria una vulnerabilidad
 *   (el cliente podria enviar precio=0 y comprar gratis).
 */
public record DetalleCarritoRequestDTO(

        @NotNull(message = "El id_producto es obligatorio")
        @Positive
        Long idProducto,
        // inclusive=false rechaza cantidad=0: no tiene sentido agregar
        // 0 unidades al carrito.
        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor a 0")
        BigDecimal cantidad
) { }
