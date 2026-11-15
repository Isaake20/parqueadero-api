package ec.edu.parqueadero.aspect;

import ec.edu.parqueadero.entity.AuditoriaOperacion;
import ec.edu.parqueadero.interceptor.RequestTrackingInterceptor;
import ec.edu.parqueadero.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Aspecto 2 - Auditoria: persiste en BD cada operacion anotada con @Auditable,
 * tanto si termina con exito como si lanza una excepcion (registro de errores).
 * Order(1) lo ubica por fuera de la transaccion del servicio.
 */
@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class AuditoriaAspect {

    private final AuditoriaService auditoriaService;

    @Around("@annotation(auditable)")
    public Object auditar(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        long inicio = System.nanoTime();
        String metodo = pjp.getSignature().getDeclaringType().getSimpleName() + "." + pjp.getSignature().getName();
        String args = recortar(Arrays.toString(pjp.getArgs()), 500);
        try {
            Object resultado = pjp.proceed();
            guardar(auditable.operacion(), metodo, args, "EXITO", null, inicio);
            return resultado;
        } catch (Throwable ex) {
            guardar(auditable.operacion(), metodo, args, "ERROR", ex.getClass().getSimpleName() + ": " + ex.getMessage(), inicio);
            throw ex;
        }
    }

    private void guardar(String operacion, String metodo, String args, String resultado, String error, long inicio) {
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        try {
            auditoriaService.registrar(AuditoriaOperacion.builder()
                    .operacion(operacion).metodo(metodo).argumentos(args).resultado(resultado)
                    .mensajeError(recortar(error, 300)).duracionMs(ms)
                    .requestId(MDC.get(RequestTrackingInterceptor.MDC_REQUEST_ID))
                    .fecha(LocalDateTime.now()).build());
            log.info("[AOP-AUDITORIA] {} -> {} ({} ms)", operacion, resultado, ms);
        } catch (Exception e) {
            // La auditoria nunca debe romper la operacion de negocio
            log.error("[AOP-AUDITORIA] No se pudo registrar la auditoria de {}: {}", operacion, e.getMessage());
        }
    }

    private static String recortar(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
