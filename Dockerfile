# Build the spring boot project
FROM eclipse-temurin:21-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn/ .mvn/

RUN ./mvnw dependency:go-offline -B
# Cache fetched dependencies 

COPY src/ src/

RUN ./mvnw -Dmaven.test.skip=true package

# Copy the fat-jar and build a smaller docker image
FROM eclipse-temurin:21-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
