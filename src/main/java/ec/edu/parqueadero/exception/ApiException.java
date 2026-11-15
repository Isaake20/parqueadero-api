package ec.edu.parqueadero.exception;

import org.springframework.http.HttpStatus;

/** Base de las excepciones de dominio: cada una conoce su codigo HTTP y un codigo funcional. */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String codigo;

    protected ApiException(String mensaje, HttpStatus status, String codigo) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
    }

    public HttpStatus getStatus() { return status; }

    public String getCodigo() { return codigo; }
}
