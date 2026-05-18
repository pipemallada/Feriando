package cl.feriando.carrito.repository;

import cl.feriando.carrito.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * repositorio del Carrito.
 * no definimos uno aparte para DetalleCarrito porque siempre lo manipulamos
 * a traves de su carrito padre (gracias a cascade=ALL en la entidad).
 */
@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    // para listar "mis carritos" del cliente.
    List<Carrito> findByIdCliente(Long idCliente);
}
