## As the project is still under development, please switch to the 'dev' branch to view the code.
<body>

This application is designed for the organization and management of events. It allows users to create, update, and manage events while enabling participants to join these events.
The system ensures secure user authentication and authorization through JWT and provides a clean separation of user data via DTOs for improved privacy.
Additionally, the application connects to an external weather API to allow users to check the weather forecast for the event day.The application also integrates with Kafka
to send event information to a Kafka server. Subsequently, it automatically sends emails to adult users about upcoming events tailored for adults, ensuring timely notifications and engagement. <br><br>


<h1>A short backend presentation of the app :</h1>

<h3>1.Creating a user:</h3>
To create a user you need to type all required fields. Otherwise an exception will be thrown. As a response you get UserDto.<br>
<a href="https://ibb.co/5ksD599"><img src="https://i.ibb.co/9hyXqvv/create-IUser.png" alt="create-IUser" border="0"></a>
<br>

<h3>2.Loggin:</h3>
If you want to log in you must type correct credentials like email and password. As a response you get a token. The token automatically goes to cookie file.<br>
<a href="https://ibb.co/QDHPTd2"><img src="https://i.ibb.co/PDWcPQ7/login-User.png" alt="login-User" border="0"></a>
<br>

<h3>3.Creating event:</h3>
To create an event you need to fill all required fields. The event name must be unique. As a response you get info about created event, organiser and list of participants.<br>
<a href="https://ibb.co/s2jqv6R"><img src="https://i.ibb.co/jThDvWf/create-Event.png" alt="create-Event" border="0"></a>
<br>

<h3>4.Updating event:</h3>
If you want to update your event you should type an id of the event and then you can change any field in the event. You can change one, all or few fields depends on you.
WARNING: You must be a owner the event to update the event.<br>
<a href="https://ibb.co/hYs1tHM"><img src="https://i.ibb.co/CPBsTvt/update-Event.png" alt="update-Event" border="0"></a>
<br>

<h3>5.Join event:</h3>
To join the event you have to type firstName, lastName, email and birthday.<br>
<a href="https://ibb.co/znrwK4Q"><img src="https://i.ibb.co/tm45yJQ/join-Event.png" alt="join-Event" border="0"></a>
<br>

<h3>6.Checking weather:</h3>
You can check a weather on a eventday when you type eventId. A response shows some weather info from API which is represented by WeatherDto class.<br>
<a href="https://ibb.co/Bg4RdMQ"><img src="https://i.ibb.co/YtDGYs1/check-Weather.png" alt="check-Weather" border="0"></a>
<br>

<h3>7.Delete event:</h3>
To delete the event you just need to give an id of the event you want to delete.<br>
WARNING: You must be an owner the event to delete the event.<br>
<a href="https://ibb.co/gD2BDNG"><img src="https://i.ibb.co/kh7Whjp/delete-Event.png" alt="delete-Event" border="0"></a>
<br>
</body>
