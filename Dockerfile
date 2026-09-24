# ----- stage 1: build the fat JAR with the Gradle wrapper -------------------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Cache the wrapper + dependencies before copying sources so layer reuse works
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies -q || true

COPY src ./src
RUN ./gradlew --no-daemon -x test bootJar

# ----- stage 2: minimal runtime image --------------------------------------
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# Run as a non-root user (security hardening)
RUN groupadd --system lpfap && useradd --system --gid lpfap --no-create-home lpfap
USER lpfap

COPY --from=build /workspace/build/libs/*.jar /app/lpfap.jar

EXPOSE 8080

# Heap kept conservative for 4 vCPU / 4 GB reference hardware (PDF C-02).
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/lpfap.jar"]
