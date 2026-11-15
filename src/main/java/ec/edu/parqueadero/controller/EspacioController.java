package ec.edu.parqueadero.controller;

import ec.edu.parqueadero.dto.*;
import ec.edu.parqueadero.entity.EstadoEspacio;
import ec.edu.parqueadero.entity.TipoVehiculo;
import ec.edu.parqueadero.service.EspacioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/espacios")
@RequiredArgsConstructor
public class EspacioController {

    private final EspacioService espacioService;

    @GetMapping
    public List<EspacioResponse> listar(@RequestParam(required = false) EstadoEspacio estado) {
        return espacioService.listar(estado);
    }

    @GetMapping("/{id}")
    public EspacioResponse obtener(@PathVariable Long id) {
        return espacioService.obtener(id);
    }

    @GetMapping("/disponibles")
    public List<EspacioResponse> disponibles(@RequestParam(required = false) TipoVehiculo tipo) {
        return espacioService.listarDisponibles(tipo);
    }

    @GetMapping("/disponibilidad")
    public DisponibilidadResponse disponibilidad() {
        return espacioService.disponibilidad();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EspacioResponse crear(@Valid @RequestBody EspacioRequest request) {
        return espacioService.crear(request);
    }

    @PutMapping("/{id}")
    public EspacioResponse actualizar(@PathVariable Long id, @Valid @RequestBody EspacioRequest request) {
        return espacioService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public EspacioResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoEspacioRequest request) {
        return espacioService.cambiarEstado(id, request.estado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        espacioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
