# syntax=docker/dockerfile:1

# ---------- Build the JAR (local builds, or anywhere no prebuilt jar exists) ----------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Retry transient download failures and stop reusing connections a proxy has already closed.
ENV MAVEN_OPTS="-Daether.connector.http.retryHandler.count=5 -Daether.connector.http.retryHandler.requestSentEnabled=true -Daether.connector.http.connectionMaxTtl=25"

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# The cache mount keeps ~/.m2 between builds on the same builder,
# so a failed or repeated build does not start from zero.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw --batch-mode --no-transfer-progress package -DskipTests

# ---------- Shared runtime base ----------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Non-root user, no login shell
RUN groupadd -g 1001 appuser \
 && useradd -u 1001 -g appuser -s /usr/sbin/nologin -m appuser

WORKDIR /app
RUN chown appuser:appuser /app

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]

# ---------- CI: package the jar the pipeline already built and tested ----------
# Used with `target: ci`. No Maven and no dependency downloads in this build.
FROM runtime AS ci
COPY --chown=appuser:appuser target/app-exec.jar app.jar

# ---------- Default: self-contained build (last stage = default target) ----------
FROM runtime AS final
COPY --from=builder --chown=appuser:appuser /app/target/app-exec.jar app.jar