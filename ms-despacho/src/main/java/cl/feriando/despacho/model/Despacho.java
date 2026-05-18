package cl.feriando.despacho.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
/**
 * entidad JPA del despacho.
 * regla clave: un pedido tiene UN despacho (relacion 1:1).
 * por eso id_pedido tiene unique=true: la BD nos protege contra duplicar
 * el despacho de un mismo pedido por error.
 */
@Entity
@Table(name = "despachos")
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_despacho")
    private Long idDespacho;
    // unique=true: garantiza 1 despacho por pedido.
    @Column(name = "id_pedido", nullable = false, unique = true)
    private Long idPedido;
    // retiro o domicilio (validado en el DTO con @Pattern).
    @Column(name = "tipo_entrega", nullable = false, length = 20)
    private String tipoEntrega;
    // opcionales para retiro; obligatorios para domicilio (validado en service).
    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "comuna", length = 100)
    private String comuna;
    // pendiente, en_ruta, entregado, cancelado (validado en el DTO).
    @Column(name = "estado_despacho", nullable = false, length = 20)
    private String estadoDespacho;
    // fecha planificada de entrega. opcional al crear (puede definirse despues).
    @Column(name = "fecha_estimada")
    private LocalDateTime fechaEstimada;
    // fecha real de entrega. se llena automaticamente al cambiar el estado
    // a entregado (logica en el service).
    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    public Despacho() { }

    public Long getIdDespacho() { return idDespacho; }
    public void setIdDespacho(Long idDespacho) { this.idDespacho = idDespacho; }

    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }

    public String getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(String tipoEntrega) { this.tipoEntrega = tipoEntrega; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public String getEstadoDespacho() { return estadoDespacho; }
    public void setEstadoDespacho(String estadoDespacho) { this.estadoDespacho = estadoDespacho; }

    public LocalDateTime getFechaEstimada() { return fechaEstimada; }
    public void setFechaEstimada(LocalDateTime fechaEstimada) { this.fechaEstimada = fechaEstimada; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
}
