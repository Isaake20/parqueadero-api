package ec.edu.parqueadero.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReporteIngresosResponse(LocalDateTime desde, LocalDateTime hasta, int totalRegistros,
                                      BigDecimal totalRecaudado, String generadoPorHilo,
                                      List<RegistroResponse> registros) {}
