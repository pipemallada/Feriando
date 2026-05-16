package cl.feriando.reporte.repository;

import cl.feriando.reporte.model.ReporteVentas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteVentasRepository extends JpaRepository<ReporteVentas, Long> {

    List<ReporteVentas> findByIdFeriante(Long idFeriante);
}
