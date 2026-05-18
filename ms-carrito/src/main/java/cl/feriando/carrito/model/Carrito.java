package cl.feriando.carrito.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * entidad JPA Carrito (cabecera).
 * relacion 1:N con DetalleCarrito: un carrito contiene muchos detalles
 * (los items). configuramos cascade=ALL + orphanRemoval para que al borrar
 * el carrito se borren sus detalles, y al sacar un detalle de la lista
 * Hibernate lo elimine de la BD automaticamente.
 */
@Entity
@Table(name = "carritos")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito")
    private Long idCarrito;
    // referencia logica al cliente (vive en ms-usuario).
    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;
    // activo (cliente lo esta editando) o cerrado (ya se convirtio en pedido).
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;
    // updatable=false: la fecha de creacion nunca cambia tras el insert.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * relación @OneToMany con DetalleCarrito.
     *
     * mappedBy = carrito -> el dueño de la FK es DetalleCarrito.
     * cascade = ALL      -> al persistir/borrar el carrito, tambien sus detalles.
     * orphanRemoval = true -> al sacar un detalle de la lista, hibernate
     *                           lo borra de la BD (esencial para quitar
     *                           item del carrito).
     *fetch = EAGER        -> casi siempre que mostramos un carrito
     *                           queremos sus detalles, asi nos ahorramos
     *                           el LazyInitializationException.
     */
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetalleCarrito> detalles = new ArrayList<>();

    public Carrito() { }
    // Accesores explicitos.
    public Long getIdCarrito() { return idCarrito; }
    public void setIdCarrito(Long idCarrito) { this.idCarrito = idCarrito; }

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<DetalleCarrito> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCarrito> detalles) { this.detalles = detalles; }
}
