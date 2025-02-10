FROM openjdk:22-jdk

WORKDIR /app

COPY . /app

RUN mvn clean package -DskipTests

COPY target/EventManagementSystem-0.0.1-SNAPSHOT.jar /app/EventManagementSystem.jar

CMD ["java", "-jar", "/app/EventManagementSystem.jar"]