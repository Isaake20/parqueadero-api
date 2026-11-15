package ec.edu.parqueadero.exception;

import org.springframework.http.HttpStatus;

/** 404 - El recurso solicitado no existe. */
public class RecursoNoEncontradoException extends ApiException {
    public RecursoNoEncontradoException(String recurso, String campo, Object valor) {
        super(String.format("%s con %s '%s' no existe", recurso, campo, valor), HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO");
    }
}
