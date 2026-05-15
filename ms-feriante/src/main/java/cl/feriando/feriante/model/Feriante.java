package cl.feriando.feriante.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "feriantes")
public class Feriante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feriante")
    private Long idFeriante;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "nombre_puesto", nullable = false, length = 150)
    private String nombrePuesto;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "calificacion_prom", precision = 3, scale = 2)
    private BigDecimal calificacionProm;

    @Column(name = "activo", nullable = false)
    private Short activo;

    public Feriante() { }

    public Long getIdFeriante() { return idFeriante; }
    public void setIdFeriante(Long idFeriante) { this.idFeriante = idFeriante; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombrePuesto() { return nombrePuesto; }
    public void setNombrePuesto(String nombrePuesto) { this.nombrePuesto = nombrePuesto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public BigDecimal getCalificacionProm() { return calificacionProm; }
    public void setCalificacionProm(BigDecimal calificacionProm) { this.calificacionProm = calificacionProm; }

    public Short getActivo() { return activo; }
    public void setActivo(Short activo) { this.activo = activo; }
}
