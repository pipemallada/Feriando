package cl.feriando.carrito.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductoBasicoDTO(
        Long idProducto,
        BigDecimal precio
) { }
