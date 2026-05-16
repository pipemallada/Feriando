package cl.feriando.despacho.repository;

import cl.feriando.despacho.model.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DespachoRepository extends JpaRepository<Despacho, Long> {
    Optional<Despacho> findByIdPedido(Long idPedido);
    boolean existsByIdPedido(Long idPedido);
}
