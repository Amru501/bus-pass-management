# =========================
# 1. Base image
# =========================
FROM openjdk:17-jdk-slim

# =========================
# 2. Set working directory
# =========================
WORKDIR /app

# =========================
# 3. Copy Maven wrapper and pom.xml
# =========================
COPY mvnw pom.xml ./
COPY .mvn .mvn

# =========================
# 4. Copy source code
# =========================
COPY src ./src

# =========================
# 5. Make Maven wrapper executable
# =========================
RUN chmod +x ./mvnw

# =========================
# 6. Build the Spring Boot app
# =========================
RUN ./mvnw clean package -DskipTests

# =========================
# 7. Copy the built jar
# =========================
RUN cp target/*.jar app.jar

# =========================
# 8. Expose port (Render will map $PORT)
# =========================
EXPOSE 10000

# =========================
# 9. Set environment variables defaults (optional)
# =========================
ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_NAME=buspassdb
ENV DB_USERNAME=root
ENV DB_PASSWORD=9074841649
ENV PORT=8080

# =========================
# 10. Run the application
# =========================
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar --spring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME} --spring.datasource.username=${DB_USERNAME} --spring.datasource.password=${DB_PASSWORD} --server.port=${PORT}"]
