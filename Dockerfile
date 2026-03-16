# ==================== STAGE 1: BUILD ====================
# Usamos la imagen oficial de Maven con Java 21 para compilar
# https://hub.docker.com/_/maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiar pom.xml primero para aprovechar el cache de capas Docker.
# Si el pom.xml no cambia, Maven no re-descarga las dependencias.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copiar el codigo fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests -q

# ==================== STAGE 2: RUNTIME ====================
# Imagen minima de JRE (no JDK completo) para reducir el tamano final.
# eclipse-temurin es la distribucion oficial de OpenJDK recomendada por la comunidad.
# https://hub.docker.com/_/eclipse-temurin
FROM eclipse-temurin:21-jre-alpine

# Metadatos del contenedor
LABEL maintainer="SkillSwap Team <dev@skillswap.app>"
LABEL version="1.0.0"
LABEL description="SkillSwap API - Plataforma de intercambio de habilidades"

WORKDIR /app

# Crear usuario no-root por seguridad (best practice OWASP)
RUN addgroup -S skillswap && adduser -S skillswap -G skillswap

# Copiar el JAR desde el stage de build
COPY --from=builder /app/target/skillswap-1.0.0-SNAPSHOT.jar app.jar

# Cambiar propietario del directorio
RUN chown -R skillswap:skillswap /app

# Cambiar al usuario no-root
USER skillswap

# Puerto expuesto (Railway asigna PORT dinamicamente)
EXPOSE 8081

# Variables de entorno por defecto (Railway las sobreescribe)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Healthcheck para que Railway/Docker sepa si la app esta sana
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:${PORT:-8081}/api/skills/categories || exit 1

# Comando de inicio
# Usamos exec para que Java sea PID 1 y reciba las senales correctamente
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8081}"]
