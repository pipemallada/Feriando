package cl.feriando.calificacion.repository;

import cl.feriando.calificacion.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * repositorio de la calificacion.
 */
@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    // lo usamos para listar todas las opiniones del feriante X y para
    // recalcular su promedio cuando se crea o se borra una calificacion.
    List<Calificacion> findByIdFeriante(Long idFeriante);
    // para prevenir duplicados. un pedido sólo puede calificarse una vez.
    boolean existsByIdPedido(Long idPedido);
}
