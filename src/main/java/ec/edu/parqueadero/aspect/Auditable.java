package ec.edu.parqueadero.aspect;

import java.lang.annotation.*;

/** Marca un metodo de servicio cuya ejecucion debe quedar registrada en la tabla de auditoria. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String operacion();
}
