package cl.feriando.carrito.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
/**
 * DTO para deserializar la respuesta de ms-producto cuando consultamos un
 * producto. solo nos interesan id y precio; el resto lo ignoramos.
 * el JSON que devuelve ms-producto tiene muchos mas campos (nombre,
 *   descripcion, categoria, etc.)
 * nos permite evolucionar ms-producto sin romper ms-carrito.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductoBasicoDTO(
        Long idProducto,
        BigDecimal precio
) { }
