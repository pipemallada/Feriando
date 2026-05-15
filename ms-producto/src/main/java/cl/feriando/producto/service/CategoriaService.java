package cl.feriando.producto.service;

import cl.feriando.producto.dto.CategoriaRequestDTO;
import cl.feriando.producto.dto.CategoriaResponseDTO;
import cl.feriando.producto.exception.BusinessException;
import cl.feriando.producto.exception.ResourceNotFoundException;
import cl.feriando.producto.mapper.CategoriaMapper;
import cl.feriando.producto.model.Categoria;
import cl.feriando.producto.repository.CategoriaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaService.class);

    private final CategoriaRepository repository;
    private final CategoriaMapper mapper;

    public CategoriaService(CategoriaRepository repository, CategoriaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtener(Long id) {
        return mapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        if (repository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new BusinessException("Ya existe una categoría con el nombre " + dto.nombre());
        }
        Categoria c = repository.save(mapper.toEntity(dto));
        log.info("Categoría creada id={}", c.getIdCategoria());
        return mapper.toResponse(c);
    }

    @Transactional
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto) {
        Categoria c = buscarPorId(id);
        mapper.updateEntity(c, dto);
        return mapper.toResponse(repository.save(c));
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarPorId(id));
        log.info("Categoría eliminada id={}", id);
    }

    public Categoria buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría " + id + " no encontrada"));
    }
}
