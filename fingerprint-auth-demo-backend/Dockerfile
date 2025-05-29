# Base image for Maven build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

# Set working directory
WORKDIR /usr/src/app

# Copy pom.xml first for layer caching
COPY pom.xml ./

# Download dependencies
RUN mvn dependency:go-offline --quiet

# Copy the source code after caching dependencies
COPY src ./src/

# Build the application
RUN mvn clean package -DskipTests --quiet

# Final runtime image
FROM eclipse-temurin:21.0.5_11-jre-alpine

WORKDIR /usr/src/app

# Set default environment variables
ENV PORT=8080 \
    CORS_ALLOWED_ORIGINS=http://localhost:3000 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="\
    -XX:+UseParallelGC \
    -Xss512k \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=25"

COPY --from=builder /usr/src/app/target/fingerprint-auth-demo-backend*.jar ./app.jar

# Expose the dynamic port
EXPOSE ${PORT}

# Start the application with environment variables
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT} --app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS}"] 