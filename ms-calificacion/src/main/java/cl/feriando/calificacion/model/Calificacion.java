package cl.feriando.calificacion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * entidad JPA de la calificacion.
 * regla 1 a 1 entre calificacion y pedido. un cliente sólo puede calificar
 * cada pedido UNA vez.
 */
@Entity
@Table(name = "calificaciones")
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_calificacion")
    private Long idCalificacion;

    // unique=true: un pedido = una sola calificacion.
    @Column(name = "id_pedido", nullable = false, unique = true)
    private Long idPedido;
    // quién califico.
    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;
    // a quien se califico.
    @Column(name = "id_feriante", nullable = false)
    private Long idFeriante;
    // short en Java = SMALLINT en Postgres. Rango efectivo 1..5 validado en el DTO.
    @Column(name = "puntaje", nullable = false)
    private Short puntaje;
    // comentario libre, opcional. TEXT para no limitar el largo.
    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    public Calificacion() { }

    public Long getIdCalificacion() { return idCalificacion; }
    public void setIdCalificacion(Long idCalificacion) { this.idCalificacion = idCalificacion; }

    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public Long getIdFeriante() { return idFeriante; }
    public void setIdFeriante(Long idFeriante) { this.idFeriante = idFeriante; }

    public Short getPuntaje() { return puntaje; }
    public void setPuntaje(Short puntaje) { this.puntaje = puntaje; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
