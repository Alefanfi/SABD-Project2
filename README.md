<h1 style="text-align:center;">SABD-Project2</h1>

<p align="center">
  <img width=200px" height="200px" src="https://github.com/Alefanfi/SABD-Project2/blob/main/logo/logo.png?raw=true">
</p>

This project uses the Apache Flink framework to analyze data taken by  [Automatic Identification Systems](https://en.wikipedia.org/wiki/Automatic_identification_system) (AIS) in the Mediterranean sea in order to answer the following queries:


<b>Query 1</b> - For each cell of the Western Mediterranean compute the mean number of military ships, passenger ships , cargo ships and other ships in a window of:

* 7 days
* 1 month

<b>Query 2</b> - For both the Western and Eastern Mediterranean compute the top 3 most popular cells divided in two time slots (am 00:00-11:59 / pm 12:00-23:59) in a window of:

* 7 days
* 1 month

<b>Query 3</b> - Compute the top 5 trips which have the highest traveled distance in a window of:

* 1 hour
* 2 hours

## Requirements
This project uses docker and docker-compose to instantiate each framework in an isolated container.

#### Docker links :
* [docker download](https://www.docker.com/products/docker-desktop)
* [docker compose download](https://docs.docker.com/compose/install/)

## Deployment
The number of Flink's taskmanagers can be scaled as needed using docker compose.

    docker compose up --scale taskmanager=2

    docker-compose scale taskmanager=5

## Submit the queries
To run the queries first you need to create a jar with all the dependencies to submit to Flink.

    mvn compile assembly:single

You can find a script in the folder /scripts which has all the needed logic to submit a job to the jobmanager, so you just need to specify which of the three queries you wish to compute.

    sh submit-job.sh 1

(eg. Submits query 1 to the cluster )

## Frameworks
* [<img src="https://miro.medium.com/max/400/1*b-i9e82pUCgJbsg3lpdFnA.jpeg" width=70px>](https://nifi.apache.org/)
* [<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/7/70/Apache_Flink_logo.svg/1200px-Apache_Flink_logo.svg.png" width=70px>](https://flink.apache.org/)
* [<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/Redis_Logo.svg/1200px-Redis_Logo.svg.png" width=70px height=35px>](https://redis.io/)
* [<img src="https://codeblog.dotsandbrackets.com/wp-content/uploads/2017/01/graphite-logo.png" width=70px height=35px>](https://graphiteapp.org/)

## Web UI
* http://localhost:9090/nifi &nbsp;&nbsp;&nbsp; nifi
* http://localhost:8081 &nbsp;&nbsp;&nbsp; flink jobmanager