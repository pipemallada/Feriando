package cl.feriando.feriante.mapper;

import cl.feriando.feriante.dto.FerianteRequestDTO;
import cl.feriando.feriante.dto.FerianteResponseDTO;
import cl.feriando.feriante.model.Feriante;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FerianteMapper {

    public Feriante toEntity(FerianteRequestDTO dto) {
        Feriante f = new Feriante();
        f.setIdUsuario(dto.idUsuario());
        f.setNombrePuesto(dto.nombrePuesto());
        f.setDescripcion(dto.descripcion());
        f.setTelefono(dto.telefono());
        f.setCalificacionProm(BigDecimal.ZERO);
        f.setActivo((short) 1);
        return f;
    }

    public FerianteResponseDTO toResponse(Feriante f) {
        return new FerianteResponseDTO(
                f.getIdFeriante(),
                f.getIdUsuario(),
                f.getNombrePuesto(),
                f.getDescripcion(),
                f.getTelefono(),
                f.getCalificacionProm(),
                f.getActivo()
        );
    }

    public void updateEntity(Feriante f, FerianteRequestDTO dto) {
        f.setNombrePuesto(dto.nombrePuesto());
        f.setDescripcion(dto.descripcion());
        f.setTelefono(dto.telefono());
    }
}
