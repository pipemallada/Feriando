package cl.feriando.despacho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
/**
 * DTO para crear o actualizar un despacho.
 * direccion y comuna son opcionales aqui porque para RETIRO no se necesitan.
 * para DOMICILIO los exige el service como regla de negocio.
 */
public record DespachoRequestDTO(

        @NotNull(message = "El id_pedido es obligatorio")
        @Positive
        Long idPedido,
        // @Pattern restringe los valores validos. cualquier otro String
        // hace fallar la validacion con 400 antes de llegar al service.
        @NotBlank(message = "El tipo de entrega es obligatorio")
        @Pattern(regexp = "RETIRO|DOMICILIO", message = "Debe ser RETIRO o DOMICILIO")
        String tipoEntrega,
         // opcional a nivel de formato; el service valida que sea obligatoria
        // cuando tipoEntrega = DOMICILIO.
        @Size(max = 255)
        String direccion,

        @Size(max = 100)
        String comuna,
     // opcional. el feriante puede estimarla despues de crear el despacho.
        LocalDateTime fechaEstimada
) { }
