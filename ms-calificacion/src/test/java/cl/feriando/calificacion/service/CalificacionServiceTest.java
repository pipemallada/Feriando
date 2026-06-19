package cl.feriando.calificacion.service;

import cl.feriando.calificacion.client.FerianteClient;
import cl.feriando.calificacion.dto.CalificacionRequestDTO;
import cl.feriando.calificacion.dto.CalificacionResponseDTO;
import cl.feriando.calificacion.exception.BusinessException;
import cl.feriando.calificacion.exception.ResourceNotFoundException;
import cl.feriando.calificacion.mapper.CalificacionMapper;
import cl.feriando.calificacion.model.Calificacion;
import cl.feriando.calificacion.repository.CalificacionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de CalificacionService.
 * Reglas de negocio que se validan:
 *  - 1 pedido = 1 calificación (BusinessException si ya existe).
 *  - Al crear, se recalcula el promedio del feriante y se notifica a ms-feriante.
 *  - Al eliminar, también se recalcula y notifica el nuevo promedio.
 *  - El promedio se calcula como media  y se redondea a 2 decimales.
 *  - Si no quedan calificaciones tras eliminar, el promedio enviado es 0.00.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CalificacionService - reglas de negocio de calificaciones")
class CalificacionServiceTest {

    @Mock private CalificacionRepository repository;
    @Mock private CalificacionMapper mapper;
    @Mock private FerianteClient ferianteClient;
    @InjectMocks private CalificacionService service;

    private Calificacion calificacion;
    private CalificacionRequestDTO requestDTO;
    private CalificacionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        calificacion = new Calificacion();
        calificacion.setIdCalificacion(1L);
        calificacion.setIdPedido(30L);
        calificacion.setIdCliente(10L);
        calificacion.setIdFeriante(20L);
        calificacion.setPuntaje((short) 5);
        calificacion.setComentario("Excelente servicio");
        calificacion.setFecha(LocalDateTime.now());

        requestDTO = new CalificacionRequestDTO(30L, 10L, 20L, (short) 5, "Excelente servicio");

        responseDTO = new CalificacionResponseDTO(
                1L, 30L, 10L, 20L, (short) 5, "Excelente servicio", calificacion.getFecha());
    }

    @Test
    @DisplayName("listar() devuelve las calificaciones mapeadas")
    void listar_devuelveCalificaciones() {
        // Given
        when(repository.findAll()).thenReturn(List.of(calificacion));
        when(mapper.toResponse(calificacion)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listar()).containsExactly(responseDTO);
    }

    @Test
    @DisplayName("listarPorFeriante() devuelve las calificaciones del feriante")
    void listarPorFeriante_devuelveCalificacionesDelFeriante() {
        // Given
        when(repository.findByIdFeriante(20L)).thenReturn(List.of(calificacion));
        when(mapper.toResponse(calificacion)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listarPorFeriante(20L)).containsExactly(responseDTO);
        verify(repository).findByIdFeriante(20L);
    }

    @Test
    @DisplayName("obtener() devuelve la calificación cuando existe")
    void obtener_existente_devuelveCalificacion() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(calificacion));
        when(mapper.toResponse(calificacion)).thenReturn(responseDTO);

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
    @DisplayName("crear() guarda la calificación y notifica el nuevo promedio a ms-feriante")
    void crear_pedidoSinCalificacion_guardaYNotificaPromedio() {
        // Given: luego de guardar, hay 1 calificación con puntaje 5 -> promedio=5.00
        when(repository.existsByIdPedido(30L)).thenReturn(false);
        when(mapper.toEntity(requestDTO)).thenReturn(calificacion);
        when(repository.save(calificacion)).thenReturn(calificacion);
        when(mapper.toResponse(calificacion)).thenReturn(responseDTO);
        when(repository.findByIdFeriante(20L)).thenReturn(List.of(calificacion));

        // When
        service.crear(requestDTO);

        // Then: ms-feriante recibe el promedio 5.00
        ArgumentCaptor<BigDecimal> promedioCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(ferianteClient).actualizarPromedio(eq(20L), promedioCaptor.capture());
        assertThat(promedioCaptor.getValue()).isEqualByComparingTo("5.00");
    }

    @Test
    @DisplayName("crear() calcula el promedio correcto con múltiples calificaciones")
    void crear_variasCalificaciones_calculaPromedioCorrectamente() {
        // Given: ya existe una calificación con puntaje 3; se agrega una con puntaje 5
        // -> promedio esperado: (3+5)/2 = 4.00
        Calificacion anterior = new Calificacion();
        anterior.setIdFeriante(20L);
        anterior.setPuntaje((short) 3);

        when(repository.existsByIdPedido(30L)).thenReturn(false);
        when(mapper.toEntity(requestDTO)).thenReturn(calificacion);
        when(repository.save(calificacion)).thenReturn(calificacion);
        when(mapper.toResponse(calificacion)).thenReturn(responseDTO);
        when(repository.findByIdFeriante(20L)).thenReturn(List.of(anterior, calificacion));

        // When
        service.crear(requestDTO);

        // Then
        ArgumentCaptor<BigDecimal> captor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(ferianteClient).actualizarPromedio(eq(20L), captor.capture());
        assertThat(captor.getValue()).isEqualByComparingTo("4.00");
    }

    @Test
    @DisplayName("crear() lanza BusinessException cuando el pedido ya tiene calificación")
    void crear_pedidoConCalificacion_lanzaBusiness() {
        // Given
        when(repository.existsByIdPedido(30L)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.crear(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya tiene una calificación");
        verify(repository, never()).save(any());
        verify(ferianteClient, never()).actualizarPromedio(any(), any());
    }

    @Test
    @DisplayName("eliminar() borra la calificación y envía promedio 0.00 si era la última")
    void eliminar_ultimaCalificacion_enviaCeroAFeriante() {
        // Given: tras borrar, no quedan calificaciones del feriante
        when(repository.findById(1L)).thenReturn(Optional.of(calificacion));
        when(repository.findByIdFeriante(20L)).thenReturn(List.of());

        // When
        service.eliminar(1L);

        // Then
        verify(repository).delete(calificacion);
        ArgumentCaptor<BigDecimal> captor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(ferianteClient).actualizarPromedio(eq(20L), captor.capture());
        assertThat(captor.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("eliminar() recalcula el promedio con las calificaciones restantes")
    void eliminar_quedanCalificaciones_recalculaPromedio() {
        // Given: tras borrar la de puntaje 5, queda otra con puntaje 3 -> promedio=3.00
        Calificacion restante = new Calificacion();
        restante.setIdFeriante(20L);
        restante.setPuntaje((short) 3);

        when(repository.findById(1L)).thenReturn(Optional.of(calificacion));
        when(repository.findByIdFeriante(20L)).thenReturn(List.of(restante));

        // When
        service.eliminar(1L);

        // Then
        verify(repository).delete(calificacion);
        ArgumentCaptor<BigDecimal> captor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(ferianteClient).actualizarPromedio(eq(20L), captor.capture());
        assertThat(captor.getValue()).isEqualByComparingTo("3.00");
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
        verify(ferianteClient, never()).actualizarPromedio(any(), any());
    }
}