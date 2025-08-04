# Multi-stage build for Spring Boot application
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Make mvnw executable
RUN chmod +x ./mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
