package cl.feriando.pago.repository;

import cl.feriando.pago.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByIdPedido(Long idPedido);
    boolean existsByIdPedido(Long idPedido);
}
