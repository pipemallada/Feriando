package cl.feriando.producto.service;

import cl.feriando.producto.dto.CategoriaRequestDTO;
import cl.feriando.producto.dto.CategoriaResponseDTO;
import cl.feriando.producto.exception.BusinessException;
import cl.feriando.producto.exception.ResourceNotFoundException;
import cl.feriando.producto.mapper.CategoriaMapper;
import cl.feriando.producto.model.Categoria;
import cl.feriando.producto.repository.CategoriaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de CategoriaService.
 * Valida la regla de unicidad de nombre y el manejo de "no encontrado".
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaService - reglas de negocio de categorias")
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repository;

    @Mock
    private CategoriaMapper mapper;

    @InjectMocks
    private CategoriaService service;

    private Categoria categoria;
    private CategoriaRequestDTO requestDTO;
    private CategoriaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setIdCategoria(1L);
        categoria.setNombre("Frutas");

        requestDTO = new CategoriaRequestDTO("Frutas");
        responseDTO = new CategoriaResponseDTO(1L, "Frutas");
    }

    @Test
    @DisplayName("listar() devuelve las categorias mapeadas")
    void listar_devuelveCategorias() {
        // Given
        when(repository.findAll()).thenReturn(List.of(categoria));
        when(mapper.toResponse(categoria)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listar()).containsExactly(responseDTO);
    }

    @Test
    @DisplayName("obtener() devuelve la categoria cuando existe")
    void obtener_existente_devuelveCategoria() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(categoria));
        when(mapper.toResponse(categoria)).thenReturn(responseDTO);

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
    @DisplayName("crear() guarda la categoria cuando el nombre no existe")
    void crear_nombreNuevo_guarda() {
        // Given
        when(repository.existsByNombreIgnoreCase("Frutas")).thenReturn(false);
        when(mapper.toEntity(requestDTO)).thenReturn(categoria);
        when(repository.save(categoria)).thenReturn(categoria);
        when(mapper.toResponse(categoria)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.crear(requestDTO)).isEqualTo(responseDTO);
        verify(repository).save(categoria);
    }

    @Test
    @DisplayName("crear() lanza BusinessException cuando el nombre ya existe")
    void crear_nombreDuplicado_lanzaBusiness() {
        // Given
        when(repository.existsByNombreIgnoreCase("Frutas")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.crear(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Frutas");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() aplica los cambios cuando la categoria existe")
    void actualizar_existente_actualiza() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(categoria));
        when(repository.save(categoria)).thenReturn(categoria);
        when(mapper.toResponse(categoria)).thenReturn(responseDTO);

        // When
        CategoriaResponseDTO resultado = service.actualizar(1L, requestDTO);

        // Then
        assertThat(resultado).isEqualTo(responseDTO);
        verify(mapper).updateEntity(categoria, requestDTO);
    }

    @Test
    @DisplayName("eliminar() borra la categoria cuando existe")
    void eliminar_existente_borra() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(categoria));

        // When
        service.eliminar(1L);

        // Then
        verify(repository).delete(categoria);
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