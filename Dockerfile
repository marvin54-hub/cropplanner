# ── Build stage ─────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom first so dependency resolution is cached across rebuilds
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ── Runtime stage ───────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a non-root user
RUN addgroup -S cropplanner && adduser -S cropplanner -G cropplanner

COPY --from=build /app/target/*.jar app.jar
RUN chown cropplanner:cropplanner app.jar

USER cropplanner
EXPOSE 8080

# Allow runtime overrides of DB host/creds and JWT secret via environment
# variables (mapped from application.properties via Spring's relaxed
# binding, e.g. SPRING_DATASOURCE_URL, JWT_SECRET).
ENTRYPOINT ["java", "-jar", "app.jar"]
