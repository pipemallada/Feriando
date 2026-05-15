package cl.feriando.carrito.service;

import cl.feriando.carrito.client.ProductoClient;
import cl.feriando.carrito.dto.CarritoRequestDTO;
import cl.feriando.carrito.dto.CarritoResponseDTO;
import cl.feriando.carrito.dto.DetalleCarritoRequestDTO;
import cl.feriando.carrito.dto.ProductoBasicoDTO;
import cl.feriando.carrito.exception.BusinessException;
import cl.feriando.carrito.exception.ResourceNotFoundException;
import cl.feriando.carrito.mapper.CarritoMapper;
import cl.feriando.carrito.model.Carrito;
import cl.feriando.carrito.model.DetalleCarrito;
import cl.feriando.carrito.repository.CarritoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CarritoService {

    private static final Logger log = LoggerFactory.getLogger(CarritoService.class);

    private final CarritoRepository repository;
    private final CarritoMapper mapper;
    private final ProductoClient productoClient;

    public CarritoService(CarritoRepository repository,
                          CarritoMapper mapper,
                          ProductoClient productoClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.productoClient = productoClient;
    }

    @Transactional(readOnly = true)
    public List<CarritoResponseDTO> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CarritoResponseDTO obtener(Long id) {
        return mapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public CarritoResponseDTO crear(CarritoRequestDTO dto) {
        Carrito c = repository.save(mapper.toEntity(dto));
        log.info("Carrito creado id={}, cliente={}", c.getIdCarrito(), dto.idCliente());
        return mapper.toResponse(c);
    }

    @Transactional
    public CarritoResponseDTO agregarItem(Long idCarrito, DetalleCarritoRequestDTO dto) {
        Carrito c = buscarPorId(idCarrito);
        if (!"ACTIVO".equalsIgnoreCase(c.getEstado())) {
            throw new BusinessException("El carrito " + idCarrito + " no está activo");
        }

        ProductoBasicoDTO producto = productoClient.buscarProducto(dto.idProducto())
                .orElseThrow(() -> new BusinessException(
                        "El producto " + dto.idProducto() + " no existe o ms-producto no responde"));

        DetalleCarrito d = new DetalleCarrito();
        d.setCarrito(c);
        d.setIdProducto(dto.idProducto());
        d.setCantidad(dto.cantidad());
        d.setPrecioUnitario(producto.precio());
        c.getDetalles().add(d);

        log.info("Item agregado a carrito={}, producto={}, cantidad={}", idCarrito, dto.idProducto(), dto.cantidad());
        return mapper.toResponse(repository.save(c));
    }

    @Transactional
    public CarritoResponseDTO removerItem(Long idCarrito, Long idDetalle) {
        Carrito c = buscarPorId(idCarrito);
        boolean removed = c.getDetalles().removeIf(d -> d.getIdDetalle().equals(idDetalle));
        if (!removed) {
            throw new ResourceNotFoundException("Detalle " + idDetalle + " no encontrado en carrito " + idCarrito);
        }
        log.info("Detalle {} removido del carrito {}", idDetalle, idCarrito);
        return mapper.toResponse(repository.save(c));
    }

    @Transactional
    public CarritoResponseDTO cerrar(Long idCarrito) {
        Carrito c = buscarPorId(idCarrito);
        c.setEstado("CERRADO");
        log.info("Carrito cerrado id={}", idCarrito);
        return mapper.toResponse(repository.save(c));
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarPorId(id));
        log.info("Carrito eliminado id={}", id);
    }

    private Carrito buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito " + id + " no encontrado"));
    }
}
