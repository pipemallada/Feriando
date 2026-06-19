package cl.feriando.usuario.service;

import cl.feriando.usuario.dto.UsuarioRequestDTO;
import cl.feriando.usuario.dto.UsuarioResponseDTO;
import cl.feriando.usuario.exception.BusinessException;
import cl.feriando.usuario.exception.ResourceNotFoundException;
import cl.feriando.usuario.mapper.UsuarioMapper;
import cl.feriando.usuario.model.Usuario;
import cl.feriando.usuario.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias de UsuarioService.

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - reglas de negocio de usuarios")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private UsuarioMapper mapper;

    @InjectMocks
    private UsuarioService service;

    private Usuario usuario;
    private UsuarioRequestDTO requestDTO;
    private UsuarioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombre("Ana");
        usuario.setApellido("Soto");
        usuario.setEmail("ana@feriando.cl");
        usuario.setPasswordHash("hash");
        usuario.setRol("CLIENTE");
        usuario.setActivo((short) 1);
        usuario.setCreatedAt(LocalDateTime.now());

        requestDTO = new UsuarioRequestDTO("Ana", "Soto", "ana@feriando.cl", "hash", "CLIENTE");

        responseDTO = new UsuarioResponseDTO(
                1L, "Ana", "Soto", "ana@feriando.cl", "CLIENTE", (short) 1, usuario.getCreatedAt());
    }

    @Test
    @DisplayName("listar() devuelve todos los usuarios mapeados a DTO")
    void listar_devuelveUsuariosMapeados() {
        // Given
        when(repository.findAll()).thenReturn(List.of(usuario));
        when(mapper.toResponse(usuario)).thenReturn(responseDTO);

        // When
        List<UsuarioResponseDTO> resultado = service.listar();

        // Then
        assertThat(resultado).hasSize(1).containsExactly(responseDTO);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("obtener() devuelve el usuario cuando existe")
    void obtener_existente_devuelveUsuario() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(mapper.toResponse(usuario)).thenReturn(responseDTO);

        // When
        UsuarioResponseDTO resultado = service.obtener(1L);

        // Then
        assertThat(resultado).isEqualTo(responseDTO);
    }

    @Test
    @DisplayName("obtener() lanza ResourceNotFoundException cuando no existe")
    void obtener_inexistente_lanzaNotFound() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("crear() guarda el usuario cuando el email no existe")
    void crear_emailNuevo_guardaUsuario() {
        // Given
        when(repository.existsByEmail("ana@feriando.cl")).thenReturn(false);
        when(mapper.toEntity(requestDTO)).thenReturn(usuario);
        when(repository.save(usuario)).thenReturn(usuario);
        when(mapper.toResponse(usuario)).thenReturn(responseDTO);

        // When
        UsuarioResponseDTO resultado = service.crear(requestDTO);

        // Then
        assertThat(resultado).isEqualTo(responseDTO);
        verify(repository).save(usuario);
    }

    @Test
    @DisplayName("crear() lanza BusinessException cuando el email ya existe")
    void crear_emailDuplicado_lanzaBusiness() {
        // Given
        when(repository.existsByEmail("ana@feriando.cl")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.crear(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ana@feriando.cl");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() actualiza cuando el email no cambia")
    void actualizar_mismoEmail_actualiza() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(usuario)).thenReturn(usuario);
        when(mapper.toResponse(usuario)).thenReturn(responseDTO);

        // When
        UsuarioResponseDTO resultado = service.actualizar(1L, requestDTO);

        // Then
        assertThat(resultado).isEqualTo(responseDTO);
        verify(mapper).updateEntity(usuario, requestDTO);
        // Si el email no cambia, no se consulta existsByEmail
        verify(repository, never()).existsByEmail(any());
    }

    @Test
    @DisplayName("actualizar() lanza BusinessException si el nuevo email ya esta en uso")
    void actualizar_emailEnUso_lanzaBusiness() {
        // Given
        UsuarioRequestDTO cambioEmail =
                new UsuarioRequestDTO("Ana", "Soto", "otro@feriando.cl", "hash", "CLIENTE");
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.existsByEmail("otro@feriando.cl")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.actualizar(1L, cambioEmail))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("otro@feriando.cl");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() lanza ResourceNotFoundException cuando el usuario no existe")
    void actualizar_inexistente_lanzaNotFound() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.actualizar(99L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("eliminar() borra el usuario cuando existe")
    void eliminar_existente_borra() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        // When
        service.eliminar(1L);

        // Then
        verify(repository, times(1)).delete(usuario);
    }

    @Test
    @DisplayName("eliminar() lanza ResourceNotFoundException cuando no existe")
    void eliminar_inexistente_lanzaNotFound() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).delete(any());
    }
}