package ec.edu.parqueadero.config;

import ec.edu.parqueadero.entity.EstadoEspacio;
import ec.edu.parqueadero.entity.Espacio;
import ec.edu.parqueadero.entity.TipoVehiculo;
import ec.edu.parqueadero.repository.EspacioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Carga los espacios fisicos iniciales si la tabla esta vacia (no se ejecuta en pruebas). */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EspacioRepository espacioRepository;

    @Override
    public void run(String... args) {
        if (espacioRepository.count() > 0) return;
        for (int i = 1; i <= 6; i++) crear(String.format("A-%02d", i), "Planta baja", TipoVehiculo.AUTO, "1.00");
        for (int i = 1; i <= 3; i++) crear(String.format("M-%02d", i), "Zona motos", TipoVehiculo.MOTO, "0.50");
        log.info("Espacios iniciales creados: {}", espacioRepository.count());
    }

    private void crear(String codigo, String zona, TipoVehiculo tipo, String tarifa) {
        espacioRepository.save(Espacio.builder().codigo(codigo).zona(zona).tipo(tipo)
                .tarifaHora(new BigDecimal(tarifa)).estado(EstadoEspacio.DISPONIBLE).build());
    }
}
