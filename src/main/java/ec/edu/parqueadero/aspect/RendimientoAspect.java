package ec.edu.parqueadero.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Aspecto 1 - Monitoreo: registra cada llamada a la capa de servicios y su tiempo de ejecucion. */
@Slf4j
@Aspect
@Component
@Order(2)
public class RendimientoAspect {

    private static final long UMBRAL_LENTO_MS = 500;

    @Pointcut("execution(public * ec.edu.parqueadero.service.impl.*ServiceImpl.*(..))")
    public void capaServicios() {}

    @Around("capaServicios()")
    public Object medir(ProceedingJoinPoint pjp) throws Throwable {
        String metodo = pjp.getSignature().getDeclaringType().getSimpleName() + "." + pjp.getSignature().getName();
        long inicio = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            if (ms > UMBRAL_LENTO_MS) log.warn("[AOP-RENDIMIENTO] {} tardo {} ms (LENTO)", metodo, ms);
            else log.info("[AOP-RENDIMIENTO] {} ejecutado en {} ms", metodo, ms);
        }
    }
}
