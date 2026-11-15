package ec.edu.parqueadero.service;

import ec.edu.parqueadero.dto.ReporteIngresosResponse;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public interface ReporteService {
    /** Version bloqueante: el hilo HTTP espera la consulta y el armado del reporte. */
    ReporteIngresosResponse generarReporte(LocalDateTime desde, LocalDateTime hasta);
    /** Version asincrona: el trabajo se delega al pool 'tareasExecutor' y se libera el hilo HTTP. */
    CompletableFuture<ReporteIngresosResponse> generarReporteAsync(LocalDateTime desde, LocalDateTime hasta);
}
