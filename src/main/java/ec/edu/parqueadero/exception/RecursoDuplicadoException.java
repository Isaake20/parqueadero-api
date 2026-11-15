package ec.edu.parqueadero.exception;

import org.springframework.http.HttpStatus;

/** 409 - Se intenta registrar un valor unico que ya existe. */
public class RecursoDuplicadoException extends ApiException {
    public RecursoDuplicadoException(String recurso, String campo, Object valor) {
        super(String.format("Ya existe un %s con %s '%s'", recurso, campo, valor), HttpStatus.CONFLICT, "RECURSO_DUPLICADO");
    }
}
