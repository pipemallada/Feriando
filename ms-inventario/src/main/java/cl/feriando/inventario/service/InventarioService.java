package cl.feriando.inventario.service;

import cl.feriando.inventario.dto.AjusteStockDTO;
import cl.feriando.inventario.dto.InventarioRequestDTO;
import cl.feriando.inventario.dto.InventarioResponseDTO;
import cl.feriando.inventario.exception.BusinessException;
import cl.feriando.inventario.exception.ResourceNotFoundException;
import cl.feriando.inventario.mapper.InventarioMapper;
import cl.feriando.inventario.model.Inventario;
import cl.feriando.inventario.repository.InventarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventarioService {

    private static final Logger log = LoggerFactory.getLogger(InventarioService.class);

    private final InventarioRepository repository;
    private final InventarioMapper mapper;

    public InventarioService(InventarioRepository repository, InventarioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<InventarioResponseDTO> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InventarioResponseDTO obtener(Long id) {
        return mapper.toResponse(buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public InventarioResponseDTO obtenerPorProducto(Long idProducto) {
        Inventario i = repository.findByIdProducto(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventario del producto " + idProducto + " no encontrado"));
        return mapper.toResponse(i);
    }

    @Transactional
    public InventarioResponseDTO crear(InventarioRequestDTO dto) {
        if (repository.existsByIdProducto(dto.idProducto())) {
            throw new BusinessException("El producto " + dto.idProducto() + " ya tiene inventario");
        }
        Inventario guardado = repository.save(mapper.toEntity(dto));
        log.info("Inventario creado id={} para producto={}", guardado.getIdInventario(), dto.idProducto());
        return mapper.toResponse(guardado);
    }

    @Transactional
    public InventarioResponseDTO actualizar(Long id, InventarioRequestDTO dto) {
        Inventario i = buscarPorId(id);
        mapper.updateEntity(i, dto);
        log.info("Inventario actualizado id={}", id);
        return mapper.toResponse(repository.save(i));
    }

    @Transactional
    public InventarioResponseDTO descontarStock(Long idProducto, AjusteStockDTO dto) {
        Inventario i = repository.findByIdProducto(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventario del producto " + idProducto + " no encontrado"));

        if (i.getStockDisponible().compareTo(dto.cantidad()) < 0) {
            throw new BusinessException(
                    "Stock insuficiente. Disponible: " + i.getStockDisponible() + ", solicitado: " + dto.cantidad());
        }

        BigDecimal nuevoStock = i.getStockDisponible().subtract(dto.cantidad());
        i.setStockDisponible(nuevoStock);
        i.setAlertaActiva(nuevoStock.compareTo(i.getStockMinimo()) <= 0 ? (short) 1 : (short) 0);
        i.setUltimaActualizacion(LocalDateTime.now());

        log.info("Stock descontado producto={}, cantidad={}, restante={}", idProducto, dto.cantidad(), nuevoStock);
        return mapper.toResponse(repository.save(i));
    }

    @Transactional
    public InventarioResponseDTO reponerStock(Long idProducto, AjusteStockDTO dto) {
        Inventario i = repository.findByIdProducto(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventario del producto " + idProducto + " no encontrado"));

        BigDecimal nuevoStock = i.getStockDisponible().add(dto.cantidad());
        i.setStockDisponible(nuevoStock);
        i.setAlertaActiva(nuevoStock.compareTo(i.getStockMinimo()) <= 0 ? (short) 1 : (short) 0);
        i.setUltimaActualizacion(LocalDateTime.now());

        log.info("Stock repuesto producto={}, cantidad={}, total={}", idProducto, dto.cantidad(), nuevoStock);
        return mapper.toResponse(repository.save(i));
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarPorId(id));
        log.info("Inventario eliminado id={}", id);
    }

    private Inventario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario " + id + " no encontrado"));
    }
}
