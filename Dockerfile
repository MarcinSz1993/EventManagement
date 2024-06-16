FROM openjdk:17-jdk

WORKDIR /app

COPY target/EventManagementSystem-0.0.1-SNAPSHOT.jar /app/EventManagement.jar

COPY src/main/resources/db/changelog /app/src/main/resources/db/changelog

EXPOSE 8080

CMD ["java", "-jar", "EventManagement.jar"]