package cl.feriando.carrito.service;

import cl.feriando.carrito.client.ProductoClient;
import cl.feriando.carrito.dto.CarritoRequestDTO;
import cl.feriando.carrito.dto.CarritoResponseDTO;
import cl.feriando.carrito.dto.DetalleCarritoRequestDTO;
import cl.feriando.carrito.dto.DetalleCarritoResponseDTO;
import cl.feriando.carrito.dto.ProductoBasicoDTO;
import cl.feriando.carrito.exception.BusinessException;
import cl.feriando.carrito.exception.ResourceNotFoundException;
import cl.feriando.carrito.mapper.CarritoMapper;
import cl.feriando.carrito.model.Carrito;
import cl.feriando.carrito.model.DetalleCarrito;
import cl.feriando.carrito.repository.CarritoRepository;

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
 * Pruebas unitarias de CarritoService.
 * Reglas de negocio que se validan:
 *  - Solo se puede agregar items a un carrito ACTIVO.
 *  - El precio del producto viene de ms-producto (mock de ProductoClient),nunca del cliente.
 *  - Si ms-producto no responde o el producto no existe, se lanza BusinessException.
 *  - removerItem usa orphanRemoval: sacar el detalle de la lista lo borra.
 *  - cerrar() cambia el estado a CERRADO.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CarritoService - reglas de negocio del carrito")
class CarritoServiceTest {

    @Mock private CarritoRepository repository;
    @Mock private CarritoMapper mapper;
    @Mock private ProductoClient productoClient;
    @InjectMocks private CarritoService service;

    private Carrito carrito;
    private CarritoRequestDTO requestDTO;
    private CarritoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        carrito = new Carrito();
        carrito.setIdCarrito(1L);
        carrito.setIdCliente(10L);
        carrito.setEstado("ACTIVO");
        carrito.setCreatedAt(LocalDateTime.now());
        carrito.setDetalles(new ArrayList<>());

        requestDTO = new CarritoRequestDTO(10L);

        responseDTO = new CarritoResponseDTO(
                1L, 10L, "ACTIVO", carrito.getCreatedAt(), List.of(), BigDecimal.ZERO);
    }

    @Test
    @DisplayName("listar() devuelve los carritos mapeados")
    void listar_devuelveCarritos() {
        // Given
        when(repository.findAll()).thenReturn(List.of(carrito));
        when(mapper.toResponse(carrito)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listar()).containsExactly(responseDTO);
    }

    @Test
    @DisplayName("obtener() devuelve el carrito cuando existe")
    void obtener_existente_devuelveCarrito() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(carrito));
        when(mapper.toResponse(carrito)).thenReturn(responseDTO);

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
    @DisplayName("crear() persiste el carrito y lo devuelve")
    void crear_guardaCarrito() {
        // Given
        when(mapper.toEntity(requestDTO)).thenReturn(carrito);
        when(repository.save(carrito)).thenReturn(carrito);
        when(mapper.toResponse(carrito)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.crear(requestDTO)).isEqualTo(responseDTO);
        verify(repository).save(carrito);
    }

    @Test
    @DisplayName("agregarItem() consulta el precio en ms-producto y lo congela en el detalle")
    void agregarItem_carritoActivo_agregaDetalle() {
        // Given
        ProductoBasicoDTO producto = new ProductoBasicoDTO(5L, new BigDecimal("1990.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(carrito));
        when(productoClient.buscarProducto(5L)).thenReturn(Optional.of(producto));
        when(repository.save(any(Carrito.class))).thenReturn(carrito);
        when(mapper.toResponse(carrito)).thenReturn(responseDTO);
        DetalleCarritoRequestDTO item = new DetalleCarritoRequestDTO(5L, new BigDecimal("2"));

        // When
        service.agregarItem(1L, item);

        // Then: el detalle añadido trae el precio de ms-producto, no del cliente
        ArgumentCaptor<Carrito> captor = ArgumentCaptor.forClass(Carrito.class);
        verify(repository).save(captor.capture());
        DetalleCarrito detalle = captor.getValue().getDetalles().get(0);
        assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo("1990.00");
        assertThat(detalle.getCantidad()).isEqualByComparingTo("2");
    }

    @Test
    @DisplayName("agregarItem() lanza BusinessException cuando el carrito NO está ACTIVO")
    void agregarItem_carritoCerrado_lanzaBusiness() {
        // Given
        carrito.setEstado("CERRADO");
        when(repository.findById(1L)).thenReturn(Optional.of(carrito));
        DetalleCarritoRequestDTO item = new DetalleCarritoRequestDTO(5L, new BigDecimal("1"));

        // When / Then
        assertThatThrownBy(() -> service.agregarItem(1L, item))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no está activo");
        verify(productoClient, never()).buscarProducto(any());
    }

    @Test
    @DisplayName("agregarItem() lanza BusinessException cuando ms-producto no encuentra el producto")
    void agregarItem_productoInexistente_lanzaBusiness() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(carrito));
        when(productoClient.buscarProducto(5L)).thenReturn(Optional.empty());
        DetalleCarritoRequestDTO item = new DetalleCarritoRequestDTO(5L, new BigDecimal("1"));

        // When / Then
        assertThatThrownBy(() -> service.agregarItem(1L, item))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no existe");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("removerItem() elimina el detalle de la lista del carrito")
    void removerItem_existente_eliminaDetalle() {
        // Given
        DetalleCarrito detalle = new DetalleCarrito();
        detalle.setIdDetalle(99L);
        detalle.setCarrito(carrito);
        detalle.setIdProducto(5L);
        detalle.setCantidad(BigDecimal.ONE);
        detalle.setPrecioUnitario(BigDecimal.TEN);
        carrito.getDetalles().add(detalle);

        when(repository.findById(1L)).thenReturn(Optional.of(carrito));
        when(repository.save(any(Carrito.class))).thenReturn(carrito);
        when(mapper.toResponse(carrito)).thenReturn(responseDTO);

        // When
        service.removerItem(1L, 99L);

        // Then: la lista queda vacía
        ArgumentCaptor<Carrito> captor = ArgumentCaptor.forClass(Carrito.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDetalles()).isEmpty();
    }

    @Test
    @DisplayName("removerItem() lanza ResourceNotFoundException cuando el detalle no pertenece al carrito")
    void removerItem_detalleInexistente_lanzaNotFound() {
        // Given: carrito sin detalles
        when(repository.findById(1L)).thenReturn(Optional.of(carrito));

        // When / Then
        assertThatThrownBy(() -> service.removerItem(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("cerrar() cambia el estado del carrito a CERRADO")
    void cerrar_carritoActivo_pasaACerrado() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(carrito));
        when(repository.save(any(Carrito.class))).thenReturn(carrito);
        when(mapper.toResponse(carrito)).thenReturn(responseDTO);

        // When
        service.cerrar(1L);

        // Then
        ArgumentCaptor<Carrito> captor = ArgumentCaptor.forClass(Carrito.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo("CERRADO");
    }

    @Test
    @DisplayName("eliminar() borra el carrito cuando existe")
    void eliminar_existente_borra() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(carrito));

        // When
        service.eliminar(1L);

        // Then
        verify(repository).delete(carrito);
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