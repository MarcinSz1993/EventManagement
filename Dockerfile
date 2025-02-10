FROM openjdk:22-jdk

WORKDIR /app

COPY target/EventManagementSystem-0.0.1-SNAPSHOT.jar /app/EventManagementSystem.jar

EXPOSE 8080

CMD ["java", "-jar", "EventManagementSystem.jar"]