#!/usr/bin/env bash
# Demostracion de los principales endpoints. Uso: bash scripts/demo.sh [carpeta_salida]
# Requiere la API corriendo en http://localhost:8080 sobre una base vacia.
BASE="${BASE_URL:-http://localhost:8080/api}"
KEY="${API_KEY:-PARQ-2026-G10}"
OUT="${1:-}"
N=0

req() { # req "titulo" METODO ruta [json] [cabecera_extra]
  local titulo="$1" metodo="$2" ruta="$3" body="${4:-}" extra="${5:-}"
  N=$((N+1)); local id; id=$(printf "%02d" $N)
  local args=(-s -i -X "$metodo" "$BASE$ruta" -H "User-Agent: demo-grupo10")
  [ -n "$body" ] && args+=(-H "Content-Type: application/json" -d "$body")
  [ -n "$extra" ] && args+=(-H "$extra")
  local cmd="curl -X $metodo $BASE$ruta"
  [ -n "$extra" ] && cmd="$cmd -H \"$extra\""
  [ -n "$body" ] && cmd="$cmd -d '$body'"
  local resp; resp=$(curl "${args[@]}")
  local status; status=$(echo "$resp" | head -1 | tr -d '\r')
  local headers; headers=$(echo "$resp" | grep -iE '^(X-Request-Id|X-Response-Time):' | tr -d '\r')
  local json; json=$(echo "$resp" | sed '1,/^\r\{0,1\}$/d')
  local pretty; pretty=$(echo "$json" | python3 -m json.tool --no-ensure-ascii 2>/dev/null || echo "$json")
  local texto="### $id. $titulo
\$ $cmd
$status
$headers

$pretty"
  echo "$texto"; echo
  [ -n "$OUT" ] && echo "$texto" > "$OUT/$id.txt"
}
K="X-API-KEY: $KEY"

req "Disponibilidad inicial de espacios" GET /espacios/disponibilidad
req "Registrar usuario (POST correcto)" POST /usuarios '{"cedula":"1726354890","nombres":"Isaac","apellidos":"Betun","email":"isaac.betun@mail.com","telefono":"0991234567","tipo":"ESTUDIANTE"}' "$K"
req "Registrar segundo usuario" POST /usuarios '{"cedula":"1712345678","nombres":"Maria","apellidos":"Lopez","email":"maria.lopez@mail.com","telefono":"0987654321","tipo":"DOCENTE"}' "$K"
req "Registrar vehiculo tipo AUTO (placa se normaliza)" POST /vehiculos '{"placa":"pba1234","marca":"Kia","modelo":"Rio","color":"Blanco","tipo":"AUTO","usuarioId":1}' "$K"
req "Registrar vehiculo tipo MOTO" POST /vehiculos '{"placa":"IB123C","marca":"Yamaha","modelo":"FZ","color":"Azul","tipo":"MOTO","usuarioId":2}' "$K"
req "Registrar segundo AUTO" POST /vehiculos '{"placa":"PCC-0987","marca":"Chevrolet","modelo":"Sail","color":"Gris","tipo":"AUTO","usuarioId":2}' "$K"
req "Listar usuarios (GET)" GET /usuarios
req "Consultar vehiculo por placa (consulta personalizada)" GET /vehiculos/placa/PBA-1234
req "Actualizar vehiculo (PUT correcto)" PUT /vehiculos/1 '{"placa":"PBA-1234","marca":"Kia","modelo":"Rio","color":"Negro","tipo":"AUTO","usuarioId":1}' "$K"
req "Controlar ingreso con asignacion automatica de espacio" POST /registros/ingreso '{"placa":"PBA-1234","observacion":"Ingreso puerta norte"}' "$K"
req "Controlar ingreso de moto en espacio M-01" POST /registros/ingreso '{"placa":"IB123C","espacioId":7}' "$K"
req "RN-02: ingreso duplicado del mismo vehiculo" POST /registros/ingreso '{"placa":"PBA-1234"}' "$K"
req "RN-03: espacio ocupado" POST /registros/ingreso '{"placa":"PCC-0987","espacioId":1}' "$K"
req "RN-03: tipo de espacio incompatible" POST /registros/ingreso '{"placa":"PCC-0987","espacioId":8}' "$K"
req "Vehiculos dentro del parqueadero (JPQL)" GET /registros/activos
req "Disponibilidad tras los ingresos" GET /espacios/disponibilidad
sleep 61
req "Salida con notificacion BLOQUEANTE (sync)" PATCH "/registros/1/salida?notificacion=sync" "" "$K"
req "Salida con notificacion ASINCRONA (async)" PATCH /registros/2/salida "" "$K"
req "RN-05: salida ya registrada" PATCH /registros/1/salida "" "$K"
req "Historial por placa" GET /vehiculos/placa/PBA-1234/historial
req "Recurso inexistente: usuario 999" GET /usuarios/999
req "Recurso inexistente: placa" GET /vehiculos/placa/ZZZ-9999
req "Interceptor: DELETE sin X-API-KEY" DELETE /espacios/9
req "Interceptor: API key invalida" POST /espacios '{"codigo":"B-01","zona":"Subsuelo","tipo":"AUTO","tarifaHora":1.5}' "X-API-KEY: clave-falsa"
req "Validacion de datos (Bean Validation)" POST /usuarios '{"cedula":"123","nombres":"","apellidos":"X","email":"correo-malo","tipo":"ESTUDIANTE"}' "$K"
req "RN-01: cedula duplicada" POST /usuarios '{"cedula":"1726354890","nombres":"Otro","apellidos":"Usuario","email":"otro@mail.com","tipo":"VISITANTE"}' "$K"
req "Cambiar estado de espacio (PATCH)" PATCH /espacios/9/estado '{"estado":"MANTENIMIENTO"}' "$K"
req "Desactivar usuario (PATCH)" PATCH /usuarios/2/estado '{"activo":false}' "$K"
req "RN-04: usuario inactivo no puede ingresar" POST /registros/ingreso '{"placa":"PCC-0987"}' "$K"
req "Eliminar vehiculo sin historial (DELETE correcto)" DELETE /vehiculos/3 "" "$K"
req "RN-06: eliminar vehiculo con historial" DELETE /vehiculos/1 "" "$K"
req "Eliminar espacio libre sin historial (DELETE correcto)" DELETE /espacios/6 "" "$K"
req "RN-06: eliminar usuario con vehiculos" DELETE /usuarios/1 "" "$K"
req "Reporte de recaudacion BLOQUEANTE" GET "/reportes/ingresos?desde=2026-01-01T00:00:00&hasta=2026-12-31T23:59:59"
req "Reporte de recaudacion ASINCRONO (CompletableFuture)" GET "/reportes/ingresos/async?desde=2026-01-01T00:00:00&hasta=2026-12-31T23:59:59"
req "Trazas del aspecto de auditoria" GET "/auditoria"
