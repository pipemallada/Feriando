package cl.feriando.pago.service;

import cl.feriando.pago.client.InventarioClient;
import cl.feriando.pago.client.PedidoClient;
import cl.feriando.pago.dto.PagoRequestDTO;
import cl.feriando.pago.dto.PagoResponseDTO;
import cl.feriando.pago.dto.PedidoDTO;
import cl.feriando.pago.exception.BusinessException;
import cl.feriando.pago.exception.ResourceNotFoundException;
import cl.feriando.pago.mapper.PagoMapper;
import cl.feriando.pago.model.Pago;
import cl.feriando.pago.repository.PagoRepository;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de PagoService.
 * Reglas de negocio que se validan:
 *  - 1 pedido = 1 pago (BusinessException si ya existe).
 *  - confirmar() no puede aplicarse a un pago ya confirmado.
 *  - confirmar() requiere obtener el pedido de ms-pedido (mock PedidoClient).
 *  - confirmar() descuenta stock de cada detalle vía InventarioClient.
 *  - confirmar() marca el pedido como PAGADO en ms-pedido.
 *  - rechazar() cambia el estado a RECHAZADO.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PagoService - reglas de negocio del pago")
class PagoServiceTest {

    @Mock private PagoRepository repository;
    @Mock private PagoMapper mapper;
    @Mock private PedidoClient pedidoClient;
    @Mock private InventarioClient inventarioClient;
    @InjectMocks private PagoService service;

    private Pago pago;
    private PagoRequestDTO requestDTO;
    private PagoResponseDTO responseDTO;
    private PedidoDTO pedidoDTO;

    @BeforeEach
    void setUp() {
        pago = new Pago();
        pago.setIdPago(1L);
        pago.setIdPedido(50L);
        pago.setIdCliente(10L);
        pago.setMonto(new BigDecimal("1000.00"));
        pago.setMetodoPago("TRANSFERENCIA");
        pago.setEstadoPago("PENDIENTE");
        pago.setFechaPago(LocalDateTime.now());

        requestDTO = new PagoRequestDTO(50L, 10L, new BigDecimal("1000.00"), "TRANSFERENCIA", null);

        responseDTO = new PagoResponseDTO(
                1L, 50L, 10L, new BigDecimal("1000.00"),
                "TRANSFERENCIA", "PENDIENTE", pago.getFechaPago(), null);

        PedidoDTO.DetallePedidoDTO detalleDTO = new PedidoDTO.DetallePedidoDTO(100L, new BigDecimal("2"));
        pedidoDTO = new PedidoDTO(
                50L, 10L, 20L, "PENDIENTE", new BigDecimal("1000.00"), List.of(detalleDTO));
    }

    @Test
    @DisplayName("listar() devuelve los pagos mapeados")
    void listar_devuelvePagos() {
        // Given
        when(repository.findAll()).thenReturn(List.of(pago));
        when(mapper.toResponse(pago)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listar()).containsExactly(responseDTO);
    }

    @Test
    @DisplayName("obtener() devuelve el pago cuando existe")
    void obtener_existente_devuelvePago() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(pago));
        when(mapper.toResponse(pago)).thenReturn(responseDTO);

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
    @DisplayName("crear() guarda el pago cuando el pedido no tiene pago previo")
    void crear_pedidoSinPago_guarda() {
        // Given
        when(repository.existsByIdPedido(50L)).thenReturn(false);
        when(mapper.toEntity(requestDTO)).thenReturn(pago);
        when(repository.save(pago)).thenReturn(pago);
        when(mapper.toResponse(pago)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.crear(requestDTO)).isEqualTo(responseDTO);
        verify(repository).save(pago);
    }

    @Test
    @DisplayName("crear() lanza BusinessException cuando el pedido ya tiene un pago")
    void crear_pedidoConPago_lanzaBusiness() {
        // Given
        when(repository.existsByIdPedido(50L)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.crear(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya tiene un pago");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("confirmar() cambia estado a CONFIRMADO, marca pedido como PAGADO y descuenta stock")
    void confirmar_pagoExistente_confirmaYDescuentaStock() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(pago));
        when(pedidoClient.buscarPedido(50L)).thenReturn(Optional.of(pedidoDTO));
        when(repository.save(any(Pago.class))).thenReturn(pago);
        when(mapper.toResponse(any(Pago.class))).thenReturn(responseDTO);

        // When
        service.confirmar(1L);

        // Then: estado cambiado a CONFIRMADO
        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEstadoPago()).isEqualTo("CONFIRMADO");

        // ms-pedido notificado
        verify(pedidoClient).marcarComoPagado(50L);

        // ms-inventario descontado por cada detalle del pedido
        verify(inventarioClient).descontarStock(100L, new BigDecimal("2"));
    }

    @Test
    @DisplayName("confirmar() lanza BusinessException cuando el pago ya está confirmado")
    void confirmar_yaConfirmado_lanzaBusiness() {
        // Given
        pago.setEstadoPago("CONFIRMADO");
        when(repository.findById(1L)).thenReturn(Optional.of(pago));

        // When / Then
        assertThatThrownBy(() -> service.confirmar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya está confirmado");
        verify(pedidoClient, never()).buscarPedido(any());
    }

    @Test
    @DisplayName("confirmar() lanza BusinessException cuando ms-pedido no responde")
    void confirmar_pedidoNoDisponible_lanzaBusiness() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(pago));
        when(pedidoClient.buscarPedido(50L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.confirmar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ms-pedido");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("confirmar() lanza ResourceNotFoundException cuando el pago no existe")
    void confirmar_pagoInexistente_lanzaNotFound() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.confirmar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("rechazar() cambia el estado del pago a RECHAZADO")
    void rechazar_cambiaEstadoARechazado() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(pago));
        when(repository.save(any(Pago.class))).thenReturn(pago);
        when(mapper.toResponse(any(Pago.class))).thenReturn(responseDTO);

        // When
        service.rechazar(1L);

        // Then
        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEstadoPago()).isEqualTo("RECHAZADO");
    }

    @Test
    @DisplayName("rechazar() lanza ResourceNotFoundException cuando no existe")
    void rechazar_inexistente_lanzaNotFound() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.rechazar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("eliminar() borra el pago cuando existe")
    void eliminar_existente_borra() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(pago));

        // When
        service.eliminar(1L);

        // Then
        verify(repository).delete(pago);
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