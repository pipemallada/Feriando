package cl.feriando.calificacion.repository;

import cl.feriando.calificacion.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    List<Calificacion> findByIdFeriante(Long idFeriante);

    boolean existsByIdPedido(Long idPedido);
}
