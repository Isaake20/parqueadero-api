package ec.edu.parqueadero.controller;

import ec.edu.parqueadero.dto.*;
import ec.edu.parqueadero.service.RegistroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registros")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;

    /** Controla el ingreso: valida reglas de negocio y ocupa el espacio. */
    @PostMapping("/ingreso")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse ingreso(@Valid @RequestBody IngresoRequest request) {
        return registroService.registrarIngreso(request);
    }

    /**
     * Controla la salida: calcula permanencia y monto, libera el espacio.
     * notificacion=async (por defecto) envia el comprobante sin bloquear; notificacion=sync espera el envio.
     */
    @PatchMapping("/{id}/salida")
    public RegistroResponse salida(@PathVariable Long id,
                                   @RequestParam(defaultValue = "async") String notificacion) {
        return registroService.registrarSalida(id, !"sync".equalsIgnoreCase(notificacion));
    }

    @GetMapping("/activos")
    public List<RegistroResponse> activos() {
        return registroService.listarActivos();
    }

    @GetMapping("/{id}")
    public RegistroResponse obtener(@PathVariable Long id) {
        return registroService.obtener(id);
    }
}
