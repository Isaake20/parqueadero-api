package ec.edu.parqueadero.service.impl;

import ec.edu.parqueadero.dto.RegistroResponse;
import ec.edu.parqueadero.service.NotificacionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Simula el envio del comprobante de pago por correo electronico. En un sistema real
 * esta operacion depende de un servidor SMTP externo y puede tardar varios segundos.
 */
@Slf4j
@Service
public class NotificacionServiceImpl implements NotificacionService {

    private final long retardoMs;

    public NotificacionServiceImpl(@Value("${app.notificaciones.retardo-ms:2000}") long retardoMs) {
        this.retardoMs = retardoMs;
    }

    @Override
    public void enviarComprobante(RegistroResponse r) {
        log.info("[BLOQUEANTE] Enviando comprobante del registro {} en hilo {}", r.id(), Thread.currentThread().getName());
        simularEnvio(r);
    }

    @Override
    @Async("tareasExecutor")
    public CompletableFuture<Void> enviarComprobanteAsync(RegistroResponse r) {
        log.info("[ASINCRONO] Enviando comprobante del registro {} en hilo {}", r.id(), Thread.currentThread().getName());
        simularEnvio(r);
        return CompletableFuture.completedFuture(null);
    }

    private void simularEnvio(RegistroResponse r) {
        try {
            Thread.sleep(retardoMs); // latencia simulada del servidor de correo
            log.info("Comprobante enviado: placa={} monto=${} tiempo={}", r.placa(), r.montoCobrado(), r.tiempoPermanencia());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Envio de comprobante interrumpido para registro {}", r.id());
        }
    }
}
