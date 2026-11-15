package ec.edu.parqueadero.controller;

import ec.edu.parqueadero.entity.AuditoriaOperacion;
import ec.edu.parqueadero.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expone las trazas generadas por el aspecto de auditoria (evidencia de AOP). */
@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public List<AuditoriaOperacion> ultimas(@RequestParam(required = false) String resultado) {
        return auditoriaService.ultimas(resultado);
    }
}
