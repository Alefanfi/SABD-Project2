#!/bin/bash

if [[ $1 -eq 1 ]]
then
  echo "Submitting query 1 to flink"
  docker exec -t -i jobmanager flink run -p $2 -c "queries.query1.Query1" ./queries/SABD-Project2-1.0-SNAPSHOT-jar-with-dependencies.jar
elif [[ $1 -eq 2 ]]
then
  echo "Submitting query 2 to flink"
  docker exec -t -i jobmanager flink run -p $2 -c "queries.query2.Query2" ./queries/SABD-Project2-1.0-SNAPSHOT-jar-with-dependencies.jar
elif [[ $1 -eq 3 ]]
then
  echo "Submitting query 3 to flink"
  docker exec -t -i jobmanager flink run -p $2 -c "queries.query3.Query3" ./queries/SABD-Project2-1.0-SNAPSHOT-jar-with-dependencies.jar
else
  echo "Usage: sh submit-job.sh query_num parallelism"
fi