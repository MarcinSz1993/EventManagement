#### As the project is still under development, please switch to 'dev' branch to view the most up-to-date code.


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
2.Go to directory of the project and run Kafka server by using the command:
```bash
docker-compose -f docker-compose-kafka.yml up -d
```
3.After Kafka is running build the project using command:
```bash
  mvn clean install
```
4.Run the app using command:
```bash
  mvn spring-boot:run
```

### A short backend presentation of the app :

### 1.Creating a user:
To create a user you need to type all required fields. Otherwise, an exception will be thrown. As a response you get UserDto.
```http request
[POST]
http://localhost:8080/users/
```
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
```http request
[POST]
http://localhost:8080/users/login
```
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

### 3.1 Creating event:
To create an event you need to fill all required fields and of course you must be logged in. The event name must be unique. As a response you get info about created event, organiser and list of participants.
Example request:
```http request
[POST]
http://localhost:8080/events/
```
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
### 3.2 Creating event
There is also a second way to create events. You can upload a CSV file from your machine and the app will do rest job for you.
The file must be in CSV format and must contain exactly 7 headers seperated by comma.

A sample of correct CSV file:
```html
eventname,eventdescription,location,maxattendees,eventdate,ticketprice,eventtarget
Tech Conference 2024,Annual tech conference featuring industry leaders,San Francisco,500,2024-10-15,199.99,ADULTS_ONLY
Art Exhibition: Modern Art,Showcase of contemporary artworks,New York City,200,2024-09-05,29.99,ADULTS_ONLY
Charity Gala 2024,Fancy gala dinner to support local charities,Los Angeles,300,2024-12-01,299.99,FAMILY
```

In this way you can add many events in one time.
Uploading the file is under the endpoint:
```http request
[POST]
http://localhost:8080/csv/uploadEvents
```
The response is number of added events:
```html
Added 3 events
```
### 3.3 Creating event
You also create event or events by uploading file in JSON format.
```http request
[POST]
http://localhost:8080/json/
```
The json file must have structure as below:
```json
[
  {
    "eventName": "Future Innovators Meetup2",
    "eventDescription": "A networking event for aspiring entrepreneurs and tech enthusiasts.",
    "location": "San Francisco",
    "maxAttendees": 400,
    "eventDate": "2024-11-01",
    "ticketPrice": 120.00,
    "eventTarget": "CHILDREN"
  }
]
```
The structure of the JSON file is validated by both the JSON Schema and the Spring validation system.
If the JSON file is valid, the response will show the number of events that were successfully added.

Response:
```html
Added 1 events
```
---------------------------
When you create an event it's automatically sending to Kafka server. In logs, you will see something like this:
```html
Sent message: EventDto(id=40, eventName=Example event, eventDescription=Here you can write all details about the event., eventLocation=Poznań, maxAttendees=10, eventDate=2024-08-30, eventStatus=ACTIVE, ticketPrice=20.0, eventTarget=FAMILY, createdDate=2024-07-25T20:44:26.298079200, organiser=OrganiserDto(firstName=Marcin, lastName=Kowalski, userName=Marcin2024, email=marcin@kowalski.pl, phoneNumber=563215675), participants=[])with offset: 3

```
and
```html
Event different from for adults only.
```
If created event will be for ADULTS_ONLY, application consume a message from Kafka topic and automatically will email to all registered adult users about the event.
In logs, you will see for whom emails have been sent:
```html
Sent email to: [marcinsz1993@hotmail.com, jann@nowak.com, marcin@kowalski.pl]
```
A template of the message user will see in their mailbox will be like this:
```html
[Subject] New event for you is waiting Example event for adults

Event details:

    Name: Example event for adults
    Description: Here you can write all details about the event.
    Location: Łódź
    Date: 2024-08-21
    Organiser: Marcin Kowalski
    Username: Marcin2024
    Phone: 563215675
```

### 4.Updating event:
If you want to update your event you should type an id of the event, and then you can change any field in the event. You can change one, all or few fields depends on you.
WARNING: You must be an owner the event to update the event.<br>
Example request(Suppose we want to change max attendees):
```http request
[PUT]
http://localhost:8080/events/?eventId=40
```
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
[PUT]
http://localhost:8080/events/join?eventName=Example%20event.
```
```json
{
  "email": "marcin@kowalski.com"
}
```
Example response:
```html
You joined to the event EXAMPLE EVENT.
```

### 6.Checking weather:
You can check a weather on an eventday when you type eventId. A response shows some weather info from API which is represented by WeatherDto class.<br>
Example request:
```http request
[GET]
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
[DELETE]
http://localhost:8080/events/?eventId=36
```
Example response:
```html
You deleted event Example event.
```

### 8.Reviews
After taking a part in the event you can write the review.
You can do this only if you were the participant of the event
and event status must be COMPLETED.

Request:
```http request
[POST]
http://localhost:8080/reviews/
```
Example request:
```json
{
  "eventName": "Science Fair 2024",
  "degree": 5,
  "content": "It was a really nice event!"
}
```
Degree is acceptable from 1 do 5.

Content is limited to 500 characters.

Example response:
```json
{
  "eventName": "Science Fair 2024",
  "degree": 5,
  "content": "It was a really nice event!",
  "reviewer": "ChaLor3810"
}
```

## EVERYDAY AT MIDNIGHT APPLICATION UPDATES EVENT STATUES AUTOMATICALLY

### 9. Download a personal file
If a user wants to get all available information about their events,
they can download a file in JSON format. The file contains details
about the events they have joined, including basic information
about other participants.

```http request
[GET]
http://localhost:8080/json/
```

Example downloaded personal file:
```json
[
  {
    "id": 1,
    "eventName": "Science Fair 2024",
    "eventDescription": "Annual fair featuring science projects from local students",
    "eventLocation": "Houston",
    "maxAttendees": 400,
    "eventDate": "2024-08-15",
    "eventStatus": "ACTIVE",
    "ticketPrice": 15.0,
    "eventTarget": "CHILDREN",
    "createdDate": "2024-08-08T22:02:25.131709",
    "organiser": {
      "firstName": "Mireielle",
      "lastName": "Darbie",
      "userName": "MirDar4410",
      "email": "Mireielle.Darbie@yopmail.com",
      "phoneNumber": "605696203"
    },
    "participants": [
      {
        "userId": 53,
        "firstName": "Charmaine",
        "lastName": "Loring",
        "email": "Charmaine.Loring@yopmail.com",
        "username": "ChaLor3810",
        "birthDate": "1971-12-05",
        "role": "USER",
        "phoneNumber": "336028537",
        "accountNumber": "4166756406",
        "accountStatus": "ACTIVE"
      },
      {
        "userId": 60,
        "firstName": "Silvana",
        "lastName": "Leffen",
        "email": "Silvana.Leffen@yopmail.com",
        "username": "SilLef8910",
        "birthDate": "1975-01-15",
        "role": "USER",
        "phoneNumber": "952271367",
        "accountNumber": "1190932600",
        "accountStatus": "ACTIVE"
      }
    ]
  }
]
```