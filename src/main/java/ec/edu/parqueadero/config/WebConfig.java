package ec.edu.parqueadero.config;

import ec.edu.parqueadero.interceptor.ApiKeyInterceptor;
import ec.edu.parqueadero.interceptor.RequestTrackingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Registro de interceptores HTTP en el orden de ejecucion. */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestTrackingInterceptor trackingInterceptor;
    private final ApiKeyInterceptor apiKeyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(trackingInterceptor).addPathPatterns("/api/**").order(1);
        registry.addInterceptor(apiKeyInterceptor).addPathPatterns("/api/**").order(2);
    }
}
