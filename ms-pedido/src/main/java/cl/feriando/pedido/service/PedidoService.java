package cl.feriando.pedido.service;

import cl.feriando.pedido.dto.CambioEstadoDTO;
import cl.feriando.pedido.dto.PedidoRequestDTO;
import cl.feriando.pedido.dto.PedidoResponseDTO;
import cl.feriando.pedido.exception.ResourceNotFoundException;
import cl.feriando.pedido.mapper.PedidoMapper;
import cl.feriando.pedido.model.Pedido;
import cl.feriando.pedido.repository.PedidoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository repository;
    private final PedidoMapper mapper;

    public PedidoService(PedidoRepository repository, PedidoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorCliente(Long idCliente) {
        return repository.findByIdCliente(idCliente).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorFeriante(Long idFeriante) {
        return repository.findByIdFeriante(idFeriante).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO obtener(Long id) {
        return mapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        Pedido p = repository.save(mapper.toEntity(dto));
        log.info("Pedido creado id={}, cliente={}, feriante={}, total={}",
                p.getIdPedido(), p.getIdCliente(), p.getIdFeriante(), p.getTotal());
        return mapper.toResponse(p);
    }

    @Transactional
    public PedidoResponseDTO cambiarEstado(Long id, CambioEstadoDTO dto) {
        Pedido p = buscarPorId(id);
        String anterior = p.getEstado();
        p.setEstado(dto.estado());
        log.info("Pedido id={} cambió de estado {} -> {}", id, anterior, dto.estado());
        return mapper.toResponse(repository.save(p));
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarPorId(id));
        log.info("Pedido eliminado id={}", id);
    }

    private Pedido buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido " + id + " no encontrado"));
    }
}
