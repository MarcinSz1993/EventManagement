
This application is designed for the organization and management of events. It allows users to create, update, and manage events while enabling participants to join these events.
The system ensures secure user authentication and authorization through JWT and provides a clean separation of user data via DTOs for improved privacy.
Additionally, the application connects to an external weather API to allow users to check the weather forecast for the event day.The application also 
integrates with Kafka to send event information to a Kafka server. Subsequently, it automatically sends emails to adult users about upcoming events tailored for adults,
ensuring timely notifications and engagement. <br><br>

### System requirements
- Java 11 or higher
- Maven
- Docker

### Installation and running
1.Clone the repo:
```bash
  git clone https://github.com/MarcinSz1993/EventManagement
```
2.Go to directory of the project and build the project using command:
```bash
mvn clean install
```
3.Run the app using command:
```bash
mvn spring-boot:run
```

### A short backend presentation of the app :

### 1.Creating a user:
To create a user you need to type all required fields. Otherwise, an exception will be thrown. As a response you get UserDto.

Example request:
```json
{
  "firstName": "Jan",
  "lastName": "Nowak",
  "email": "jann@nowak.com",
  "username": "Janek",
  "password": "qwerty",
  "birthDate": "1993-02-10",
  "phoneNumber": "574632181",
  "accountNumber": "1274362541"
}
```
Example response:
```json

 {
  "userDto": {
  "userId": 15,
  "firstName": "Jan",
  "lastName": "Nowak",
  "email": "jann@nowak.com",
  "username": "Janek",
  "birthDate": "1993-02-10",
  "role": "USER",
  "phoneNumber": "574632181",
  "accountNumber": "1274362541",
  "accountStatus": "ACTIVE"
 },
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJKYW5layIsImlhdCI6MTcyMTkxNjQzMSwiZXhwIjoxNzIyMDAyODMxfQ.eRa94dhXLCX-TuIV4RsREWKOmsqG9YytWI7NXnV52Zc"
 } 
```


### 2.Loggin:
If you want to log in you must type correct credentials like email and password. As a response you get a token. The token automatically goes to cookie file.
Example request:
```json
{
  "username": "Janek",
  "password": "qwerty"
}
```
Example response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJKYW5layIsImlhdCI6MTcyMTkxNjUyNSwiZXhwIjoxNzIyMDAyOTI1fQ.OWxbZeqnph35EURhnTzDi5R8eWtJw9UCMalxwjhlbbo"
}
```

### 3.Creating event:
To create an event you need to fill all required fields and of course you must be logged in. The event name must be unique. As a response you get info about created event, organiser and list of participants.
Example request:
```json
{
  "eventName": "Example event",
  "eventDescription": "Here you can write all details about the event.",
  "location": "Kraków",
  "maxAttendees": 6,
  "eventDate": "2025-02-10",
  "ticketPrice": 0,
  "eventTarget": "EVERYBODY"
}
```
```json
{
  "eventName": "Example event",
  "eventDescription": "Here you can write all details about the event.",
  "eventLocation": "Kraków",
  "maxAttendees": 6,
  "eventDate": "2025-02-10",
  "eventStatus": "ACTIVE",
  "ticketPrice": 0,
  "eventTarget": "EVERYBODY",
  "createdDate": "2024-07-25T16:16:50.6737306",
  "organiser": {
    "firstName": "Jan",
    "lastName": "Nowak",
    "userName": "Janek",
    "email": "jann@nowak.com",
    "phoneNumber": "574632181"
  },
  "participants": []
}
```

### 4.Updating event:
If you want to update your event you should type an id of the event and then you can change any field in the event. You can change one, all or few fields depends on you.
WARNING: You must be an owner the event to update the event.<br>
Example request(Suppose we want to change max attendees):
```json
{
  "maxAttendees": 10
}
```
Example response:
```json
{
  "eventName": "Example event.",
  "eventDescription": "Here you can write all details about the event.",
  "eventLocation": "Kraków",
  "maxAttendees": 10,
  "eventDate": "2025-02-10",
  "eventStatus": "ACTIVE",
  "ticketPrice": 0,
  "eventTarget": "EVERYBODY",
  "createdDate": "2024-07-25T16:16:50.66073",
  "organiser": {
    "firstName": "Jan",
    "lastName": "Nowak",
    "userName": "Janek",
    "email": "jann@nowak.com",
    "phoneNumber": "574632181"
  },
  "participants": []
}
```


### 5.Join event:
To join the event you have to type eventName as a parameter and the email as a request body.<br>
Example request:
```http request
http://localhost:8080/events/join?eventName=Example%20event.
```
```json
{
  "email": "marcin@kowalski.com"
}
```
Example response:
```json
"You joined to the event EXAMPLE EVENT.."
```

### 6.Checking weather:
You can check a weather on an eventday when you type eventId. A response shows some weather info from API which is represented by WeatherDto class.<br>
Example request:
```http request
http://localhost:8080/weather/?eventId=36
```
Example response:
```json
{
  "date": "2024-07-30",
  "cityName": "Krakow Am See",
  "country": "Germany",
  "sunrise": "05:23 AM",
  "sunset": "09:11 PM",
  "maxTemperature": 23,
  "minTemperature": 11,
  "maxWind": 16,
  "chanceOfRain": 0,
  "chanceOfSnow": 0
}
```

### 7.Delete event:
To delete the event you just need to give an id of the event you want to delete.
WARNING: You must be an owner the event to delete the event.<br>
Example request:
```http request
http://localhost:8080/events/?eventId=36
```
Example response:
```json
"You deleted event Example event."
```


