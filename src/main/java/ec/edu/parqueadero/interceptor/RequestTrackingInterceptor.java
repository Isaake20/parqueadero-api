package ec.edu.parqueadero.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.util.UUID;

/**
 * Interceptor 1 - Registro de solicitudes, informacion del cliente y medicion de tiempo.
 * Asigna un identificador unico a cada peticion (cabecera X-Request-Id) y registra
 * metodo, URI, IP, User-Agent, codigo de estado y duracion total.
 */
@Slf4j
@Component
public class RequestTrackingInterceptor implements AsyncHandlerInterceptor {

    public static final String ATTR_INICIO = "tracking.inicio";
    public static final String ATTR_REQUEST_ID = "tracking.requestId";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String MDC_REQUEST_ID = "requestId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // En peticiones asincronas preHandle se invoca de nuevo en el redespacho: se conserva el inicio original
        if (request.getAttribute(ATTR_INICIO) == null) {
            String requestId = request.getHeader(HEADER_REQUEST_ID);
            if (requestId == null || requestId.isBlank()) requestId = UUID.randomUUID().toString().substring(0, 8);
            request.setAttribute(ATTR_INICIO, System.nanoTime());
            request.setAttribute(ATTR_REQUEST_ID, requestId);
            response.setHeader(HEADER_REQUEST_ID, requestId);
            log.info("--> {} {}{} | cliente={} | agente={} | requestId={}", request.getMethod(), request.getRequestURI(),
                    request.getQueryString() == null ? "" : "?" + request.getQueryString(),
                    obtenerIpCliente(request), request.getHeader("User-Agent"), requestId);
        }
        MDC.put(MDC_REQUEST_ID, String.valueOf(request.getAttribute(ATTR_REQUEST_ID)));
        return true;
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("... {} {} liberando hilo {} (procesamiento asincrono)", request.getMethod(),
                request.getRequestURI(), Thread.currentThread().getName());
        MDC.remove(MDC_REQUEST_ID);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.info("<-- {} {} | estado={} | duracion={} ms | requestId={}", request.getMethod(), request.getRequestURI(),
                response.getStatus(), duracionMs(request), request.getAttribute(ATTR_REQUEST_ID));
        MDC.remove(MDC_REQUEST_ID);
    }

    public static long duracionMs(HttpServletRequest request) {
        Object inicio = request.getAttribute(ATTR_INICIO);
        return inicio == null ? -1 : (System.nanoTime() - (Long) inicio) / 1_000_000;
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
