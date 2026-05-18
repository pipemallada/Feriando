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
/**
 * mapper de carrito.
 * aca viven dos calculos importantes:
 * subtotal por línea (cantidad × precioUnitario) y total del carrito (suma de subtotales)
 * lo hacemos en el mapper para que el service quede limpio y para que NO
 * existan campos derivados persistidos en la BD (que podrian
 * desincronizarse de los detalles reales).
 */
@Component
public class CarritoMapper {
    // carrito nuevo arranca activo y con timestamp = ahora.
    // los detalles se agregan despues con agregarItem() del service.
    public Carrito toEntity(CarritoRequestDTO dto) {
        Carrito c = new Carrito();
        c.setIdCliente(dto.idCliente());
        c.setEstado("ACTIVO");
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }
    // convierte la entidad a DTO completo, incluyendo detalles y total.
    public CarritoResponseDTO toResponse(Carrito c) {
        // cada detalle pasa por toDetalleResponse (calcula su subtotal).
        List<DetalleCarritoResponseDTO> detalles = c.getDetalles().stream()
                .map(this::toDetalleResponse)
                .toList();
        // total = suma de subtotales. reduce con identidad BigDecimal.ZERO
        // garantiza un valor incluso si el carrito esta vacio.
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
    // calcula el subtotal de un detalle al vuelo y arma su DTO de salida.
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
