package cl.feriando.reporte.service;

import cl.feriando.reporte.client.PedidoClient;
import cl.feriando.reporte.dto.PedidoResumenDTO;
import cl.feriando.reporte.dto.ReporteVentasRequestDTO;
import cl.feriando.reporte.dto.ReporteVentasResponseDTO;
import cl.feriando.reporte.exception.ResourceNotFoundException;
import cl.feriando.reporte.mapper.ReporteVentasMapper;
import cl.feriando.reporte.model.ReporteVentas;
import cl.feriando.reporte.repository.ReporteVentasRepository;

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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de ReporteService.
 * Reglas de negocio que se validan:
 *  - generarParaFeriante() suma SOLO los pedidos en estado PAGADO/ENTREGADO/LISTO/PREPARANDO.
 *  - generarParaFeriante() calcula productoTop como el producto con mayor cantidad total.
 *  - generarParaFeriante() devuelve totalVentas=0 y productoTop=null si no hay pedidos válidos.
 *  - El reporte se persiste con el período en formato yyyy-MM.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReporteService - reglas de negocio de reportes de ventas")
class ReporteServiceTest {

    @Mock private ReporteVentasRepository repository;
    @Mock private ReporteVentasMapper mapper;
    @Mock private PedidoClient pedidoClient;
    @InjectMocks private ReporteService service;

    private ReporteVentas reporte;
    private ReporteVentasRequestDTO requestDTO;
    private ReporteVentasResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        reporte = new ReporteVentas();
        reporte.setIdReporte(1L);
        reporte.setIdFeriante(20L);
        reporte.setPeriodo("2026-06");
        reporte.setTotalVentas(new BigDecimal("50000.00"));
        reporte.setTotalPedidos(3);
        reporte.setProductoTop("Producto 100");
        reporte.setGeneradoEn(LocalDateTime.now());

        requestDTO = new ReporteVentasRequestDTO(
                20L, "2026-06", new BigDecimal("50000.00"), 3, "Producto 100");

        responseDTO = new ReporteVentasResponseDTO(
                1L, 20L, "2026-06", new BigDecimal("50000.00"), 3, "Producto 100", reporte.getGeneradoEn());
    }

    @Test
    @DisplayName("listar() devuelve todos los reportes mapeados")
    void listar_devuelveReportes() {
        // Given
        when(repository.findAll()).thenReturn(List.of(reporte));
        when(mapper.toResponse(reporte)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listar()).containsExactly(responseDTO);
    }

    @Test
    @DisplayName("listarPorFeriante() filtra por el id del feriante")
    void listarPorFeriante_devuelveReportesDelFeriante() {
        // Given
        when(repository.findByIdFeriante(20L)).thenReturn(List.of(reporte));
        when(mapper.toResponse(reporte)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.listarPorFeriante(20L)).containsExactly(responseDTO);
        verify(repository).findByIdFeriante(20L);
    }

    @Test
    @DisplayName("obtener() devuelve el reporte cuando existe")
    void obtener_existente_devuelveReporte() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(reporte));
        when(mapper.toResponse(reporte)).thenReturn(responseDTO);

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
    @DisplayName("crear() guarda el reporte manual")
    void crear_guardaReporte() {
        // Given
        when(mapper.toEntity(requestDTO)).thenReturn(reporte);
        when(repository.save(reporte)).thenReturn(reporte);
        when(mapper.toResponse(reporte)).thenReturn(responseDTO);

        // When / Then
        assertThat(service.crear(requestDTO)).isEqualTo(responseDTO);
        verify(repository).save(reporte);
    }

    @Test
    @DisplayName("generarParaFeriante() suma solo pedidos en estados contables (PAGADO/ENTREGADO/LISTO/PREPARANDO)")
    void generarParaFeriante_sumaEstadosContables() {
        // Given: 2 pedidos válidos (PAGADO, ENTREGADO) y 1 ignorado (CANCELADO)
        PedidoResumenDTO.DetalleResumenDTO det = new PedidoResumenDTO.DetalleResumenDTO(100L, new BigDecimal("3"));
        PedidoResumenDTO pagado = new PedidoResumenDTO(
                1L, 20L, "PAGADO", new BigDecimal("20000.00"), LocalDateTime.now(), List.of(det));
        PedidoResumenDTO entregado = new PedidoResumenDTO(
                2L, 20L, "ENTREGADO", new BigDecimal("15000.00"), LocalDateTime.now(), List.of(det));
        PedidoResumenDTO cancelado = new PedidoResumenDTO(
                3L, 20L, "CANCELADO", new BigDecimal("9000.00"), LocalDateTime.now(), List.of());

        when(pedidoClient.listarPorFeriante(20L)).thenReturn(List.of(pagado, entregado, cancelado));
        when(repository.save(any(ReporteVentas.class))).thenReturn(reporte);
        when(mapper.toResponse(reporte)).thenReturn(responseDTO);

        // When
        service.generarParaFeriante(20L);

        // Then: solo suma PAGADO + ENTREGADO = 35000, no incluye CANCELADO
        ArgumentCaptor<ReporteVentas> captor = ArgumentCaptor.forClass(ReporteVentas.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTotalVentas()).isEqualByComparingTo("35000.00");
        // totalPedidos incluye TODOS los pedidos del feriante (no solo los contables)
        assertThat(captor.getValue().getTotalPedidos()).isEqualTo(3);
    }

    @Test
    @DisplayName("generarParaFeriante() calcula el producto con mayor cantidad como productoTop")
    void generarParaFeriante_calculaProductoTop() {
        // Given: producto 200 tiene cantidad total 10 (5+5) > producto 100 con 3
        PedidoResumenDTO.DetalleResumenDTO det100 = new PedidoResumenDTO.DetalleResumenDTO(100L, new BigDecimal("3"));
        PedidoResumenDTO.DetalleResumenDTO det200a = new PedidoResumenDTO.DetalleResumenDTO(200L, new BigDecimal("5"));
        PedidoResumenDTO.DetalleResumenDTO det200b = new PedidoResumenDTO.DetalleResumenDTO(200L, new BigDecimal("5"));

        PedidoResumenDTO p1 = new PedidoResumenDTO(
                1L, 20L, "PAGADO", new BigDecimal("10000.00"), LocalDateTime.now(),
                List.of(det100, det200a));
        PedidoResumenDTO p2 = new PedidoResumenDTO(
                2L, 20L, "ENTREGADO", new BigDecimal("5000.00"), LocalDateTime.now(),
                List.of(det200b));

        when(pedidoClient.listarPorFeriante(20L)).thenReturn(List.of(p1, p2));
        when(repository.save(any(ReporteVentas.class))).thenReturn(reporte);
        when(mapper.toResponse(reporte)).thenReturn(responseDTO);

        // When
        service.generarParaFeriante(20L);

        // Then: producto 200 sumó 10, producto 100 sumó 3 -> top = "Producto 200"
        ArgumentCaptor<ReporteVentas> captor = ArgumentCaptor.forClass(ReporteVentas.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getProductoTop()).isEqualTo("Producto 200");
    }

    @Test
    @DisplayName("generarParaFeriante() genera reporte vacío cuando ms-pedido no devuelve pedidos")
    void generarParaFeriante_sinPedidos_reporteVacio() {
        // Given
        when(pedidoClient.listarPorFeriante(20L)).thenReturn(Collections.emptyList());
        when(repository.save(any(ReporteVentas.class))).thenReturn(reporte);
        when(mapper.toResponse(reporte)).thenReturn(responseDTO);

        // When
        service.generarParaFeriante(20L);

        // Then: totalVentas=0, totalPedidos=0, productoTop=null
        ArgumentCaptor<ReporteVentas> captor = ArgumentCaptor.forClass(ReporteVentas.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTotalVentas()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(captor.getValue().getTotalPedidos()).isEqualTo(0);
        assertThat(captor.getValue().getProductoTop()).isNull();
    }

    @Test
    @DisplayName("generarParaFeriante() persiste el período en formato yyyy-MM")
    void generarParaFeriante_guardaPeriodoEnFormatoCorrecto() {
        // Given
        when(pedidoClient.listarPorFeriante(20L)).thenReturn(Collections.emptyList());
        when(repository.save(any(ReporteVentas.class))).thenReturn(reporte);
        when(mapper.toResponse(reporte)).thenReturn(responseDTO);

        // When
        service.generarParaFeriante(20L);

        // Then: el período debe coincidir con el patrón yyyy-MM
        ArgumentCaptor<ReporteVentas> captor = ArgumentCaptor.forClass(ReporteVentas.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPeriodo()).matches("\\d{4}-\\d{2}");
    }

    @Test
    @DisplayName("eliminar() borra el reporte cuando existe")
    void eliminar_existente_borra() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(reporte));

        // When
        service.eliminar(1L);

        // Then
        verify(repository).delete(reporte);
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