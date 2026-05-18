package cl.feriando.feriante.mapper;

import cl.feriando.feriante.dto.FerianteRequestDTO;
import cl.feriando.feriante.dto.FerianteResponseDTO;
import cl.feriando.feriante.model.Feriante;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
/**
 * mapper entre Feriante (entidad) y sus DTOs.
 * misma idea que UsuarioMapper: aisla la traduccion Entity<->DTO en un unico
 * componente, evitando que ese codigo se repita en el service o controller.
 */
@Component
public class FerianteMapper {
    // al crear un feriante nuevo, la calificacion arranca en 0.00 y el
    // perfil queda activo. estos valores no los manda el cliente porque
    // son administrados internamente por el sistema.
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
    // entidad -> DTO de respuesta. mantiene el contrato de salida.
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
    // en update no tocamos id_usuario ni la calificacion: el dueño del puesto
    // no deberia cambiar a otro usuario, y la calificacion se actualiza por
    // el flujo de ms-calificacion.
    public void updateEntity(Feriante f, FerianteRequestDTO dto) {
        f.setNombrePuesto(dto.nombrePuesto());
        f.setDescripcion(dto.descripcion());
        f.setTelefono(dto.telefono());
    }
}
