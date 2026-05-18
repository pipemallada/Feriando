package cl.feriando.despacho.repository;

import cl.feriando.despacho.model.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
/**
 * repositorio del despacho.
 */
@Repository
public interface DespachoRepository extends JpaRepository<Despacho, Long> {
    // permite consultar "el despacho de tal pedido" sin saber su id_despacho.
    Optional<Despacho> findByIdPedido(Long idPedido);
    // existsBy<Campo> es mas eficiente que findBy<Campo>.isPresent().
    // lo usamos para validar la regla 1 pedido = 1 despacho.
    boolean existsByIdPedido(Long idPedido);
}
