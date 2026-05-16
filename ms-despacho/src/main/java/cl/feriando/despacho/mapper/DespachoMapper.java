package cl.feriando.despacho.mapper;

import cl.feriando.despacho.dto.DespachoRequestDTO;
import cl.feriando.despacho.dto.DespachoResponseDTO;
import cl.feriando.despacho.model.Despacho;

import org.springframework.stereotype.Component;

@Component
public class DespachoMapper {

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

    public void updateEntity(Despacho d, DespachoRequestDTO dto) {
        d.setTipoEntrega(dto.tipoEntrega());
        d.setDireccion(dto.direccion());
        d.setComuna(dto.comuna());
        d.setFechaEstimada(dto.fechaEstimada());
    }
}
