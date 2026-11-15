package ec.edu.parqueadero.exception;

import org.springframework.http.HttpStatus;

/** 401 - Lanzada por el interceptor cuando falta o es invalida la cabecera X-API-KEY. */
public class AccesoNoAutorizadoException extends ApiException {
    public AccesoNoAutorizadoException(String mensaje) {
        super(mensaje, HttpStatus.UNAUTHORIZED, "ACCESO_NO_AUTORIZADO");
    }
}
