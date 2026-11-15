package ec.edu.parqueadero.controller;

import ec.edu.parqueadero.dto.ReporteIngresosResponse;
import ec.edu.parqueadero.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    /** Bloqueante: el hilo de Tomcat queda ocupado hasta terminar la consulta. */
    @GetMapping("/ingresos")
    public ReporteIngresosResponse ingresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return reporteService.generarReporte(desde, hasta);
    }

    /** Asincrono: Spring MVC libera el hilo de Tomcat y responde cuando el CompletableFuture termina. */
    @GetMapping("/ingresos/async")
    public CompletableFuture<ReporteIngresosResponse> ingresosAsync(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return reporteService.generarReporteAsync(desde, hasta);
    }
}
