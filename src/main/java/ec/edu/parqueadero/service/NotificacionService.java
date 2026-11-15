package ec.edu.parqueadero.service;

import ec.edu.parqueadero.dto.RegistroResponse;
import java.util.concurrent.CompletableFuture;

public interface NotificacionService {
    /** Simula el envio del comprobante por correo (I/O lento) bloqueando al llamador. */
    void enviarComprobante(RegistroResponse registro);
    /** Mismo envio pero ejecutado en un hilo del pool asincrono. */
    CompletableFuture<Void> enviarComprobanteAsync(RegistroResponse registro);
}
