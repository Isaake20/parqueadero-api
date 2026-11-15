package ec.edu.parqueadero.service.impl;

import ec.edu.parqueadero.dto.*;
import ec.edu.parqueadero.entity.EstadoRegistro;
import ec.edu.parqueadero.exception.ReglaNegocioException;
import ec.edu.parqueadero.repository.RegistroEstacionamientoRepository;
import ec.edu.parqueadero.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final RegistroEstacionamientoRepository registroRepository;

    @Override
    public ReporteIngresosResponse generarReporte(LocalDateTime desde, LocalDateTime hasta) {
        if (desde.isAfter(hasta))
            throw new ReglaNegocioException("RANGO_INVALIDO", "La fecha 'desde' no puede ser posterior a 'hasta'");
        List<RegistroResponse> registros = registroRepository
                .finalizadosEntre(EstadoRegistro.FINALIZADO, desde, hasta)
                .stream().map(DtoMapper::toResponse).toList();
        return new ReporteIngresosResponse(desde, hasta, registros.size(),
                registroRepository.totalRecaudado(desde, hasta), Thread.currentThread().getName(), registros);
    }

    @Override
    @Async("tareasExecutor")
    public CompletableFuture<ReporteIngresosResponse> generarReporteAsync(LocalDateTime desde, LocalDateTime hasta) {
        return CompletableFuture.completedFuture(generarReporte(desde, hasta));
    }
}
