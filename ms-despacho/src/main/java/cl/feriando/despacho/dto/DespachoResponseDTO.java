package cl.feriando.despacho.dto;

import java.time.LocalDateTime;
/**
 * DTO de salida del despacho. incluye el estado y la fecha real de entrega
 * (que en el request son administrados internamente).
 */
public record DespachoResponseDTO(
        Long idDespacho,
        Long idPedido,
        String tipoEntrega,
        String direccion,
        String comuna,
        String estadoDespacho,
        LocalDateTime fechaEstimada,
        LocalDateTime fechaEntrega
) { }
