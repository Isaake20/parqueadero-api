package ec.edu.parqueadero.controller;

import ec.edu.parqueadero.dto.*;
import ec.edu.parqueadero.service.RegistroService;
import ec.edu.parqueadero.service.VehiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;
    private final RegistroService registroService;

    @GetMapping
    public List<VehiculoResponse> listar() {
        return vehiculoService.listar();
    }

    @GetMapping("/{id}")
    public VehiculoResponse obtener(@PathVariable Long id) {
        return vehiculoService.obtener(id);
    }

    @GetMapping("/placa/{placa}")
    public VehiculoResponse porPlaca(@PathVariable String placa) {
        return vehiculoService.obtenerPorPlaca(placa);
    }

    @GetMapping("/placa/{placa}/historial")
    public List<RegistroResponse> historial(@PathVariable String placa) {
        return registroService.historialPorPlaca(placa);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculoResponse crear(@Valid @RequestBody VehiculoRequest request) {
        return vehiculoService.crear(request);
    }

    @PutMapping("/{id}")
    public VehiculoResponse actualizar(@PathVariable Long id, @Valid @RequestBody VehiculoRequest request) {
        return vehiculoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
