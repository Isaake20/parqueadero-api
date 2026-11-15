package ec.edu.parqueadero.dto;

import java.util.Map;

public record DisponibilidadResponse(long totalEspacios, long disponibles, long ocupados, long enMantenimiento,
                                     double porcentajeOcupacion, Map<String, Map<String, Long>> detallePorTipo) {}
