package ec.edu.parqueadero.service.impl;

import ec.edu.parqueadero.aspect.Auditable;
import ec.edu.parqueadero.dto.*;
import ec.edu.parqueadero.entity.Usuario;
import ec.edu.parqueadero.exception.*;
import ec.edu.parqueadero.repository.UsuarioRepository;
import ec.edu.parqueadero.repository.VehiculoRepository;
import ec.edu.parqueadero.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;

    @Override
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    public UsuarioResponse obtener(Long id) {
        return DtoMapper.toResponse(buscar(id));
    }

    @Override
    @Transactional
    @Auditable(operacion = "CREAR_USUARIO")
    public UsuarioResponse crear(UsuarioRequest r) {
        if (usuarioRepository.existsByCedula(r.cedula()))
            throw new RecursoDuplicadoException("usuario", "cedula", r.cedula());
        if (usuarioRepository.existsByEmailIgnoreCase(r.email()))
            throw new RecursoDuplicadoException("usuario", "email", r.email());
        Usuario u = Usuario.builder().activo(true).build();
        copiar(r, u);
        return DtoMapper.toResponse(usuarioRepository.save(u));
    }

    @Override
    @Transactional
    @Auditable(operacion = "ACTUALIZAR_USUARIO")
    public UsuarioResponse actualizar(Long id, UsuarioRequest r) {
        Usuario u = buscar(id);
        if (usuarioRepository.existsByCedulaAndIdNot(r.cedula(), id))
            throw new RecursoDuplicadoException("usuario", "cedula", r.cedula());
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(r.email(), id))
            throw new RecursoDuplicadoException("usuario", "email", r.email());
        copiar(r, u);
        return DtoMapper.toResponse(usuarioRepository.save(u));
    }

    @Override
    @Transactional
    @Auditable(operacion = "CAMBIAR_ESTADO_USUARIO")
    public UsuarioResponse cambiarEstado(Long id, boolean activo) {
        Usuario u = buscar(id);
        u.setActivo(activo);
        return DtoMapper.toResponse(usuarioRepository.save(u));
    }

    @Override
    @Transactional
    @Auditable(operacion = "ELIMINAR_USUARIO")
    public void eliminar(Long id) {
        Usuario u = buscar(id);
        long vehiculos = vehiculoRepository.countByUsuarioId(id);
        if (vehiculos > 0)
            throw new ReglaNegocioException("USUARIO_CON_VEHICULOS",
                    "No se puede eliminar el usuario porque tiene " + vehiculos + " vehiculo(s) registrado(s)");
        usuarioRepository.delete(u);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", "id", id));
    }

    private void copiar(UsuarioRequest r, Usuario u) {
        u.setCedula(r.cedula());
        u.setNombres(r.nombres().trim());
        u.setApellidos(r.apellidos().trim());
        u.setEmail(r.email().trim().toLowerCase());
        u.setTelefono(r.telefono());
        u.setTipo(r.tipo());
    }
}
