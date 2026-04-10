# 🔥 Use Ubuntu-based JDK to ensure AI Native Libraries work perfectly
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the compiled JAR file into the container
COPY target/*.jar app.jar

RUN apt-get update && apt-get install -y tesseract-ocr libtesseract-dev && rm -rf /var/lib/apt/lists/*
# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]