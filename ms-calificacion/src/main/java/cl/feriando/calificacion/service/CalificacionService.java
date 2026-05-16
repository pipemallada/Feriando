package cl.feriando.calificacion.service;

import cl.feriando.calificacion.client.FerianteClient;
import cl.feriando.calificacion.dto.CalificacionRequestDTO;
import cl.feriando.calificacion.dto.CalificacionResponseDTO;
import cl.feriando.calificacion.exception.BusinessException;
import cl.feriando.calificacion.exception.ResourceNotFoundException;
import cl.feriando.calificacion.mapper.CalificacionMapper;
import cl.feriando.calificacion.model.Calificacion;
import cl.feriando.calificacion.repository.CalificacionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CalificacionService {

    private static final Logger log = LoggerFactory.getLogger(CalificacionService.class);

    private final CalificacionRepository repository;
    private final CalificacionMapper mapper;
    private final FerianteClient ferianteClient;

    public CalificacionService(CalificacionRepository repository,
                               CalificacionMapper mapper,
                               FerianteClient ferianteClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.ferianteClient = ferianteClient;
    }

    @Transactional(readOnly = true)
    public List<CalificacionResponseDTO> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CalificacionResponseDTO> listarPorFeriante(Long idFeriante) {
        return repository.findByIdFeriante(idFeriante).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CalificacionResponseDTO obtener(Long id) {
        return mapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public CalificacionResponseDTO crear(CalificacionRequestDTO dto) {
        if (repository.existsByIdPedido(dto.idPedido())) {
            throw new BusinessException("El pedido " + dto.idPedido() + " ya tiene una calificación");
        }
        Calificacion c = repository.save(mapper.toEntity(dto));
        log.info("Calificación creada id={}, feriante={}, puntaje={}",
                c.getIdCalificacion(), dto.idFeriante(), dto.puntaje());

        BigDecimal nuevoPromedio = calcularPromedio(dto.idFeriante());
        ferianteClient.actualizarPromedio(dto.idFeriante(), nuevoPromedio);

        return mapper.toResponse(c);
    }

    @Transactional
    public void eliminar(Long id) {
        Calificacion c = buscarPorId(id);
        Long idFeriante = c.getIdFeriante();
        repository.delete(c);
        log.info("Calificación eliminada id={}", id);

        BigDecimal nuevoPromedio = calcularPromedio(idFeriante);
        ferianteClient.actualizarPromedio(idFeriante, nuevoPromedio);
    }

    private BigDecimal calcularPromedio(Long idFeriante) {
        List<Calificacion> califs = repository.findByIdFeriante(idFeriante);
        if (califs.isEmpty()) {
            return BigDecimal.ZERO;
        }
        double promedio = califs.stream()
                .mapToInt(Calificacion::getPuntaje)
                .average()
                .orElse(0.0);
        return BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP);
    }

    private Calificacion buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Calificación " + id + " no encontrada"));
    }
}
