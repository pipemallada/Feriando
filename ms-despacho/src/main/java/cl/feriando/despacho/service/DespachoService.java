package cl.feriando.despacho.service;

import cl.feriando.despacho.dto.CambioEstadoDespachoDTO;
import cl.feriando.despacho.dto.DespachoRequestDTO;
import cl.feriando.despacho.dto.DespachoResponseDTO;
import cl.feriando.despacho.exception.BusinessException;
import cl.feriando.despacho.exception.ResourceNotFoundException;
import cl.feriando.despacho.mapper.DespachoMapper;
import cl.feriando.despacho.model.Despacho;
import cl.feriando.despacho.repository.DespachoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DespachoService {

    private static final Logger log = LoggerFactory.getLogger(DespachoService.class);

    private final DespachoRepository repository;
    private final DespachoMapper mapper;

    public DespachoService(DespachoRepository repository, DespachoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<DespachoResponseDTO> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DespachoResponseDTO obtener(Long id) {
        return mapper.toResponse(buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public DespachoResponseDTO obtenerPorPedido(Long idPedido) {
        Despacho d = repository.findByIdPedido(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Despacho del pedido " + idPedido + " no encontrado"));
        return mapper.toResponse(d);
    }

    @Transactional
    public DespachoResponseDTO crear(DespachoRequestDTO dto) {
        if (repository.existsByIdPedido(dto.idPedido())) {
            throw new BusinessException("El pedido " + dto.idPedido() + " ya tiene un despacho asignado");
        }
        if ("DOMICILIO".equalsIgnoreCase(dto.tipoEntrega())
                && (dto.direccion() == null || dto.direccion().isBlank())) {
            throw new BusinessException("La dirección es obligatoria para entrega a domicilio");
        }
        Despacho d = repository.save(mapper.toEntity(dto));
        log.info("Despacho creado id={}, pedido={}, tipo={}", d.getIdDespacho(), dto.idPedido(), dto.tipoEntrega());
        return mapper.toResponse(d);
    }

    @Transactional
    public DespachoResponseDTO actualizar(Long id, DespachoRequestDTO dto) {
        Despacho d = buscarPorId(id);
        mapper.updateEntity(d, dto);
        log.info("Despacho actualizado id={}", id);
        return mapper.toResponse(repository.save(d));
    }

    @Transactional
    public DespachoResponseDTO cambiarEstado(Long id, CambioEstadoDespachoDTO dto) {
        Despacho d = buscarPorId(id);
        d.setEstadoDespacho(dto.estadoDespacho());
        if ("ENTREGADO".equalsIgnoreCase(dto.estadoDespacho())) {
            d.setFechaEntrega(LocalDateTime.now());
        }
        log.info("Despacho id={} cambió de estado a {}", id, dto.estadoDespacho());
        return mapper.toResponse(repository.save(d));
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarPorId(id));
        log.info("Despacho eliminado id={}", id);
    }

    private Despacho buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despacho " + id + " no encontrado"));
    }
}
