package cl.feriando.pago.service;

import cl.feriando.pago.client.InventarioClient;
import cl.feriando.pago.client.PedidoClient;
import cl.feriando.pago.dto.PagoRequestDTO;
import cl.feriando.pago.dto.PagoResponseDTO;
import cl.feriando.pago.dto.PedidoDTO;
import cl.feriando.pago.exception.BusinessException;
import cl.feriando.pago.exception.ResourceNotFoundException;
import cl.feriando.pago.mapper.PagoMapper;
import cl.feriando.pago.model.Pago;
import cl.feriando.pago.repository.PagoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final PagoRepository repository;
    private final PagoMapper mapper;
    private final PedidoClient pedidoClient;
    private final InventarioClient inventarioClient;

    public PagoService(PagoRepository repository,
                       PagoMapper mapper,
                       PedidoClient pedidoClient,
                       InventarioClient inventarioClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.pedidoClient = pedidoClient;
        this.inventarioClient = inventarioClient;
    }

    @Transactional(readOnly = true)
    public List<PagoResponseDTO> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PagoResponseDTO obtener(Long id) {
        return mapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public PagoResponseDTO crear(PagoRequestDTO dto) {
        if (repository.existsByIdPedido(dto.idPedido())) {
            throw new BusinessException("El pedido " + dto.idPedido() + " ya tiene un pago registrado");
        }
        Pago p = repository.save(mapper.toEntity(dto));
        log.info("Pago creado id={}, pedido={}, monto={}", p.getIdPago(), dto.idPedido(), dto.monto());
        return mapper.toResponse(p);
    }

    @Transactional
    public PagoResponseDTO confirmar(Long idPago) {
        Pago pago = buscarPorId(idPago);
        if ("CONFIRMADO".equalsIgnoreCase(pago.getEstadoPago())) {
            throw new BusinessException("El pago " + idPago + " ya está confirmado");
        }

        PedidoDTO pedido = pedidoClient.buscarPedido(pago.getIdPedido())
                .orElseThrow(() -> new BusinessException(
                        "No se pudo obtener el pedido " + pago.getIdPedido() + " desde ms-pedido"));

        pago.setEstadoPago("CONFIRMADO");
        Pago confirmado = repository.save(pago);
        log.info("Pago confirmado id={}, pedido={}", idPago, pago.getIdPedido());

        pedidoClient.marcarComoPagado(pago.getIdPedido());

        if (pedido.detalles() != null) {
            pedido.detalles().forEach(d ->
                    inventarioClient.descontarStock(d.idProducto(), d.cantidad()));
        }

        return mapper.toResponse(confirmado);
    }

    @Transactional
    public PagoResponseDTO rechazar(Long idPago) {
        Pago pago = buscarPorId(idPago);
        pago.setEstadoPago("RECHAZADO");
        log.info("Pago rechazado id={}", idPago);
        return mapper.toResponse(repository.save(pago));
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarPorId(id));
        log.info("Pago eliminado id={}", id);
    }

    private Pago buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago " + id + " no encontrado"));
    }
}
