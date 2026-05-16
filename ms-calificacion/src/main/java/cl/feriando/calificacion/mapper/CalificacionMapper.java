package cl.feriando.calificacion.mapper;

import cl.feriando.calificacion.dto.CalificacionRequestDTO;
import cl.feriando.calificacion.dto.CalificacionResponseDTO;
import cl.feriando.calificacion.model.Calificacion;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CalificacionMapper {

    public Calificacion toEntity(CalificacionRequestDTO dto) {
        Calificacion c = new Calificacion();
        c.setIdPedido(dto.idPedido());
        c.setIdCliente(dto.idCliente());
        c.setIdFeriante(dto.idFeriante());
        c.setPuntaje(dto.puntaje());
        c.setComentario(dto.comentario());
        c.setFecha(LocalDateTime.now());
        return c;
    }

    public CalificacionResponseDTO toResponse(Calificacion c) {
        return new CalificacionResponseDTO(
                c.getIdCalificacion(),
                c.getIdPedido(),
                c.getIdCliente(),
                c.getIdFeriante(),
                c.getPuntaje(),
                c.getComentario(),
                c.getFecha()
        );
    }
}
