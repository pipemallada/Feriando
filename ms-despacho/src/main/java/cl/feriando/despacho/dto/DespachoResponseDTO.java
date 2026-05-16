package cl.feriando.despacho.dto;

import java.time.LocalDateTime;

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
