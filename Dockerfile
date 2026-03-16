# ==================== STAGE 1: BUILD ====================
# Imagen oficial de Maven con Java 21 para compilar el proyecto.
# https://hub.docker.com/_/maven
# Compatible con: Railway, Render, Fly.io, Google Cloud Run
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiar pom.xml primero para aprovechar el cache de capas Docker.
# Si el pom.xml no cambia, Maven no re-descarga las dependencias.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copiar el codigo fuente y compilar el JAR
COPY src ./src
RUN mvn clean package -DskipTests -q

# ==================== STAGE 2: RUNTIME ====================
# JRE minimo (no JDK completo) para reducir tamano final de imagen (~200MB vs ~600MB).
# eclipse-temurin es la distribucion oficial de OpenJDK.
# https://hub.docker.com/_/eclipse-temurin
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="SkillSwap <dev@skillswap.app>"
LABEL description="SkillSwap API - Spring Boot 3 + Java 21"

WORKDIR /app

# Crear usuario no-root por seguridad (OWASP Container Security best practice)
RUN addgroup -S skillswap && adduser -S skillswap -G skillswap

# Copiar el JAR del stage anterior
COPY --from=builder /app/target/skillswap-1.0.0-SNAPSHOT.jar app.jar
RUN chown -R skillswap:skillswap /app

USER skillswap

# Puerto por defecto. Render y Railway sobreescriben con $PORT.
EXPOSE 8081

# JVM tuneada para contenedores con memoria limitada (plan gratuito ~512MB RAM)
ENV JAVA_OPTS="-Xms128m -Xmx400m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Healthcheck: verifica que la app responde antes de marcarla como healthy
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget -qO- http://localhost:${PORT:-8081}/api/skills/categories > /dev/null || exit 1

# ENTRYPOINT con conversion postgresql:// -> jdbc:postgresql://
# Render inyecta SPRING_DATASOURCE_URL como "postgresql://..." (formato psycopg2).
# Spring Boot requiere "jdbc:postgresql://..." (formato JDBC).
# El sed convierte el formato automaticamente en el arranque.
ENTRYPOINT ["sh", "-c", "\
  if [ -n \"$SPRING_DATASOURCE_URL\" ]; then \
    export SPRING_DATASOURCE_URL=$(echo $SPRING_DATASOURCE_URL | sed 's|^postgresql://|jdbc:postgresql://|'); \
  fi; \
  exec java $JAVA_OPTS \
    -jar app.jar \
    --server.port=${PORT:-8081} \
"]
