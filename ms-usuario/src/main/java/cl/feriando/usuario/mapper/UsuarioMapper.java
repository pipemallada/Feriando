package cl.feriando.usuario.mapper;

import cl.feriando.usuario.dto.UsuarioRequestDTO;
import cl.feriando.usuario.dto.UsuarioResponseDTO;
import cl.feriando.usuario.model.Usuario;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UsuarioMapper {

    public Usuario toEntity(UsuarioRequestDTO dto) {
        Usuario u = new Usuario();
        u.setNombre(dto.nombre());
        u.setApellido(dto.apellido());
        u.setEmail(dto.email());
        u.setPasswordHash(dto.passwordHash());
        u.setRol(dto.rol());
        u.setActivo((short) 1);
        u.setCreatedAt(LocalDateTime.now());
        return u;
    }

    public UsuarioResponseDTO toResponse(Usuario u) {
        return new UsuarioResponseDTO(
                u.getIdUsuario(),
                u.getNombre(),
                u.getApellido(),
                u.getEmail(),
                u.getRol(),
                u.getActivo(),
                u.getCreatedAt()
        );
    }

    public void updateEntity(Usuario u, UsuarioRequestDTO dto) {
        u.setNombre(dto.nombre());
        u.setApellido(dto.apellido());
        u.setEmail(dto.email());
        u.setPasswordHash(dto.passwordHash());
        u.setRol(dto.rol());
    }
}
