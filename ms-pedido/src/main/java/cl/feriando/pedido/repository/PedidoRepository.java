package cl.feriando.pedido.repository;

import cl.feriando.pedido.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByIdCliente(Long idCliente);
    List<Pedido> findByIdFeriante(Long idFeriante);
}
