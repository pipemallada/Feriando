package cl.feriando.feriante.repository;

import cl.feriando.feriante.model.Feriante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
/**
 * repositorio JPA del Feriante.
 * heredando JpaRepository ya tenemos CRUD basico; solo agregamos las
 * consultas especificas del dominio.
 */
@Repository
public interface FerianteRepository extends JpaRepository<Feriante, Long> {
    // sirve para validar que un usuario no tenga ya un perfil de feriante
    // antes de crear uno nuevo (regla: un usuario = un perfil).
    Optional<Feriante> findByIdUsuario(Long idUsuario);
}
