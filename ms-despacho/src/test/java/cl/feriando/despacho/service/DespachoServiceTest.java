package cl.feriando.despacho.service;

import cl.feriando.despacho.dto.CambioEstadoDespachoDTO;
import cl.feriando.despacho.dto.DespachoRequestDTO;
import cl.feriando.despacho.dto.DespachoResponseDTO;
import cl.feriando.despacho.exception.BusinessException;
import cl.feriando.despacho.exception.ResourceNotFoundException;
import cl.feriando.despacho.mapper.DespachoMapper;
import cl.feriando.despacho.model.Despacho;
import cl.feriando.despacho.repository.DespachoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de DespachoService.
 * Reglas de negocio que se validan:
 *  - 1 pedido = 1 despacho (BusinessException si ya existe).
 *  - Para DOMICILIO la dirección es obligatoria (validación condicional en service).
 *  - Para RETIRO la dirección puede estar vacía (no se exige).
 *  - Al cambiar estado a ENTREGADO, se registra automáticamente la fecha_entrega.
 *  - Al cambiar a otros estados, fecha_entrega no se toca.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DespachoService - reglas de negocio del despacho")
class DespachoServiceTest {

    @Mock private DespachoRepository repository;
    @Mock private DespachoMapper mapper;
    @InjectMocks private DespachoService service;

    private Despacho despacho;
    private DespachoRequestDTO requestDomicilio;
    private DespachoRequestDTO requestRetiro;
    private DespachoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        despacho = new Despacho();
        despacho.setIdDespacho(1L);
        despacho.setIdPedido(50L);
        despacho.setTipoEntrega("DOMICILIO");
        despacho.setDireccion("Av. Siempre Viva 123");
        despacho.setComuna("Santiago");
        despacho.setEstadoDespacho("PENDIENTE");

        requestDomicilio = new DespachoRequestDTO(
                50L, "DOMICILIO", "Av. Siempre Viva 123", "Santiago", null);
        requestRetiro = new DespachoRequestDTO(
                51L, "RETIRO", null, null, null);

        responseDTO = new DespachoResponseDTO(
                1L, 50L, "DOMICILIO", "Av. Siempre Viva 123",
                "Santiago", "PENDIENTE", null, null);
    }

    @Test
    @DisplayName("listar() devuelve todos los despachos mapeados")
    void listar_devuelveDespachos() {
        // Given
        when(repository.findAll()).thenReturn(List.of(despacho));
        when(mapper.toResponse(despacho)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listar()).containsExactly(responseDTO);
    }

    @Test
    @DisplayName("obtener() devuelve el despacho cuando existe")
    void obtener_existente_devuelveDespacho() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(despacho));
        when(mapper.toResponse(despacho)).thenReturn(responseDTO);

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
    @DisplayName("obtenerPorPedido() devuelve el despacho del pedido")
    void obtenerPorPedido_existente_devuelveDespacho() {
        // Given
        when(repository.findByIdPedido(50L)).thenReturn(Optional.of(despacho));
        when(mapper.toResponse(despacho)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.obtenerPorPedido(50L)).isEqualTo(responseDTO);
    }

    @Test
    @DisplayName("obtenerPorPedido() lanza ResourceNotFoundException cuando no existe")
    void obtenerPorPedido_inexistente_lanzaNotFound() {
        // Given
        when(repository.findByIdPedido(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.obtenerPorPedido(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("crear() guarda el despacho cuando es domicilio con dirección")
    void crear_domicilioConDireccion_guarda() {
        // Given
        when(repository.existsByIdPedido(50L)).thenReturn(false);
        when(mapper.toEntity(requestDomicilio)).thenReturn(despacho);
        when(repository.save(despacho)).thenReturn(despacho);
        when(mapper.toResponse(despacho)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.crear(requestDomicilio)).isEqualTo(responseDTO);
        verify(repository).save(despacho);
    }

    @Test
    @DisplayName("crear() guarda el despacho cuando es RETIRO (sin dirección)")
    void crear_retiroSinDireccion_guarda() {
        // Given
        Despacho despachoRetiro = new Despacho();
        despachoRetiro.setIdDespacho(2L);
        despachoRetiro.setIdPedido(51L);
        despachoRetiro.setTipoEntrega("RETIRO");
        DespachoResponseDTO respRetiro = new DespachoResponseDTO(
                2L, 51L, "RETIRO", null, null, "PENDIENTE", null, null);

        when(repository.existsByIdPedido(51L)).thenReturn(false);
        when(mapper.toEntity(requestRetiro)).thenReturn(despachoRetiro);
        when(repository.save(despachoRetiro)).thenReturn(despachoRetiro);
        when(mapper.toResponse(despachoRetiro)).thenReturn(respRetiro);

        // When / Then
        assertThat(service.crear(requestRetiro)).isEqualTo(respRetiro);
    }

    @Test
    @DisplayName("crear() lanza BusinessException cuando el pedido ya tiene despacho")
    void crear_pedidoConDespacho_lanzaBusiness() {
        // Given
        when(repository.existsByIdPedido(50L)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.crear(requestDomicilio))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya tiene un despacho");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crear() lanza BusinessException cuando DOMICILIO no trae dirección")
    void crear_domicilioSinDireccion_lanzaBusiness() {
        // Given
        DespachoRequestDTO sinDireccion = new DespachoRequestDTO(
                52L, "DOMICILIO", null, "Santiago", null);
        when(repository.existsByIdPedido(52L)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> service.crear(sinDireccion))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("dirección es obligatoria");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crear() lanza BusinessException cuando DOMICILIO trae dirección en blanco")
    void crear_domicilioDireccionBlanca_lanzaBusiness() {
        // Given
        DespachoRequestDTO dirBlanca = new DespachoRequestDTO(
                52L, "DOMICILIO", "   ", "Santiago", null);
        when(repository.existsByIdPedido(52L)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> service.crear(dirBlanca))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("actualizar() aplica los cambios cuando el despacho existe")
    void actualizar_existente_actualiza() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(despacho));
        when(repository.save(despacho)).thenReturn(despacho);
        when(mapper.toResponse(despacho)).thenReturn(responseDTO);

        // When
        service.actualizar(1L, requestDomicilio);

        // Then
        verify(mapper).updateEntity(despacho, requestDomicilio);
        verify(repository).save(despacho);
    }

    @Test
    @DisplayName("cambiarEstado() registra fecha_entrega automáticamente al pasar a ENTREGADO")
    void cambiarEstado_entregado_fijaFechaEntrega() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(despacho));
        when(repository.save(any(Despacho.class))).thenReturn(despacho);
        when(mapper.toResponse(any(Despacho.class))).thenReturn(responseDTO);
        CambioEstadoDespachoDTO dto = new CambioEstadoDespachoDTO("ENTREGADO");

        // When
        service.cambiarEstado(1L, dto);

        // Then: fecha_entrega se establece automáticamente
        ArgumentCaptor<Despacho> captor = ArgumentCaptor.forClass(Despacho.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEstadoDespacho()).isEqualTo("ENTREGADO");
        assertThat(captor.getValue().getFechaEntrega()).isNotNull();
    }

    @Test
    @DisplayName("cambiarEstado() NO toca fecha_entrega para estados distintos de ENTREGADO")
    void cambiarEstado_enRuta_noTocaFechaEntrega() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(despacho));
        when(repository.save(any(Despacho.class))).thenReturn(despacho);
        when(mapper.toResponse(any(Despacho.class))).thenReturn(responseDTO);
        CambioEstadoDespachoDTO dto = new CambioEstadoDespachoDTO("EN_RUTA");

        // When
        service.cambiarEstado(1L, dto);

        // Then
        ArgumentCaptor<Despacho> captor = ArgumentCaptor.forClass(Despacho.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEstadoDespacho()).isEqualTo("EN_RUTA");
        assertThat(captor.getValue().getFechaEntrega()).isNull();
    }

    @Test
    @DisplayName("cambiarEstado() lanza ResourceNotFoundException cuando no existe")
    void cambiarEstado_inexistente_lanzaNotFound() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.cambiarEstado(99L, new CambioEstadoDespachoDTO("EN_RUTA")))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("eliminar() borra el despacho cuando existe")
    void eliminar_existente_borra() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(despacho));

        // When
        service.eliminar(1L);

        // Then
        verify(repository).delete(despacho);
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