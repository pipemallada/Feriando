package cl.feriando.pago.mapper;

import cl.feriando.pago.dto.PagoRequestDTO;
import cl.feriando.pago.dto.PagoResponseDTO;
import cl.feriando.pago.model.Pago;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PagoMapper {

    public Pago toEntity(PagoRequestDTO dto) {
        Pago p = new Pago();
        p.setIdPedido(dto.idPedido());
        p.setIdCliente(dto.idCliente());
        p.setMonto(dto.monto());
        p.setMetodoPago(dto.metodoPago());
        p.setEstadoPago("PENDIENTE");
        p.setFechaPago(LocalDateTime.now());
        p.setCodigoTransaccion(dto.codigoTransaccion());
        return p;
    }

    public PagoResponseDTO toResponse(Pago p) {
        return new PagoResponseDTO(
                p.getIdPago(),
                p.getIdPedido(),
                p.getIdCliente(),
                p.getMonto(),
                p.getMetodoPago(),
                p.getEstadoPago(),
                p.getFechaPago(),
                p.getCodigoTransaccion()
        );
    }
}
