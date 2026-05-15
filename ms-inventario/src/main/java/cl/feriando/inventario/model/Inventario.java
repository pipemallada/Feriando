package cl.feriando.inventario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Long idInventario;

    @Column(name = "id_producto", nullable = false, unique = true)
    private Long idProducto;

    @Column(name = "id_feriante", nullable = false)
    private Long idFeriante;

    @Column(name = "stock_disponible", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockDisponible;

    @Column(name = "stock_minimo", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockMinimo;

    @Column(name = "alerta_activa", nullable = false)
    private Short alertaActiva;

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;

    public Inventario() { }

    public Long getIdInventario() { return idInventario; }
    public void setIdInventario(Long idInventario) { this.idInventario = idInventario; }

    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }

    public Long getIdFeriante() { return idFeriante; }
    public void setIdFeriante(Long idFeriante) { this.idFeriante = idFeriante; }

    public BigDecimal getStockDisponible() { return stockDisponible; }
    public void setStockDisponible(BigDecimal stockDisponible) { this.stockDisponible = stockDisponible; }

    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo; }

    public Short getAlertaActiva() { return alertaActiva; }
    public void setAlertaActiva(Short alertaActiva) { this.alertaActiva = alertaActiva; }

    public LocalDateTime getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }
}
