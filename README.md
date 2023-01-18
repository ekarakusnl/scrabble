# Scrabble

A scrabble game that contains angular frontend and java backend. The list of the components used in the project are;

>Angular 14  
Java 11  
Hibernate 5  
Spring 5  
Redis 4  
PostgreSQL 13
`

## Project Composition

Project has 4 modules. The modules are;

**scrabble-rest-model** : Manages dto objects used by the gateway and the api servers for the rest calls  

**scrabble-api**        : Manages dao, service and resource layers. Hibernate persistence operations, redis cache
  operations, scheduled quartz jobs are all managed in this module  

**scrabble-gateway**    : Manages authentication and authorization of frontend requests. Authentication is done by using
  spring authentication and a jwt token is generated and stored in cache for each successful login attempt  

**scrabble-web**        : Angular frontend application

## Running in Development Environment

The easiest way to run the project in local development environment is to use docker compose. `scrabble-api` and
`scrabble-gateway` are both dockerized and ready to run the application by creating the database and populating with sample data

The first step is to build the project since docker compose configuration expects deployment files to be ready.

```bash
mvn clean install -DskipTests -DskipITs -Denvironment.prod -e
```

### Running scrabble-api Project

Run the command below in `/scrabble` directory;

```bash
docker compose -f scrabble-api/docker-compose.yml up
```

This will run the api server on port `6081`, redis server on port `6800` and postgresql server on port `11223`. Then you can
navigate to `http://localhost:6081/scrabble-api/rest/users/by/user` to get the sample user.

### Running scrabble-gateway Project

Run the command below in `/scrabble` directory;

```bash
docker compose -f scrabble-gateway/docker-compose.yml up
```

This will run the gateway server on port `6080`. Then you can navigate to `http://localhost:6080/scrabble-gateway/rest/games`
to test the `games` resource. Since games resource only is reachable for authenticated users, getting `HTTP 401` response would
prove that the server is up and running.

### Running scrabble-web Project

Since scrabble-web is an angular project, `node v14.20.0` and `angular-cli` should be installed first. Before running the server,
an `.env` file should be provided under `/scrabble-web` directory to get the environment variables. 2 environment variables are
needed to start the frontend server;

**GATEWAY_URL**    : URL of the gateway server (eg: `http://localhost:6080/scrabble-gateway`)

**USER_IMAGE_URL** : URL of the image server for user profile pictures (eg: `http://localhost:8080/users`). These images should be
  stored in .png format and should have userId as the file name (eg: `http://localhost:8080/users/1.png` for user with id 1)

Then you can run 

```bash
npm run start
```

to run the node server. Navigating to `http://localhost:4200` will directly redirect you to the login screen where you can
login with

>username: user  
password: 1

user to use the application.

## Gameplay

Currently there are 3 menu items in the game;

**Create Game**: Creates a new game by specifying the game properties

**Lobby**  : Shows the last 3 created games

**Lounge** : Shows the games that the authenticated user currently is playing

The rules of the game are mostly traditional with some custom changes. Properties such as board size, cell values, rack size,
game language etc are customizable (even though there isn't any page to customize them yet). 3 languages
(french, english and turkish) can be selected to play the game and to use the application.

When a game is created, then others players are waited to reach the player count. As soon as the player count is reached, 
then the game starts and the board, the racks and the chat messaging are shown in the page. Every turn has a duration, when
a player exceeds his duration then his turn automatically is skipped and the turn passes to the next player. The remaining time
can be followed by a hourglass shown below the profile of the player.

Every round the players can exchange 1 letter on their rack with a vowel from the bag. The words played by the players are
validated in the dictionary and in case an invalid word is played then a warning message is shown to the player. When there
are no more letters in the bag an info message is shown to let the players know that the last round is getting played. When
the last round ends, then the winning player is mentioned and the game ends.
