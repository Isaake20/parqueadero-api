package ec.edu.parqueadero.dto;

import ec.edu.parqueadero.entity.*;

/** Conversion entre entidades JPA y DTOs (la API nunca expone entidades directamente). */
public final class DtoMapper {

    private DtoMapper() {}

    public static UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getCedula(), u.getNombres(), u.getApellidos(), u.getEmail(),
                u.getTelefono(), u.getTipo(), u.getActivo(), u.getFechaRegistro());
    }

    public static VehiculoResponse toResponse(Vehiculo v) {
        Usuario u = v.getUsuario();
        return new VehiculoResponse(v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(), v.getColor(),
                v.getTipo(), u.getId(), u.getNombres() + " " + u.getApellidos());
    }

    public static EspacioResponse toResponse(Espacio e) {
        return new EspacioResponse(e.getId(), e.getCodigo(), e.getZona(), e.getTipo(), e.getTarifaHora(), e.getEstado());
    }

    public static RegistroResponse toResponse(RegistroEstacionamiento r) {
        return new RegistroResponse(r.getId(), r.getVehiculo().getPlaca(), r.getVehiculo().getTipo(),
                r.getEspacio().getCodigo(), r.getFechaIngreso(), r.getFechaSalida(), r.getMinutosPermanencia(),
                formatearTiempo(r.getMinutosPermanencia()), r.getMontoCobrado(), r.getEstado(), r.getObservacion());
    }

    public static String formatearTiempo(Long minutos) {
        if (minutos == null) return null;
        return (minutos / 60) + " h " + (minutos % 60) + " min";
    }
}
