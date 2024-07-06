FROM openjdk:17-jdk

WORKDIR /app

COPY target/EventManagementSystem-0.0.1-SNAPSHOT.jar /app/EventManagement.jar

EXPOSE 8080

CMD ["java", "-jar", "EventManagement.jar"]