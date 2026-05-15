package cl.feriando.inventario.repository;

import cl.feriando.inventario.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByIdProducto(Long idProducto);

    List<Inventario> findByIdFeriante(Long idFeriante);

    boolean existsByIdProducto(Long idProducto);
}
