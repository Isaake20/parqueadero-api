package ec.edu.parqueadero.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.parqueadero.repository.AuditoriaOperacionRepository;
import ec.edu.parqueadero.repository.RegistroEstacionamientoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Prueba de integracion de extremo a extremo sobre H2 (controller -> service -> repository -> BD). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ParqueaderoIntegrationTest {

    private static final String KEY = "X-API-KEY";
    private static final String VALOR_KEY = "TEST-KEY";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired RegistroEstacionamientoRepository registroRepository;
    @Autowired AuditoriaOperacionRepository auditoriaRepository;

    static Long usuarioId, vehiculoId, espacioId, registroId;

    @Test @Order(1)
    @DisplayName("Registro correcto de usuario, espacio y vehiculo (201)")
    void registrosCorrectos() throws Exception {
        usuarioId = idDe(mvc.perform(post("/api/usuarios").header(KEY, VALOR_KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cedula":"1726354890","nombres":"Isaac","apellidos":"Betun",
                                 "email":"isaac.test@mail.com","telefono":"0991234567","tipo":"ESTUDIANTE"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Request-Id"))
                .andReturn());

        espacioId = idDe(mvc.perform(post("/api/espacios").header(KEY, VALOR_KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"T-01","zona":"Pruebas","tipo":"AUTO","tarifaHora":1.25}"""))
                .andExpect(status().isCreated()).andReturn());

        vehiculoId = idDe(mvc.perform(post("/api/vehiculos").header(KEY, VALOR_KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"pcd4567","marca":"Chevrolet","modelo":"Aveo","color":"Rojo",
                                 "tipo":"AUTO","usuarioId":%d}""".formatted(usuarioId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placa").value("PCD-4567"))
                .andReturn());
    }

    @Test @Order(2)
    @DisplayName("Consulta correcta (200)")
    void consultaCorrecta() throws Exception {
        mvc.perform(get("/api/vehiculos/placa/PCD-4567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propietario").value("Isaac Betun"));
    }

    @Test @Order(3)
    @DisplayName("Actualizacion correcta (200)")
    void actualizacionCorrecta() throws Exception {
        mvc.perform(put("/api/vehiculos/" + vehiculoId).header(KEY, VALOR_KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"PCD-4567","marca":"Chevrolet","modelo":"Sail","color":"Negro",
                                 "tipo":"AUTO","usuarioId":%d}""".formatted(usuarioId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Sail"));
    }

    @Test @Order(4)
    @DisplayName("Ingreso y regla de negocio: segundo ingreso del mismo vehiculo (422)")
    void ingresoYReglaNegocio() throws Exception {
        registroId = idDe(mvc.perform(post("/api/registros/ingreso").header(KEY, VALOR_KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"PCD-4567\",\"espacioId\":" + espacioId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVO")).andReturn());

        mvc.perform(post("/api/registros/ingreso").header(KEY, VALOR_KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\":\"PCD-4567\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("INGRESO_DUPLICADO"));

        mvc.perform(delete("/api/espacios/" + espacioId).header(KEY, VALOR_KEY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ESPACIO_OCUPADO"));
    }

    @Test @Order(5)
    @DisplayName("Salida: calcula permanencia y persiste el registro FINALIZADO")
    void salida() throws Exception {
        mvc.perform(patch("/api/registros/" + registroId + "/salida").header(KEY, VALOR_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"))
                .andExpect(header().exists("X-Response-Time"));
        assertThat(registroRepository.findById(registroId).orElseThrow().getFechaSalida()).isNotNull();
    }

    @Test @Order(6)
    @DisplayName("Busqueda de recurso inexistente (404)")
    void recursoInexistente() throws Exception {
        mvc.perform(get("/api/usuarios/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test @Order(7)
    @DisplayName("Interceptor: escritura sin X-API-KEY es rechazada (401)")
    void interceptorSinApiKey() throws Exception {
        mvc.perform(post("/api/espacios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"T-02\",\"zona\":\"X\",\"tipo\":\"AUTO\",\"tarifaHora\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("ACCESO_NO_AUTORIZADO"));
    }

    @Test @Order(8)
    @DisplayName("Validacion de datos de entrada (400)")
    void validacion() throws Exception {
        mvc.perform(post("/api/usuarios").header(KEY, VALOR_KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cedula\":\"123\",\"nombres\":\"\",\"apellidos\":\"X\",\"email\":\"malo\",\"tipo\":\"ESTUDIANTE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.length()").value(3));
    }

    @Test @Order(9)
    @DisplayName("Aspecto: las operaciones quedan auditadas con EXITO y ERROR")
    void aspectoAuditoria() {
        assertThat(auditoriaRepository.findByResultadoOrderByIdDesc("EXITO")).isNotEmpty();
        assertThat(auditoriaRepository.findByResultadoOrderByIdDesc("ERROR"))
                .anyMatch(a -> a.getOperacion().equals("REGISTRAR_INGRESO"));
    }

    @Test @Order(10)
    @DisplayName("Eliminacion correcta (204)")
    void eliminacionCorrecta() throws Exception {
        Long id = idDe(mvc.perform(post("/api/espacios").header(KEY, VALOR_KEY).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"T-09\",\"zona\":\"Temporal\",\"tipo\":\"MOTO\",\"tarifaHora\":0.5}"))
                .andExpect(status().isCreated()).andReturn());
        mvc.perform(delete("/api/espacios/" + id).header(KEY, VALOR_KEY)).andExpect(status().isNoContent());
        mvc.perform(get("/api/espacios/" + id)).andExpect(status().isNotFound());
    }

    private Long idDe(MvcResult result) throws Exception {
        JsonNode json = mapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}
