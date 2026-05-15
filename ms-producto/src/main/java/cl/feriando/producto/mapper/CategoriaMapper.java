package cl.feriando.producto.mapper;

import cl.feriando.producto.dto.CategoriaRequestDTO;
import cl.feriando.producto.dto.CategoriaResponseDTO;
import cl.feriando.producto.model.Categoria;

import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public Categoria toEntity(CategoriaRequestDTO dto) {
        Categoria c = new Categoria();
        c.setNombre(dto.nombre());
        return c;
    }

    public CategoriaResponseDTO toResponse(Categoria c) {
        return new CategoriaResponseDTO(c.getIdCategoria(), c.getNombre());
    }

    public void updateEntity(Categoria c, CategoriaRequestDTO dto) {
        c.setNombre(dto.nombre());
    }
}
