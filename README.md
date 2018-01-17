[![Build Status](https://travis-ci.org/peterbecich/NarrativeDemo.svg?branch=master)](https://travis-ci.org/peterbecich/NarrativeDemo)

Cues taken from [peterbecich/BannoDemo](https://github.com/peterbecich/BannoDemo) and [peterbecich/stock-ops](https://github.com/peterbecich/stock-ops)

-----------------------

# Quick Start

Docker Compose is required.

1. Create a file `NarrativeDemo/ops/server/.env`.  Docker Compose will read this file for its environmental variables
1. Copy this template into `.env`.  Make up a password.  Both the server and client will access this environmental variable:

```
POSTGRES_PASSWORD=
```
4. Proceed with either a pre-built Docker Image, or build the image from source

## Start from Pre-built Docker Image

1. Pull the Docker image from Docker Hub: 
   [peterbecich/narrativedemo](https://hub.docker.com/r/peterbecich/narrativedemo/)

1. Proceed to start the Compose application

## (or) Build from Source

SBT is required.

1. Start SBT in `NarrativeDemo/`
1. Enter the `server` sub-project
   ```
   sbt:narrativedemo> project backend
   [info] Set current project to narrativedemo (in build file:./NarrativeDemo/)
   ```
1. Run `docker` in SBT to produce the Docker image `peterbecich/narrativedemo:latest`
   ```
   sbt:narrativedemo> docker
   [info] Including from cache: cats-effect_2.12-0.8.jar
   .
   .
   .
   [info] Successfully built d467a3dfffeb
   [info] Tagging image d467a3dfffeb with name: peterbecich/narrativedemo:latest
   [success] Total time: 4 s, completed Jan 16, 2018 4:45:11 PM 
   ```
1. Exit SBT
1. Proceed to start the Compose application

## Start Docker Compose

1. Change directory to `NarrativeDemo/ops/server`
1. Run `docker-compose up` to start the demonstration and log to the terminal, or `docker-compose up -d` to start the demonstration and detach

1. See the documentation of REST endpoints

1. If detached, run `docker-compose down` to stop the Compose application


-----------------------

# Endpoints

* Create user
  ```POST http://localhost:80/user```
  ``` 
  {
  "userId": "97daea7a-4f58-4d15-a540-7c967c9df55b",
  "createdAt": "2018-01-17T01:05:46.988"
  }
  // POST http://localhost:80/user
  // HTTP/1.1 200 OK
  ```
  
* Retrieve user

  ```GET http://localhost:80/user?userId=97daea7a-4f58-4d15-a540-7c967c9df55b```
  
  ```
  {
  "userId": "97daea7a-4f58-4d15-a540-7c967c9df55b",
  "createdAt": "2018-01-17T01:05:46.988"
  }
  // GET http://localhost:80/user?userId=97daea7a-4f58-4d15-a540-7c967c9df55b
  // HTTP/1.1 200 OK
  ```
* Register "click" with user at current UTC time

  ```POST http://localhost:80/analytics?userId=97daea7a-4f58-4d15-a540-7c967c9df55b&event=click```
  
  ```
  // POST http://localhost:80/analytics?userId=97daea7a-4f58-4d15-a540-7c967c9df55b&event=click
  // HTTP/1.1 204 No Content
  ```
* Register "click" with user at given UTC epoch milliseconds

  ```POST http://localhost:80/analytics?userId=97daea7a-4f58-4d15-a540-7c967c9df55b&event=click&timestamp=1516151445306```
  
  ```
  // POST http://localhost:80/analytics?userId=97daea7a-4f58-4d15-a540-7c967c9df55b&event=click&timestamp=1516151445306
  // HTTP/1.1 204 No Content
  ```

* Register "impression" with user at current UTC time

  ```POST http://localhost:80/analytics?userId=97daea7a-4f58-4d15-a540-7c967c9df55b&event=impression```
  
  ```
  // POST http://localhost:80/analytics?userId=97daea7a-4f58-4d15-a540-7c967c9df55b&event=impression
  // HTTP/1.1 204 No Content
  ```

* Register "impression" with user at given UTC epoch milliseconds

  ```POST http://localhost:80/analytics?userId=97daea7a-4f58-4d15-a540-7c967c9df55b&event=impression&timestamp=1516151445306```
  
  ```
  // POST http://localhost:80/analytics?userId=97daea7a-4f58-4d15-a540-7c967c9df55b&event=impression&timestamp=1516151445306
  // HTTP/1.1 204 No Content
  ```
  
* Retrieve number of clicks, impressions, and new users created in the current hour

  ```GET http://localhost:80/analytics```
  
  ```
  {
	  "hour": "2018-01-17T01:00",
	  "usersCreated": 3,
	  "clicks": 1,
	  "impressions": 1
  }
  // GET http://localhost:80/analytics
  // HTTP/1.1 200 OK
  ```

* Retrieve number of clicks, impressions, and new users created in the hour of the given UTC epoch milliseconds

  ```GET http://localhost:80/analytics?timestamp=1516150000000```
  
  ```
  {
  "hour": "2018-01-17T00:00",
  "usersCreated": 4,
  "clicks": 1,
  "impressions": 2
  }
  // GET http://localhost:80/analytics?timestamp=1516150000000
  // HTTP/1.1 200 OK
  ```







  





