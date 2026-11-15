package ec.edu.parqueadero.service.impl;

import ec.edu.parqueadero.aspect.Auditable;
import ec.edu.parqueadero.dto.*;
import ec.edu.parqueadero.entity.EstadoRegistro;
import ec.edu.parqueadero.entity.Usuario;
import ec.edu.parqueadero.entity.Vehiculo;
import ec.edu.parqueadero.exception.*;
import ec.edu.parqueadero.repository.*;
import ec.edu.parqueadero.service.VehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistroEstacionamientoRepository registroRepository;

    @Override
    public List<VehiculoResponse> listar() {
        return vehiculoRepository.findAll().stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    public VehiculoResponse obtener(Long id) {
        return DtoMapper.toResponse(buscar(id));
    }

    @Override
    public VehiculoResponse obtenerPorPlaca(String placa) {
        String p = normalizarPlaca(placa);
        return vehiculoRepository.findByPlacaIgnoreCase(p).map(DtoMapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo", "placa", p));
    }

    @Override
    public List<VehiculoResponse> listarPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId))
            throw new RecursoNoEncontradoException("Usuario", "id", usuarioId);
        return vehiculoRepository.buscarPorUsuario(usuarioId).stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    @Transactional
    @Auditable(operacion = "CREAR_VEHICULO")
    public VehiculoResponse crear(VehiculoRequest r) {
        String placa = normalizarPlaca(r.placa());
        if (vehiculoRepository.existsByPlacaIgnoreCase(placa))
            throw new RecursoDuplicadoException("vehiculo", "placa", placa);
        Vehiculo v = new Vehiculo();
        copiar(r, placa, v);
        return DtoMapper.toResponse(vehiculoRepository.save(v));
    }

    @Override
    @Transactional
    @Auditable(operacion = "ACTUALIZAR_VEHICULO")
    public VehiculoResponse actualizar(Long id, VehiculoRequest r) {
        Vehiculo v = buscar(id);
        String placa = normalizarPlaca(r.placa());
        if (vehiculoRepository.existsByPlacaIgnoreCaseAndIdNot(placa, id))
            throw new RecursoDuplicadoException("vehiculo", "placa", placa);
        if (v.getTipo() != r.tipo() && registroRepository.existsByVehiculoIdAndEstado(id, EstadoRegistro.ACTIVO))
            throw new ReglaNegocioException("VEHICULO_DENTRO",
                    "No se puede cambiar el tipo de un vehiculo que se encuentra dentro del parqueadero");
        copiar(r, placa, v);
        return DtoMapper.toResponse(vehiculoRepository.save(v));
    }

    @Override
    @Transactional
    @Auditable(operacion = "ELIMINAR_VEHICULO")
    public void eliminar(Long id) {
        Vehiculo v = buscar(id);
        if (registroRepository.existsByVehiculoIdAndEstado(id, EstadoRegistro.ACTIVO))
            throw new ReglaNegocioException("VEHICULO_DENTRO",
                    "No se puede eliminar el vehiculo " + v.getPlaca() + " porque se encuentra dentro del parqueadero");
        if (registroRepository.existsByVehiculoId(id))
            throw new ReglaNegocioException("VEHICULO_CON_HISTORIAL",
                    "No se puede eliminar el vehiculo " + v.getPlaca() + " porque tiene historial de estacionamiento");
        vehiculoRepository.delete(v);
    }

    private Vehiculo buscar(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo", "id", id));
    }

    private void copiar(VehiculoRequest r, String placa, Vehiculo v) {
        Usuario u = usuarioRepository.findById(r.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", "id", r.usuarioId()));
        v.setPlaca(placa);
        v.setMarca(r.marca().trim());
        v.setModelo(r.modelo().trim());
        v.setColor(r.color());
        v.setTipo(r.tipo());
        v.setUsuario(u);
    }

    /** Normaliza la placa a mayusculas y con guion (PBA1234 -> PBA-1234). */
    static String normalizarPlaca(String placa) {
        String p = placa.trim().toUpperCase().replace(" ", "");
        if (p.matches("^[A-Z]{3}\\d{3,4}$")) p = p.substring(0, 3) + "-" + p.substring(3);
        return p;
    }
}
