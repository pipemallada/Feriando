package cl.feriando.producto.service;

import cl.feriando.producto.dto.ProductoRequestDTO;
import cl.feriando.producto.dto.ProductoResponseDTO;
import cl.feriando.producto.exception.ResourceNotFoundException;
import cl.feriando.producto.mapper.ProductoMapper;
import cl.feriando.producto.model.Categoria;
import cl.feriando.producto.model.Producto;
import cl.feriando.producto.repository.ProductoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository repository;
    private final ProductoMapper mapper;
    private final CategoriaService categoriaService;

    public ProductoService(ProductoRepository repository,
                           ProductoMapper mapper,
                           CategoriaService categoriaService) {
        this.repository = repository;
        this.mapper = mapper;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarPorFeriante(Long idFeriante) {
        return repository.findByIdFeriante(idFeriante).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO obtener(Long id) {
        return mapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        Categoria categoria = categoriaService.buscarPorId(dto.idCategoria());
        Producto guardado = repository.save(mapper.toEntity(dto, categoria));
        log.info("Producto creado id={}, feriante={}", guardado.getIdProducto(), dto.idFeriante());
        return mapper.toResponse(guardado);
    }

    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto p = buscarPorId(id);
        Categoria categoria = categoriaService.buscarPorId(dto.idCategoria());
        mapper.updateEntity(p, dto, categoria);
        log.info("Producto actualizado id={}", id);
        return mapper.toResponse(repository.save(p));
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscarPorId(id));
        log.info("Producto eliminado id={}", id);
    }

    private Producto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto " + id + " no encontrado"));
    }
}
