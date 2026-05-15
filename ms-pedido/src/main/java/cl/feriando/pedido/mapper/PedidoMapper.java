package cl.feriando.pedido.mapper;

import cl.feriando.pedido.dto.DetallePedidoRequestDTO;
import cl.feriando.pedido.dto.DetallePedidoResponseDTO;
import cl.feriando.pedido.dto.PedidoRequestDTO;
import cl.feriando.pedido.dto.PedidoResponseDTO;
import cl.feriando.pedido.model.DetallePedido;
import cl.feriando.pedido.model.Pedido;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class PedidoMapper {

    public Pedido toEntity(PedidoRequestDTO dto) {
        Pedido p = new Pedido();
        p.setIdCliente(dto.idCliente());
        p.setIdFeriante(dto.idFeriante());
        p.setEstado("PENDIENTE");
        p.setFechaPedido(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedidoRequestDTO d : dto.detalles()) {
            DetallePedido det = new DetallePedido();
            det.setPedido(p);
            det.setIdProducto(d.idProducto());
            det.setCantidad(d.cantidad());
            det.setPrecioUnitario(d.precioUnitario());
            BigDecimal subtotal = d.cantidad().multiply(d.precioUnitario());
            det.setSubtotal(subtotal);
            total = total.add(subtotal);
            p.getDetalles().add(det);
        }
        p.setTotal(total);
        return p;
    }

    public PedidoResponseDTO toResponse(Pedido p) {
        List<DetallePedidoResponseDTO> detalles = p.getDetalles().stream()
                .map(this::toDetalleResponse)
                .toList();
        return new PedidoResponseDTO(
                p.getIdPedido(),
                p.getIdCliente(),
                p.getIdFeriante(),
                p.getEstado(),
                p.getTotal(),
                p.getFechaPedido(),
                detalles
        );
    }

    public DetallePedidoResponseDTO toDetalleResponse(DetallePedido d) {
        return new DetallePedidoResponseDTO(
                d.getIdDetalle(),
                d.getIdProducto(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getSubtotal()
        );
    }
}
