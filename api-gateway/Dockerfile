# 🔥 Use Ubuntu-based JDK to ensure AI Native Libraries work perfectly
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the compiled JAR file into the container
COPY target/*.jar app.jar

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]