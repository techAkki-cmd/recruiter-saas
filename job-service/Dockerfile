# 1. Base Image
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# 2. Install Native Dependencies (Cached Layer)
# We do this BEFORE copying the jar so it doesn't re-run every time your code changes
RUN apt-get update && \
    apt-get install -y tesseract-ocr libtesseract-dev && \
    rm -rf /var/lib/apt/lists/*

# 3. Copy Application Code
# This layer changes often, so it stays at the bottom
COPY target/*.jar app.jar

# 4. Entry point
ENTRYPOINT ["java", "-jar", "app.jar"]