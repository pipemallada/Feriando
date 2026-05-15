package cl.feriando.feriante.repository;

import cl.feriando.feriante.model.Feriante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FerianteRepository extends JpaRepository<Feriante, Long> {

    Optional<Feriante> findByIdUsuario(Long idUsuario);
}
