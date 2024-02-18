# syntax = docker/dockerfile:1.2
FROM maven:3.8.5-openjdk-17-slim as build

# set work directory
WORKDIR /scrabble

# add project directory
ADD . /scrabble

# maven build
RUN mvn clean install -e -DskipTests -DskipITs -Denvironment=prod

FROM tomcat:10.1.18-jdk17

# get environment variables
ARG DICTIONARY_PATH
ARG LOG_PATH
ARG POSTGRES_URL
ARG POSTGRES_USERNAME
ARG POSTGRES_PASSWORD
ARG REDIS_HOST
ARG REDIS_PORT
ARG REDIS_USERNAME
ARG REDIS_PASSWORD
ARG REDIS_USE_SSL

# move deployable to webapps
COPY --from=build /scrabble/scrabble-api/target/scrabble-api.war /usr/local/tomcat/webapps/scrabble-api.war

# create scrabble directory
RUN mkdir /tmp/scrabble

# create dictionary directory
RUN mkdir /tmp/scrabble/dictionary

# create logs directory
RUN mkdir /tmp/scrabble/logs

# copy dictionaries to dictionary path
COPY --from=build /scrabble/scrabble-api/src/main/resources/dictionary /tmp/scrabble/dictionary

# set environment variables
ENV JAVA_OPTS="-Ddictionary.path=$DICTIONARY_PATH -Dlog.path=$LOG_PATH \
               -Dpostgres.url=$POSTGRES_URL -Dpostgres.username=$POSTGRES_USERNAME \
               -Dpostgres.password=$POSTGRES_PASSWORD -Dredis.host=$REDIS_HOST -Dredis.port=$REDIS_PORT \
               -Dredis.username=$REDIS_USERNAME -Dredis.password=$REDIS_PASSWORD -Dredis.use.ssl=$REDIS_USE_SSL"

# run tomcat
EXPOSE 8080
CMD ["catalina.sh", "run"]