# ════════════════════════════════════════════════════════════════════════════
# BACKEND DOCKERFILE - Multi-stage build
# ════════════════════════════════════════════════════════════════════════════
# Stage 1: Build the JAR using Maven
# Stage 2: Run the JAR in a minimal JRE image
# ════════════════════════════════════════════════════════════════════════════

# ─────────────────────────────────────────────────────────────────────────────
# STAGE 1: BUILD
# Uses Maven with JDK 17 to compile and package the application
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only pom.xml first to cache dependencies
# This layer is cached unless pom.xml changes
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source code and build
# This layer rebuilds when source code changes
COPY src ./src
COPY .code_quality ./.code_quality
COPY lombok.config .

# Package the application (skip tests - they run in CI)
RUN mvn clean package -DskipTests

# ─────────────────────────────────────────────────────────────────────────────
# STAGE 2: RUNTIME
# Uses minimal JRE Alpine image for smaller size (~200MB vs ~500MB)
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user for security
# Running as root in containers is a security risk
RUN addgroup -S app && adduser -S app -G app
USER app

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Health check endpoint (Spring Actuator or custom)
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
