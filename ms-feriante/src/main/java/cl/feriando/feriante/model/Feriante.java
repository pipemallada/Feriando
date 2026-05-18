package cl.feriando.feriante.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
/**
 * entidad JPA que mapea feriante_schema.feriantes.
 * usuario vive en otro microservicio (ms-usuario) y en OTRO schema. cruzar
 *   schemas con FK fisicas rompe la independencia de microservicios.
 * aqui id_usuario es una FK *logica*: la validamos llamando via WebClient
 *   a ms-usuario (ver UsuarioClient).
 */
@Entity
@Table(name = "feriantes")
public class Feriante {
    // PK auto-generada por la secuencia BIGSERIAL del schema.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feriante")
    private Long idFeriante;
    // referencia logica al usuario dueño del perfil. no es FK fisica.
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;
    // nombre visible del puesto en la feria (ej. "Frutas Don Pepe").
    @Column(name = "nombre_puesto", nullable = false, length = 150)
    private String nombrePuesto;
    // columnDefinition = "TEXT" porque la descripcion puede ser larga (CLOB
    // en Oracle / TEXT en Postgres).
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "telefono", length = 20)
    private String telefono;
    // precision=3, scale=2 -> formato 9.99 (rango 0.00 a 5.00).
    // BigDecimal y no Double para evitar errores de coma flotante.
    @Column(name = "calificacion_prom", precision = 3, scale = 2)
    private BigDecimal calificacionProm;
    // Soft delete (1 activo / 0 desactivado). igual que en Usuario.
    @Column(name = "activo", nullable = false)
    private Short activo;
    // constructor sin args requerido por JPA.
    public Feriante() { }
    // getters/setters explicitos para evitar dependencias adicionales (Lombok).
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
