#!/bin/sh
docker build -t authdotsche . -f dockerfile
docker run -d --rm --privileged --name authdotsche authdotsche:latest
echo waiting for docker to start inside of the container...
sleep 5

docker exec -it authdotsche java -jar target/attackerJava-1.0.jar
docker cp authdotsche:/code/result.html .
docker kill authdotsche
