#!/usr/bin/env bash
# Compila, prueba, levanta la API sobre PostgreSQL, ejecuta la demo y guarda todo en ./evidencias
set -u
cd "$(dirname "$0")/.."
EV="$PWD/evidencias"; rm -rf "$EV"; mkdir -p "$EV/requests"
LOG="$EV/00_resumen.txt"
paso() { echo; echo "===== $* =====" | tee -a "$LOG"; }

paso "Entorno"
{ uname -a; java -version 2>&1; (mvn -v 2>&1 | head -3) || echo "mvn no instalado"; psql --version 2>&1; git --version; } | tee -a "$LOG"

MVN=mvn
if ! command -v mvn >/dev/null; then
  paso "Descargando Maven 3.9.11 en ~/.local"
  mkdir -p ~/.local && curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.tar.gz | tar xz -C ~/.local
  MVN=~/.local/apache-maven-3.9.11/bin/mvn
fi

paso "Base de datos PostgreSQL"
export PGPASSWORD=parqueadero123
PSQL="psql -h localhost -U parqueadero -d parqueadero_db"
if ! $PSQL -c "select 1" >/dev/null 2>&1; then
  echo "Creando usuario y base parqueadero_db (puede pedir tu contrasena de sudo)..." | tee -a "$LOG"
  sudo -u postgres psql -c "CREATE USER parqueadero WITH PASSWORD 'parqueadero123';" 2>&1 | tee -a "$LOG"
  sudo -u postgres psql -c "CREATE DATABASE parqueadero_db OWNER parqueadero;" 2>&1 | tee -a "$LOG"
fi
$PSQL -c "select version();" 2>&1 | tee -a "$LOG"
$PSQL -c "DROP TABLE IF EXISTS auditoria_operaciones, registros_estacionamiento, vehiculos, espacios, usuarios CASCADE;" 2>&1 | tee -a "$LOG"

paso "Compilacion y pruebas (mvn clean verify)"
$MVN -B -ntp clean verify > "$EV/01_build.log" 2>&1; BUILD=$?
grep -E "Tests run:|BUILD|ERROR" "$EV/01_build.log" | tail -25 | tee -a "$LOG"
[ $BUILD -ne 0 ] && { echo "BUILD FALLIDO (ver evidencias/01_build.log)" | tee -a "$LOG"; exit 1; }

paso "Levantando la API"
java -jar target/parqueadero-api-1.0.0.jar > "$EV/02_app.log" 2>&1 & APP=$!
for i in $(seq 1 60); do curl -s localhost:8080/api/espacios >/dev/null && break; sleep 1; done
curl -s localhost:8080/api/espacios >/dev/null && echo "API arriba (pid $APP)" | tee -a "$LOG" || { echo "La API no arranco" | tee -a "$LOG"; tail -40 "$EV/02_app.log" | tee -a "$LOG"; kill $APP; exit 1; }

paso "Demostracion de endpoints (tarda ~1.5 min: espera 61 s para simular permanencia)"
bash scripts/demo.sh "$EV/requests" > "$EV/03_demo.txt" 2>&1
sleep 3
echo "Solicitudes ejecutadas: $(ls "$EV/requests" | wc -l)" | tee -a "$LOG"

paso "Persistencia: reinicio de la API y consulta del registro 1"
kill $APP; wait $APP 2>/dev/null; sleep 2
java -jar target/parqueadero-api-1.0.0.jar > "$EV/02b_app_reinicio.log" 2>&1 & APP=$!
for i in $(seq 1 60); do curl -s localhost:8080/api/espacios >/dev/null && break; sleep 1; done
{ echo "\$ curl http://localhost:8080/api/registros/1   (despues de reiniciar la aplicacion)"; curl -s -i localhost:8080/api/registros/1 | tr -d '\r'; } > "$EV/04_persistencia_api.txt"
kill $APP; wait $APP 2>/dev/null

paso "Persistencia: consultas directas en PostgreSQL"
{
 echo "\$ psql -d parqueadero_db -c '\\dt'"; $PSQL -c '\dt'
 echo "\$ SELECT * FROM usuarios"; $PSQL -c "SELECT id, cedula, nombres, apellidos, tipo, activo FROM usuarios ORDER BY id;"
 echo "\$ SELECT * FROM vehiculos"; $PSQL -c "SELECT id, placa, marca, modelo, tipo, usuario_id FROM vehiculos ORDER BY id;"
 echo "\$ SELECT * FROM espacios"; $PSQL -c "SELECT id, codigo, zona, tipo, tarifa_hora, estado FROM espacios ORDER BY id;"
 echo "\$ SELECT * FROM registros_estacionamiento"; $PSQL -c "SELECT id, vehiculo_id, espacio_id, to_char(fecha_ingreso,'HH24:MI:SS') ingreso, to_char(fecha_salida,'HH24:MI:SS') salida, minutos_permanencia min, monto_cobrado, estado FROM registros_estacionamiento ORDER BY id;"
 echo "\$ SELECT * FROM auditoria_operaciones"; $PSQL -c "SELECT id, operacion, resultado, duracion_ms, request_id FROM auditoria_operaciones ORDER BY id;"
 echo "\$ Restricciones (PK, FK, UNIQUE, CHECK)"; $PSQL -c "SELECT conrelid::regclass tabla, conname, contype FROM pg_constraint WHERE connamespace='public'::regnamespace ORDER BY 1,3;"
} > "$EV/05_postgresql.txt" 2>&1

paso "Git"
git log --oneline --graph --all > "$EV/06_git_log.txt" 2>&1
echo "LISTO. Resultados en: $EV" | tee -a "$LOG"
