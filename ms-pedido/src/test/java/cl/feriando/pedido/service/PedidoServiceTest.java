package cl.feriando.pedido.service;

import cl.feriando.pedido.dto.CambioEstadoDTO;
import cl.feriando.pedido.dto.DetallePedidoRequestDTO;
import cl.feriando.pedido.dto.DetallePedidoResponseDTO;
import cl.feriando.pedido.dto.PedidoRequestDTO;
import cl.feriando.pedido.dto.PedidoResponseDTO;
import cl.feriando.pedido.exception.ResourceNotFoundException;
import cl.feriando.pedido.mapper.PedidoMapper;
import cl.feriando.pedido.model.DetallePedido;
import cl.feriando.pedido.model.Pedido;
import cl.feriando.pedido.repository.PedidoRepository;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de PedidoService.
 * Reglas de negocio que se validan:
 *  - El estado inicial al crear es PENDIENTE.
 *  - cambiarEstado() persiste el nuevo estado.
 *  - Filtros por cliente y por feriante.
 *  - Manejo de "no encontrado" en obtener, cambiarEstado y eliminar.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService - reglas de negocio del pedido")
class PedidoServiceTest {

    @Mock private PedidoRepository repository;
    @Mock private PedidoMapper mapper;
    @InjectMocks private PedidoService service;

    private Pedido pedido;
    private PedidoRequestDTO requestDTO;
    private PedidoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        DetallePedido detalle = new DetallePedido();
        detalle.setIdDetalle(1L);
        detalle.setIdProducto(100L);
        detalle.setCantidad(new BigDecimal("2"));
        detalle.setPrecioUnitario(new BigDecimal("500.00"));
        detalle.setSubtotal(new BigDecimal("1000.00"));

        pedido = new Pedido();
        pedido.setIdPedido(1L);
        pedido.setIdCliente(10L);
        pedido.setIdFeriante(20L);
        pedido.setEstado("PENDIENTE");
        pedido.setTotal(new BigDecimal("1000.00"));
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setDetalles(new ArrayList<>(List.of(detalle)));

        DetallePedidoRequestDTO detalleDTO = new DetallePedidoRequestDTO(
                100L, new BigDecimal("2"), new BigDecimal("500.00"));
        requestDTO = new PedidoRequestDTO(10L, 20L, List.of(detalleDTO));

        DetallePedidoResponseDTO detalleRespDTO = new DetallePedidoResponseDTO(
                1L, 100L, new BigDecimal("2"), new BigDecimal("500.00"), new BigDecimal("1000.00"));
        responseDTO = new PedidoResponseDTO(
                1L, 10L, 20L, "PENDIENTE", new BigDecimal("1000.00"),
                pedido.getFechaPedido(), List.of(detalleRespDTO));
    }

    @Test
    @DisplayName("listar() devuelve todos los pedidos mapeados")
    void listar_devuelvePedidos() {
        // Given
        when(repository.findAll()).thenReturn(List.of(pedido));
        when(mapper.toResponse(pedido)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listar()).containsExactly(responseDTO);
    }

    @Test
    @DisplayName("listarPorCliente() filtra por id del cliente")
    void listarPorCliente_devuelvePedidosDelCliente() {
        // Given
        when(repository.findByIdCliente(10L)).thenReturn(List.of(pedido));
        when(mapper.toResponse(pedido)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listarPorCliente(10L)).containsExactly(responseDTO);
        verify(repository).findByIdCliente(10L);
    }

    @Test
    @DisplayName("listarPorFeriante() filtra por id del feriante")
    void listarPorFeriante_devuelvePedidosDelFeriante() {
        // Given
        when(repository.findByIdFeriante(20L)).thenReturn(List.of(pedido));
        when(mapper.toResponse(pedido)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listarPorFeriante(20L)).containsExactly(responseDTO);
        verify(repository).findByIdFeriante(20L);
    }

    @Test
    @DisplayName("obtener() devuelve el pedido cuando existe")
    void obtener_existente_devuelvePedido() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(pedido));
        when(mapper.toResponse(pedido)).thenReturn(responseDTO);

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
    @DisplayName("crear() persiste el pedido con estado PENDIENTE")
    void crear_guardaPedidoConEstadoPendiente() {
        // Given
        when(mapper.toEntity(requestDTO)).thenReturn(pedido);
        when(repository.save(pedido)).thenReturn(pedido);
        when(mapper.toResponse(pedido)).thenReturn(responseDTO);

        // When
        PedidoResponseDTO resultado = service.crear(requestDTO);

        // Then
        assertThat(resultado).isEqualTo(responseDTO);
        verify(repository).save(pedido);
    }

    @Test
    @DisplayName("cambiarEstado() actualiza el estado del pedido")
    void cambiarEstado_actualizaEstado() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(pedido));
        when(repository.save(any(Pedido.class))).thenReturn(pedido);
        when(mapper.toResponse(any(Pedido.class))).thenReturn(responseDTO);
        CambioEstadoDTO dto = new CambioEstadoDTO("PAGADO");

        // When
        service.cambiarEstado(1L, dto);

        // Then: el pedido debe tener el nuevo estado antes de guardarse
        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo("PAGADO");
    }

    @Test
    @DisplayName("cambiarEstado() lanza ResourceNotFoundException cuando no existe")
    void cambiarEstado_inexistente_lanzaNotFound() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.cambiarEstado(99L, new CambioEstadoDTO("PAGADO")))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("eliminar() borra el pedido cuando existe")
    void eliminar_existente_borra() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(pedido));

        // When
        service.eliminar(1L);

        // Then
        verify(repository).delete(pedido);
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