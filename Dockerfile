FROM docker:28.0.4-dind-alpine3.21

RUN apk add openjdk17 maven
RUN apk add openssl

ENV PYTHONUNBUFFERED=1
RUN apk add python3
RUN apk add py3-jinja2

RUN mkdir /code

COPY jinja /code/jinja
COPY src /code/src
COPY pom.xml /code

WORKDIR /code

RUN mvn clean package
RUN python3 jinja/jinja.py
RUN /code/setups/shared/cert/keyGen.sh

ENTRYPOINT ["sh", "-c", "dockerd --registry-mirror=https://mirror.gcr.io > /dev/null 2>&1 & while ! docker info > /dev/null 2>&1; do sleep 1; done; exec java -jar target/attackerJava-1.0.jar", "--"]