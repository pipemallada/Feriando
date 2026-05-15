package cl.feriando.usuario.service;

import cl.feriando.usuario.dto.UsuarioRequestDTO;
import cl.feriando.usuario.dto.UsuarioResponseDTO;
import cl.feriando.usuario.exception.BusinessException;
import cl.feriando.usuario.exception.ResourceNotFoundException;
import cl.feriando.usuario.mapper.UsuarioMapper;
import cl.feriando.usuario.model.Usuario;
import cl.feriando.usuario.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;

    public UsuarioService(UsuarioRepository repository, UsuarioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        log.info("Listando todos los usuarios");
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtener(Long id) {
        Usuario u = buscarPorId(id);
        return mapper.toResponse(u);
    }

    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new BusinessException("Ya existe un usuario con el email " + dto.email());
        }
        Usuario nuevo = mapper.toEntity(dto);
        Usuario guardado = repository.save(nuevo);
        log.info("Usuario creado con id={}", guardado.getIdUsuario());
        return mapper.toResponse(guardado);
    }

    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO dto) {
        Usuario u = buscarPorId(id);
        if (!u.getEmail().equalsIgnoreCase(dto.email()) && repository.existsByEmail(dto.email())) {
            throw new BusinessException("El email " + dto.email() + " ya está en uso");
        }
        mapper.updateEntity(u, dto);
        Usuario actualizado = repository.save(u);
        log.info("Usuario actualizado id={}", id);
        return mapper.toResponse(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario u = buscarPorId(id);
        repository.delete(u);
        log.info("Usuario eliminado id={}", id);
    }

    private Usuario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario " + id + " no encontrado"));
    }
}
