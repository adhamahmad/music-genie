# Step 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: Runtime image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# # Default environment variables (can be overridden in docker run / AWS)
# ENV SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/music_genie \
#     SPRING_DATASOURCE_USERNAME=music_admin \
#     SPRING_DATASOURCE_PASSWORD=musicGenieAdmin541 \
#     SPRING_REDIS_HOST=host.docker.internal \
#     SPRING_REDIS_PORT=6379 \
#     ENCRYPTION_PASSWORD=changeme \
#     ENCRYPTION_SALT=changeme

ENTRYPOINT ["java","-jar","app.jar"]
