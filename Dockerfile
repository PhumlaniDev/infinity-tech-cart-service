FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw package -DskipTests

# --- Final image ---
FROM eclipse-temurin:21-jre-jammy

# Create non-root user and group
RUN groupadd -g 1001 appuser && useradd -u 1001 -g appuser -s /bin/bash -m appuser

WORKDIR /app
COPY --from=builder /app/target/cart-service-0.0.1-SNAPSHOT.jar app.jar

# Change file permissions and ownership (optional but good)
RUN chown -R appuser:appuser /app

USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]