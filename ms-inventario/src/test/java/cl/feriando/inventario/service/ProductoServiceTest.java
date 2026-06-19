package cl.feriando.inventario.service;

import cl.feriando.inventario.dto.AjusteStockDTO;
import cl.feriando.inventario.dto.InventarioRequestDTO;
import cl.feriando.inventario.dto.InventarioResponseDTO;
import cl.feriando.inventario.exception.BusinessException;
import cl.feriando.inventario.exception.ResourceNotFoundException;
import cl.feriando.inventario.mapper.InventarioMapper;
import cl.feriando.inventario.model.Inventario;
import cl.feriando.inventario.repository.InventarioRepository;

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
 * Pruebas unitarias de InventarioService.
 * Se enfocan en la logica de stock, que es la mas compleja:
 *  - descontar stock validando que haya suficiente,
 *  - activar la alerta cuando el stock cae al minimo,
 *  - reponer stock.
 * El repositorio y el mapper se mockean para aislar estas reglas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventarioService - reglas de negocio de stock")
class InventarioServiceTest {

    @Mock
    private InventarioRepository repository;

    @Mock
    private InventarioMapper mapper;

    @InjectMocks
    private InventarioService service;

    private Inventario inventario;
    private InventarioRequestDTO requestDTO;
    private InventarioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        inventario = new Inventario();
        inventario.setIdInventario(1L);
        inventario.setIdProducto(100L);
        inventario.setIdFeriante(10L);
        inventario.setStockDisponible(new BigDecimal("50.00"));
        inventario.setStockMinimo(new BigDecimal("10.00"));
        inventario.setAlertaActiva((short) 0);
        inventario.setUltimaActualizacion(LocalDateTime.now());

        requestDTO = new InventarioRequestDTO(
                100L, 10L, new BigDecimal("50.00"), new BigDecimal("10.00"));

        responseDTO = new InventarioResponseDTO(
                1L, 100L, 10L, new BigDecimal("50.00"), new BigDecimal("10.00"),
                (short) 0, inventario.getUltimaActualizacion());
    }

    @Test
    @DisplayName("listar() devuelve los inventarios mapeados")
    void listar_devuelveInventarios() {
        // Given
        when(repository.findAll()).thenReturn(List.of(inventario));
        when(mapper.toResponse(inventario)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listar()).containsExactly(responseDTO);
    }

    @Test
    @DisplayName("obtener() devuelve el inventario cuando existe")
    void obtener_existente_devuelveInventario() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(inventario));
        when(mapper.toResponse(inventario)).thenReturn(responseDTO);

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
    @DisplayName("obtenerPorProducto() devuelve el inventario del producto")
    void obtenerPorProducto_existente_devuelveInventario() {
        // Given
        when(repository.findByIdProducto(100L)).thenReturn(Optional.of(inventario));
        when(mapper.toResponse(inventario)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.obtenerPorProducto(100L)).isEqualTo(responseDTO);
    }

    @Test
    @DisplayName("obtenerPorProducto() lanza ResourceNotFoundException cuando no hay inventario")
    void obtenerPorProducto_inexistente_lanzaNotFound() {
        // Given
        when(repository.findByIdProducto(100L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.obtenerPorProducto(100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("crear() guarda el inventario cuando el producto no tiene uno")
    void crear_productoSinInventario_guarda() {
        // Given
        when(repository.existsByIdProducto(100L)).thenReturn(false);
        when(mapper.toEntity(requestDTO)).thenReturn(inventario);
        when(repository.save(inventario)).thenReturn(inventario);
        when(mapper.toResponse(inventario)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.crear(requestDTO)).isEqualTo(responseDTO);
        verify(repository).save(inventario);
    }

    @Test
    @DisplayName("crear() lanza BusinessException cuando el producto ya tiene inventario")
    void crear_productoConInventario_lanzaBusiness() {
        // Given
        when(repository.existsByIdProducto(100L)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.crear(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya tiene inventario");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() aplica los cambios cuando el inventario existe")
    void actualizar_existente_actualiza() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(inventario));
        when(repository.save(inventario)).thenReturn(inventario);
        when(mapper.toResponse(inventario)).thenReturn(responseDTO);

        // When
        InventarioResponseDTO resultado = service.actualizar(1L, requestDTO);

        // Then
        assertThat(resultado).isEqualTo(responseDTO);
        verify(mapper).updateEntity(inventario, requestDTO);
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
    @DisplayName("descontarStock() resta el stock y NO activa alerta si queda sobre el minimo")
    void descontarStock_suficiente_restaSinAlerta() {
        // Given: 50 disponible, minimo 10. Se descuentan 20 -> quedan 30 (> 10)
        when(repository.findByIdProducto(100L)).thenReturn(Optional.of(inventario));
        when(repository.save(any(Inventario.class))).thenReturn(inventario);
        when(mapper.toResponse(any(Inventario.class))).thenReturn(responseDTO);
        AjusteStockDTO ajuste = new AjusteStockDTO(new BigDecimal("20.00"));

        // When
        service.descontarStock(100L, ajuste);

        // Then
        ArgumentCaptor<Inventario> captor = ArgumentCaptor.forClass(Inventario.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStockDisponible()).isEqualByComparingTo("30.00");
        assertThat(captor.getValue().getAlertaActiva()).isEqualTo((short) 0);
    }

    @Test
    @DisplayName("descontarStock() activa la alerta cuando el stock cae al minimo o menos")
    void descontarStock_quedaBajoMinimo_activaAlerta() {
        // Given: 50 disponible, minimo 10. Se descuentan 45 -> quedan 5 (<= 10)
        when(repository.findByIdProducto(100L)).thenReturn(Optional.of(inventario));
        when(repository.save(any(Inventario.class))).thenReturn(inventario);
        when(mapper.toResponse(any(Inventario.class))).thenReturn(responseDTO);
        AjusteStockDTO ajuste = new AjusteStockDTO(new BigDecimal("45.00"));

        // When
        service.descontarStock(100L, ajuste);

        // Then
        ArgumentCaptor<Inventario> captor = ArgumentCaptor.forClass(Inventario.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStockDisponible()).isEqualByComparingTo("5.00");
        assertThat(captor.getValue().getAlertaActiva()).isEqualTo((short) 1);
    }

    @Test
    @DisplayName("descontarStock() lanza BusinessException cuando el stock es insuficiente")
    void descontarStock_insuficiente_lanzaBusiness() {
        // Given: 50 disponible. Se intentan descontar 60
        when(repository.findByIdProducto(100L)).thenReturn(Optional.of(inventario));
        AjusteStockDTO ajuste = new AjusteStockDTO(new BigDecimal("60.00"));

        // When / Then
        assertThatThrownBy(() -> service.descontarStock(100L, ajuste))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stock insuficiente");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("descontarStock() lanza ResourceNotFoundException cuando el producto no tiene inventario")
    void descontarStock_sinInventario_lanzaNotFound() {
        // Given
        when(repository.findByIdProducto(100L)).thenReturn(Optional.empty());
        AjusteStockDTO ajuste = new AjusteStockDTO(new BigDecimal("5.00"));

        // When / Then
        assertThatThrownBy(() -> service.descontarStock(100L, ajuste))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("reponerStock() suma el stock y desactiva la alerta al superar el minimo")
    void reponerStock_sumaStock() {
        // Given: arranca con alerta activa y poco stock
        inventario.setStockDisponible(new BigDecimal("5.00"));
        inventario.setAlertaActiva((short) 1);
        when(repository.findByIdProducto(100L)).thenReturn(Optional.of(inventario));
        when(repository.save(any(Inventario.class))).thenReturn(inventario);
        when(mapper.toResponse(any(Inventario.class))).thenReturn(responseDTO);
        AjusteStockDTO ajuste = new AjusteStockDTO(new BigDecimal("30.00"));

        // When: 5 + 30 = 35 (> 10) -> alerta se apaga
        service.reponerStock(100L, ajuste);

        // Then
        ArgumentCaptor<Inventario> captor = ArgumentCaptor.forClass(Inventario.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStockDisponible()).isEqualByComparingTo("35.00");
        assertThat(captor.getValue().getAlertaActiva()).isEqualTo((short) 0);
    }

    @Test
    @DisplayName("reponerStock() lanza ResourceNotFoundException cuando el producto no tiene inventario")
    void reponerStock_sinInventario_lanzaNotFound() {
        // Given
        when(repository.findByIdProducto(100L)).thenReturn(Optional.empty());
        AjusteStockDTO ajuste = new AjusteStockDTO(new BigDecimal("5.00"));

        // When / Then
        assertThatThrownBy(() -> service.reponerStock(100L, ajuste))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("eliminar() borra el inventario cuando existe")
    void eliminar_existente_borra() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(inventario));

        // When
        service.eliminar(1L);

        // Then
        verify(repository).delete(inventario);
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