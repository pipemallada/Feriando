package cl.feriando.producto.service;

import cl.feriando.producto.dto.ProductoRequestDTO;
import cl.feriando.producto.dto.ProductoResponseDTO;
import cl.feriando.producto.exception.ResourceNotFoundException;
import cl.feriando.producto.mapper.ProductoMapper;
import cl.feriando.producto.model.Categoria;
import cl.feriando.producto.model.Producto;
import cl.feriando.producto.repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de ProductoService.
 * ProductoService colabora con CategoriaService (para validar/obtener la
 * categoria del producto). En estas pruebas CategoriaService se mockea, de modo
 * que se prueba solo la logica de ProductoService de forma separada >:) .
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoService - reglas de negocio de productos")
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    @Mock
    private ProductoMapper mapper;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private ProductoService service;

    private Categoria categoria;
    private Producto producto;
    private ProductoRequestDTO requestDTO;
    private ProductoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setIdCategoria(5L);
        categoria.setNombre("Verduras");

        producto = new Producto();
        producto.setIdProducto(1L);
        producto.setIdFeriante(10L);
        producto.setCategoria(categoria);
        producto.setNombre("Tomate");
        producto.setDescripcion("Tomate larga vida");
        producto.setPrecio(new BigDecimal("990.00"));
        producto.setUnidad("kg");
        producto.setActivo((short) 1);

        requestDTO = new ProductoRequestDTO(
                10L, 5L, "Tomate", "Tomate larga vida", new BigDecimal("990.00"), "kg");

        responseDTO = new ProductoResponseDTO(
                1L, 10L, 5L, "Verduras", "Tomate", "Tomate larga vida",
                new BigDecimal("990.00"), "kg", (short) 1);
    }

    @Test
    @DisplayName("listar() devuelve los productos mapeados")
    void listar_devuelveProductos() {
        // Given
        when(repository.findAll()).thenReturn(List.of(producto));
        when(mapper.toResponse(producto)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listar()).containsExactly(responseDTO);
    }

    @Test
    @DisplayName("listarPorFeriante() filtra por el id del feriante")
    void listarPorFeriante_devuelveProductosDelFeriante() {
        // Given
        when(repository.findByIdFeriante(10L)).thenReturn(List.of(producto));
        when(mapper.toResponse(producto)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listarPorFeriante(10L)).containsExactly(responseDTO);
        verify(repository).findByIdFeriante(10L);
    }

    @Test
    @DisplayName("obtener() devuelve el producto cuando existe")
    void obtener_existente_devuelveProducto() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(producto));
        when(mapper.toResponse(producto)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.obtener(1L)).isEqualTo(responseDTO);
    }

    @Test
    @DisplayName("obtener() lanza ResourceNotFoundException cuando no existe")
    void obtener_inexistente_lanzaNotFound() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("crear() resuelve la categoria y guarda el producto")
    void crear_categoriaValida_guarda() {
        // Given
        when(categoriaService.buscarPorId(5L)).thenReturn(categoria);
        when(mapper.toEntity(requestDTO, categoria)).thenReturn(producto);
        when(repository.save(producto)).thenReturn(producto);
        when(mapper.toResponse(producto)).thenReturn(responseDTO);

        // When
        ProductoResponseDTO resultado = service.crear(requestDTO);

        // Then
        assertThat(resultado).isEqualTo(responseDTO);
        verify(categoriaService).buscarPorId(5L);
        verify(repository).save(producto);
    }

    @Test
    @DisplayName("crear() propaga ResourceNotFoundException si la categoria no existe")
    void crear_categoriaInexistente_propagaNotFound() {
        // Given
        when(categoriaService.buscarPorId(5L))
                .thenThrow(new ResourceNotFoundException("Categoría 5 no encontrada"));

        // When / Then
        assertThatThrownBy(() -> service.crear(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() actualiza el producto cuando existe")
    void actualizar_existente_actualiza() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(producto));
        when(categoriaService.buscarPorId(5L)).thenReturn(categoria);
        when(repository.save(producto)).thenReturn(producto);
        when(mapper.toResponse(producto)).thenReturn(responseDTO);

        // When
        ProductoResponseDTO resultado = service.actualizar(1L, requestDTO);

        // Then
        assertThat(resultado).isEqualTo(responseDTO);
        verify(mapper).updateEntity(producto, requestDTO, categoria);
    }

    @Test
    @DisplayName("actualizar() lanza ResourceNotFoundException cuando el producto no existe")
    void actualizar_inexistente_lanzaNotFound() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.actualizar(99L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("eliminar() borra el producto cuando existe")
    void eliminar_existente_borra() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(producto));

        // When
        service.eliminar(1L);

        // Then
        verify(repository).delete(producto);
    }

    @Test
    @DisplayName("eliminar() lanza ResourceNotFoundException cuando no existe")
    void eliminar_inexistente_lanzaNotFound() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}