package cl.feriando.carrito.mapper;

import cl.feriando.carrito.dto.CarritoRequestDTO;
import cl.feriando.carrito.dto.CarritoResponseDTO;
import cl.feriando.carrito.dto.DetalleCarritoResponseDTO;
import cl.feriando.carrito.model.Carrito;
import cl.feriando.carrito.model.DetalleCarrito;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class CarritoMapper {

    public Carrito toEntity(CarritoRequestDTO dto) {
        Carrito c = new Carrito();
        c.setIdCliente(dto.idCliente());
        c.setEstado("ACTIVO");
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }

    public CarritoResponseDTO toResponse(Carrito c) {
        List<DetalleCarritoResponseDTO> detalles = c.getDetalles().stream()
                .map(this::toDetalleResponse)
                .toList();
        BigDecimal total = detalles.stream()
                .map(DetalleCarritoResponseDTO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CarritoResponseDTO(
                c.getIdCarrito(),
                c.getIdCliente(),
                c.getEstado(),
                c.getCreatedAt(),
                detalles,
                total
        );
    }

    public DetalleCarritoResponseDTO toDetalleResponse(DetalleCarrito d) {
        BigDecimal subtotal = d.getCantidad().multiply(d.getPrecioUnitario());
        return new DetalleCarritoResponseDTO(
                d.getIdDetalle(),
                d.getIdProducto(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                subtotal
        );
    }
}
