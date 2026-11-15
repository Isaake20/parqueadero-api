package ec.edu.parqueadero.interceptor;

import ec.edu.parqueadero.exception.AccesoNoAutorizadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Interceptor 2 - Control basico de acceso mediante validacion de cabecera.
 * Las operaciones de escritura (POST, PUT, PATCH, DELETE) exigen la cabecera X-API-KEY;
 * las consultas GET son publicas.
 */
@Slf4j
@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    public static final String HEADER_API_KEY = "X-API-KEY";
    private static final Set<String> METODOS_PROTEGIDOS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final String apiKey;

    public ApiKeyInterceptor(@Value("${app.security.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!METODOS_PROTEGIDOS.contains(request.getMethod())) return true;
        String recibida = request.getHeader(HEADER_API_KEY);
        if (recibida == null || recibida.isBlank()) {
            log.warn("Acceso denegado a {} {}: falta cabecera {}", request.getMethod(), request.getRequestURI(), HEADER_API_KEY);
            throw new AccesoNoAutorizadoException("Falta la cabecera " + HEADER_API_KEY + " requerida para operaciones de escritura");
        }
        if (!apiKey.equals(recibida)) {
            log.warn("Acceso denegado a {} {}: API key invalida", request.getMethod(), request.getRequestURI());
            throw new AccesoNoAutorizadoException("La cabecera " + HEADER_API_KEY + " no es valida");
        }
        return true;
    }
}
