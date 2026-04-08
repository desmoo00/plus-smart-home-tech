#!/bin/bash

set -euo pipefail

BRANCH_NAME=${GITHUB_HEAD_REF:-${GITHUB_REF##*/}}
WAIT_FOR_IT="$(dirname "$0")/../stuff/scripts/wait-for-it.sh"
LOG_DIR="${LOG_DIR:-./logs}"
POSTGRES_USER=${POSTGRES_USER:-postgres}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-password}
POSTGRES_PORT=${POSTGRES_PORT:-5432}

mkdir -p "$LOG_DIR"

find_jar() {
  local service=$1
  local jar_path

  jar_path=$(find ./ -name "${service}-*-boot.jar" | head -n 1)
  if [[ -z "$jar_path" ]]; then
    jar_path=$(find ./ -name "${service}-*.jar" | head -n 1)
  fi

  echo "$jar_path"
}

COLLECTOR_JAR=$(find_jar "collector")
AGGREGATOR_JAR=$(find_jar "aggregator")
ANALYZER_JAR=$(find_jar "analyzer")

echo "Проверка наличия JAR-файлов и запуск нужных сервисов..."
echo "Найденные JAR-файлы:"
echo "Collector: $COLLECTOR_JAR"
echo "Aggregator: $AGGREGATOR_JAR"
echo "Analyzer: $ANALYZER_JAR"
echo "Текущая ветка: $BRANCH_NAME"

start_service() {
  local service_name=$1
  local jar_path=$2
  shift 2

  local log_file="${LOG_DIR}/${service_name}.log"
  local pid_file="${LOG_DIR}/${service_name}.pid"

  if [[ -z "$jar_path" ]]; then
    echo "❌ Ошибка: JAR-файл для сервиса $service_name не найден."
    exit 1
  fi

  echo "⏳ Запуск сервиса $service_name..."
  nohup java -jar "$jar_path" "$@" --logging.file.name="$log_file" >"$log_file" 2>&1 &
  local service_pid=$!
  echo "$service_pid" > "$pid_file"

  sleep 5

  if ! kill -0 "$service_pid" 2>/dev/null; then
    echo "❌ Ошибка: Сервис $service_name не запустился. Проверьте логи: $log_file"
    cat "$log_file"
    exit 1
  fi

  echo "✅ Сервис $service_name успешно запущен. PID: $service_pid"
}

check_port() {
  local service_name=$1
  local port=$2
  local log_file="${LOG_DIR}/${service_name}.log"

  if [[ -x "$WAIT_FOR_IT" ]]; then
    if ! "$WAIT_FOR_IT" "localhost:${port}" --timeout=20 --strict -- echo "✅ ${service_name} is up and ready"; then
      echo "❌ Ошибка: Порт ${port} сервиса ${service_name} не стал доступен. Проверьте логи: $log_file"
      cat "$log_file"
      exit 1
    fi
    return
  fi

  for _ in $(seq 1 20); do
    if (echo > "/dev/tcp/localhost/${port}") >/dev/null 2>&1; then
      echo "✅ ${service_name} is up and ready"
      return
    fi
    sleep 1
  done

  echo "❌ Ошибка: Порт ${port} сервиса ${service_name} не стал доступен. Проверьте логи: $log_file"
  cat "$log_file"
  exit 1
}

case "$BRANCH_NAME" in
  "1-collector-json")
    start_service "collector" "$COLLECTOR_JAR" "--server.port=8080"
    check_port "collector" "8080"
    ;;
  "2-collector-grpc")
    start_service "collector" "$COLLECTOR_JAR" "--grpc.server.port=59091"
    check_port "collector" "59091"
    ;;
  "3-aggregator")
    start_service "collector" "$COLLECTOR_JAR" "--grpc.server.port=59091"
    start_service "aggregator" "$AGGREGATOR_JAR"
    check_port "collector" "59091"
    ;;
  "4-analyzer"|"develop")
    start_service "collector" "$COLLECTOR_JAR" "--grpc.server.port=59091"
    start_service "aggregator" "$AGGREGATOR_JAR"
    start_service \
      "analyzer" \
      "$ANALYZER_JAR" \
      "--spring.datasource.url=jdbc:postgresql://localhost:${POSTGRES_PORT}/telemetry_analyzer" \
      "--spring.datasource.username=${POSTGRES_USER}" \
      "--spring.datasource.password=${POSTGRES_PASSWORD}"
    check_port "collector" "59091"
    ;;
  *)
    echo "❌ Ошибка: Ветка $BRANCH_NAME не поддерживается этим workflow."
    exit 1
    ;;
esac
