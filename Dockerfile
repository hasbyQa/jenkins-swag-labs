# Use the official Maven image with Java 17 — handles both build and test in one image
FROM maven:3.9.6-eclipse-temurin-17-alpine

WORKDIR /app

# Copy pom.xml first so Docker caches dependencies separately from source code
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Copy the test source code
COPY src ./src

# Run the full test suite when the container starts
CMD ["mvn", "test", "-B"]
