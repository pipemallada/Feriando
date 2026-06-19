package cl.feriando.feriante.service;

import cl.feriando.feriante.client.UsuarioClient;
import cl.feriando.feriante.dto.FerianteRequestDTO;
import cl.feriando.feriante.dto.FerianteResponseDTO;
import cl.feriando.feriante.exception.BusinessException;
import cl.feriando.feriante.exception.ResourceNotFoundException;
import cl.feriando.feriante.mapper.FerianteMapper;
import cl.feriando.feriante.model.Feriante;
import cl.feriando.feriante.repository.FerianteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

// Pruebas unitarias de FerianteService.

@ExtendWith(MockitoExtension.class)
@DisplayName("FerianteService - reglas de negocio del feriante")
class FerianteServiceTest {

    @Mock
    private FerianteRepository repository;

    @Mock
    private FerianteMapper mapper;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private FerianteService service;

    private Feriante feriante;
    private FerianteRequestDTO requestDTO;
    private FerianteResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        feriante = new Feriante();
        feriante.setIdFeriante(1L);
        feriante.setIdUsuario(10L);
        feriante.setNombrePuesto("Frutas Don Pepe");
        feriante.setDescripcion("Frutas frescas");
        feriante.setTelefono("+56912345678");
        feriante.setCalificacionProm(BigDecimal.ZERO);
        feriante.setActivo((short) 1);

        requestDTO = new FerianteRequestDTO(10L, "Frutas Don Pepe", "Frutas frescas", "+56912345678");

        responseDTO = new FerianteResponseDTO(
                1L, 10L, "Frutas Don Pepe", "Frutas frescas", "+56912345678", BigDecimal.ZERO, (short) 1);
    }

    @Test
    @DisplayName("listar() devuelve los feriantes mapeados")
    void listar_devuelveFeriantes() {
        // Given
        when(repository.findAll()).thenReturn(List.of(feriante));
        when(mapper.toResponse(feriante)).thenReturn(responseDTO);

        // When
        List<FerianteResponseDTO> resultado = service.listar();

        // Then
        assertThat(resultado).containsExactly(responseDTO);
    }

    @Test
    @DisplayName("obtener() devuelve el feriante cuando existe")
    void obtener_existente_devuelveFeriante() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(feriante));
        when(mapper.toResponse(feriante)).thenReturn(responseDTO);

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
    @DisplayName("crear() guarda el feriante cuando el usuario existe y no tiene perfil")
    void crear_usuarioValido_guarda() {
        // Given
        when(usuarioClient.existeUsuario(10L)).thenReturn(true);
        when(repository.findByIdUsuario(10L)).thenReturn(Optional.empty());
        when(mapper.toEntity(requestDTO)).thenReturn(feriante);
        when(repository.save(feriante)).thenReturn(feriante);
        when(mapper.toResponse(feriante)).thenReturn(responseDTO);

        // When
        FerianteResponseDTO resultado = service.crear(requestDTO);

        // Then
        assertThat(resultado).isEqualTo(responseDTO);
        verify(repository).save(feriante);
    }

    @Test
    @DisplayName("crear() lanza BusinessException cuando el usuario no existe en ms-usuario")
    void crear_usuarioInexistente_lanzaBusiness() {
        // Given
        when(usuarioClient.existeUsuario(10L)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> service.crear(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no existe");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crear() lanza BusinessException cuando el usuario ya tiene feriante")
    void crear_usuarioConFeriante_lanzaBusiness() {
        // Given
        when(usuarioClient.existeUsuario(10L)).thenReturn(true);
        when(repository.findByIdUsuario(10L)).thenReturn(Optional.of(feriante));

        // When / Then
        assertThatThrownBy(() -> service.crear(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya tiene un feriante");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() aplica cambios cuando el feriante existe")
    void actualizar_existente_actualiza() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(feriante));
        when(repository.save(feriante)).thenReturn(feriante);
        when(mapper.toResponse(feriante)).thenReturn(responseDTO);

        // When
        FerianteResponseDTO resultado = service.actualizar(1L, requestDTO);

        // Then
        assertThat(resultado).isEqualTo(responseDTO);
        verify(mapper).updateEntity(feriante, requestDTO);
    }

    @Test
    @DisplayName("actualizar() lanza ResourceNotFoundException cuando no existe")
    void actualizar_inexistente_lanzaNotFound() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.actualizar(99L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("eliminar() borra el feriante cuando existe")
    void eliminar_existente_borra() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(feriante));

        // When
        service.eliminar(1L);

        // Then
        verify(repository).delete(feriante);
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

    @Test
    @DisplayName("actualizarCalificacionPromedio() redondea a 2 decimales (HALF_UP) antes de guardar")
    void actualizarCalificacion_redondeaAEscala2() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(feriante));
        when(repository.save(any(Feriante.class))).thenReturn(feriante);
        when(mapper.toResponse(feriante)).thenReturn(responseDTO);

        // When: un promedio con muchos decimales debe quedar en escala 2
        service.actualizarCalificacionPromedio(1L, new BigDecimal("4.567"));

        // Then
        ArgumentCaptor<Feriante> captor = ArgumentCaptor.forClass(Feriante.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCalificacionProm()).isEqualByComparingTo("4.57");
        assertThat(captor.getValue().getCalificacionProm().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("actualizarCalificacionPromedio() lanza ResourceNotFoundException cuando no existe")
    void actualizarCalificacion_inexistente_lanzaNotFound() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.actualizarCalificacionPromedio(99L, BigDecimal.ONE))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}