package ec.edu.parqueadero.exception;

import org.springframework.http.HttpStatus;

/** 409 - El espacio solicitado esta ocupado, en mantenimiento o no hay espacios libres. */
public class EspacioNoDisponibleException extends ApiException {
    public EspacioNoDisponibleException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT, "ESPACIO_NO_DISPONIBLE");
    }
}
