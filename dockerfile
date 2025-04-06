FROM docker:28.0.4-dind-alpine3.21

RUN apk add openjdk17
RUN mkdir /code

COPY compiled_artifacts /code/compiled
COPY setups /code/setups

WORKDIR /code
