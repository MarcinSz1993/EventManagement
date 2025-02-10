FROM openjdk:22-jdk

WORKDIR /app
COPY . /app
RUN ./mvnw clean package -DskipTests
COPY target/EventManagementSystem-0.0.1-SNAPSHOT.jar /app/EventManagementSystem.jar
COPY src/main/resources/db/changelog /app/src/main/resources/db/changelog

EXPOSE 8080

CMD ["java", "-jar", "EventManagementSystem.jar"]