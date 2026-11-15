package ec.edu.parqueadero.exception;

import org.springframework.http.HttpStatus;

/** 422 - La operacion es sintacticamente valida pero viola una regla de negocio. */
public class ReglaNegocioException extends ApiException {
    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje, HttpStatus.UNPROCESSABLE_ENTITY, codigo);
    }
}
