package cl.feriando.reporte.service;

import cl.feriando.reporte.client.PedidoClient;
import cl.feriando.reporte.dto.PedidoResumenDTO;
import cl.feriando.reporte.dto.ReporteVentasRequestDTO;
import cl.feriando.reporte.dto.ReporteVentasResponseDTO;
import cl.feriando.reporte.exception.ResourceNotFoundException;
import cl.feriando.reporte.mapper.ReporteVentasMapper;
import cl.feriando.reporte.model.ReporteVentas;
import cl.feriando.reporte.repository.ReporteVentasRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    private static final Logger log = LoggerFactory.getLogger(ReporteService.class);
    private static final DateTimeFormatter PERIODO_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ReporteVentasRepository repository;
    private final ReporteVentasMapper mapper;
    private final PedidoClient pedidoClient;

    public ReporteService(ReporteVentasRepository repository,
                          ReporteVentasMapper mapper,
                          PedidoClient pedidoClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.pedidoClient = pedidoClient;
    }

    @Transactional(readOnly = true)
    public List<ReporteVentasResponseDTO> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteVentasResponseDTO> listarPorFeriante(Long idFeriante) {
        return repository.findByIdFeriante(idFeriante).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReporteVentasResponseDTO obtener(Long id) {
        return mapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public ReporteVentasResponseDTO crear(ReporteVentasRequestDTO dto) {
        ReporteVentas r = repository.save(mapper.toEntity(dto));
        log.info("Reporte creado manualmente id={}, feriante={}", r.getIdReporte(), dto.idFeriante());
        return mapper.toResponse(r);
    }

    @Transactional
    public ReporteVentasResponseDTO generarParaFeriante(Long idFeriante) {
        List<PedidoResumenDTO> pedidos = pedidoClient.listarPorFeriante(idFeriante);

        BigDecimal totalVentas = pedidos.stream()
                .filter(p -> "PAGADO".equalsIgnoreCase(p.estado())
                        || "ENTREGADO".equalsIgnoreCase(p.estado())
                        || "LISTO".equalsIgnoreCase(p.estado())
                        || "PREPARANDO".equalsIgnoreCase(p.estado()))
                .map(PedidoResumenDTO::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalPedidos = pedidos.size();
        String productoTop = calcularProductoTop(pedidos);

        ReporteVentas r = new ReporteVentas();
        r.setIdFeriante(idFeriante);
        r.setPeriodo(LocalDateTime.now().format(PERIODO_FMT));
        r.setTotalVentas(totalVentas);
        r.setTotalPedidos(totalPedidos);
        r.setProductoTop(productoTop);
        r.setGeneradoEn(LocalDateTime.now());

        ReporteVentas guardado = repository.save(r);
        log.info("Reporte generado para feriante={}, ventas={}, pedidos={}",
                idFeriante, totalVentas, totalPedidos);
        return mapper.toResponse(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarPorId(id));
        log.info("Reporte eliminado id={}", id);
    }

    private String calcularProductoTop(List<PedidoResumenDTO> pedidos) {
        Map<Long, BigDecimal> totalesPorProducto = new HashMap<>();
        for (PedidoResumenDTO p : pedidos) {
            if (p.detalles() == null) continue;
            for (PedidoResumenDTO.DetalleResumenDTO d : p.detalles()) {
                totalesPorProducto.merge(d.idProducto(), d.cantidad(), BigDecimal::add);
            }
        }
        return totalesPorProducto.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> "Producto " + e.getKey())
                .orElse(null);
    }

    private ReporteVentas buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte " + id + " no encontrado"));
    }
}
