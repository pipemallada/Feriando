package cl.feriando.feriante.service;

import cl.feriando.feriante.client.UsuarioClient;
import cl.feriando.feriante.dto.FerianteRequestDTO;
import cl.feriando.feriante.dto.FerianteResponseDTO;
import cl.feriando.feriante.exception.BusinessException;
import cl.feriando.feriante.exception.ResourceNotFoundException;
import cl.feriando.feriante.mapper.FerianteMapper;
import cl.feriando.feriante.model.Feriante;
import cl.feriando.feriante.repository.FerianteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class FerianteService {

    private static final Logger log = LoggerFactory.getLogger(FerianteService.class);

    private final FerianteRepository repository;
    private final FerianteMapper mapper;
    private final UsuarioClient usuarioClient;

    public FerianteService(FerianteRepository repository,
                           FerianteMapper mapper,
                           UsuarioClient usuarioClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.usuarioClient = usuarioClient;
    }

    @Transactional(readOnly = true)
    public List<FerianteResponseDTO> listar() {
        log.info("Listando todos los feriantes");
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FerianteResponseDTO obtener(Long id) {
        return mapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public FerianteResponseDTO crear(FerianteRequestDTO dto) {
        if (!usuarioClient.existeUsuario(dto.idUsuario())) {
            throw new BusinessException("El usuario " + dto.idUsuario() + " no existe");
        }
        if (repository.findByIdUsuario(dto.idUsuario()).isPresent()) {
            throw new BusinessException("El usuario " + dto.idUsuario() + " ya tiene un feriante asociado");
        }
        Feriante guardado = repository.save(mapper.toEntity(dto));
        log.info("Feriante creado id={}", guardado.getIdFeriante());
        return mapper.toResponse(guardado);
    }

    @Transactional
    public FerianteResponseDTO actualizar(Long id, FerianteRequestDTO dto) {
        Feriante f = buscarPorId(id);
        mapper.updateEntity(f, dto);
        log.info("Feriante actualizado id={}", id);
        return mapper.toResponse(repository.save(f));
    }

    @Transactional
    public void eliminar(Long id) {
        Feriante f = buscarPorId(id);
        repository.delete(f);
        log.info("Feriante eliminado id={}", id);
    }

    @Transactional
    public FerianteResponseDTO actualizarCalificacionPromedio(Long idFeriante, BigDecimal nuevoPromedio) {
        Feriante f = buscarPorId(idFeriante);
        f.setCalificacionProm(nuevoPromedio.setScale(2, RoundingMode.HALF_UP));
        log.info("Promedio actualizado para feriante id={}, nuevo={}", idFeriante, nuevoPromedio);
        return mapper.toResponse(repository.save(f));
    }

    private Feriante buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feriante " + id + " no encontrado"));
    }
}
