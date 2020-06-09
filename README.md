# TicketToRide
This is a web version of the [Ticket to Ride](https://www.daysofwonder.com/tickettoride/en/) board game developed during COVID-19 times to play with my friends and to put my hands on the Kotlin language. Under the hood you'll find:
* Kotlin multiplatform - JVM + JS
* State machines and functional programming style for the game logic
* React for the frontend
* Google Maps for the underlying game map
* Websockets for the bidirectional communication between server and clients
* Some simple graph algorithms for final score calculation covered with property-based testing

The game is hosted at https://ticketgame.herokuapp.com/ (EU server) and https://ticketgame-us.herokuapp.com/ (US server).
Check out my writings on what I've learned in this project here - https://medium.com/@kiryushin.andrey.
