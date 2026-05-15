package cl.feriando.producto.mapper;

import cl.feriando.producto.dto.ProductoRequestDTO;
import cl.feriando.producto.dto.ProductoResponseDTO;
import cl.feriando.producto.model.Categoria;
import cl.feriando.producto.model.Producto;

import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public Producto toEntity(ProductoRequestDTO dto, Categoria categoria) {
        Producto p = new Producto();
        p.setIdFeriante(dto.idFeriante());
        p.setCategoria(categoria);
        p.setNombre(dto.nombre());
        p.setDescripcion(dto.descripcion());
        p.setPrecio(dto.precio());
        p.setUnidad(dto.unidad());
        p.setActivo((short) 1);
        return p;
    }

    public ProductoResponseDTO toResponse(Producto p) {
        return new ProductoResponseDTO(
                p.getIdProducto(),
                p.getIdFeriante(),
                p.getCategoria().getIdCategoria(),
                p.getCategoria().getNombre(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getUnidad(),
                p.getActivo()
        );
    }

    public void updateEntity(Producto p, ProductoRequestDTO dto, Categoria categoria) {
        p.setIdFeriante(dto.idFeriante());
        p.setCategoria(categoria);
        p.setNombre(dto.nombre());
        p.setDescripcion(dto.descripcion());
        p.setPrecio(dto.precio());
        p.setUnidad(dto.unidad());
    }
}
