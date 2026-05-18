package cl.feriando.despacho.mapper;

import cl.feriando.despacho.dto.DespachoRequestDTO;
import cl.feriando.despacho.dto.DespachoResponseDTO;
import cl.feriando.despacho.model.Despacho;

import org.springframework.stereotype.Component;
/**
 * mapper de despacho. sin calculos derivados (a diferencia del de carrito).
 */
@Component
public class DespachoMapper {
    // Nuevo despacho: estado inicial = PENDIENTE, fechaEntrega queda null.
    public Despacho toEntity(DespachoRequestDTO dto) {
        Despacho d = new Despacho();
        d.setIdPedido(dto.idPedido());
        d.setTipoEntrega(dto.tipoEntrega());
        d.setDireccion(dto.direccion());
        d.setComuna(dto.comuna());
        d.setEstadoDespacho("PENDIENTE");
        d.setFechaEstimada(dto.fechaEstimada());
        return d;
    }
    // Entidad -> DTO.
    public DespachoResponseDTO toResponse(Despacho d) {
        return new DespachoResponseDTO(
                d.getIdDespacho(),
                d.getIdPedido(),
                d.getTipoEntrega(),
                d.getDireccion(),
                d.getComuna(),
                d.getEstadoDespacho(),
                d.getFechaEstimada(),
                d.getFechaEntrega()
        );
    }
    // Update: NO tocamos id_pedido (1:1 inmutable) ni los campos de estado
    // y fecha_entrega (que tienen su propio endpoint de cambio de estado).
    public void updateEntity(Despacho d, DespachoRequestDTO dto) {
        d.setTipoEntrega(dto.tipoEntrega());
        d.setDireccion(dto.direccion());
        d.setComuna(dto.comuna());
        d.setFechaEstimada(dto.fechaEstimada());
    }
}
