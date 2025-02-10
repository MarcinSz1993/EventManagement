FROM maven:3.9.6-eclipse-temurin-22 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:22-jdk-slim
COPY --from=build /target/EventManagementSystem-0.0.1-SNAPSHOT.jar /app/EventManagementSystem.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/EventManagementSystem.jar"]