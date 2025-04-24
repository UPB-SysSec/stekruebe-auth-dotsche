FROM docker:28.0.4-dind-alpine3.21

RUN apk add openjdk17 maven
RUN mkdir /code

COPY src /code/src
COPY setups /code/setups
COPY pom.xml /code

WORKDIR /code

RUN mvn clean package