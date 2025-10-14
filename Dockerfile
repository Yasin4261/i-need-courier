# Multi-stage build for Spring Boot application
# Builder stage: use Temurin JDK 21 and the Maven wrapper to build the application
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
# Copy wrapper and source
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Make wrapper executable and build
RUN chmod +x ./mvnw && ./mvnw -B clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
