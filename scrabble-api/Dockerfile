# syntax = docker/dockerfile:1.2
FROM tomcat:9.0.70-jdk11

# get connection properties
ARG POSTGRES_CONNECTION_URL
ARG POSTGRES_USERNAME
ARG POSTGRES_PASSWORD
ARG REDIS_HOST
ARG REDIS_PORT
ARG REDIS_USERNAME
ARG REDIS_PASSWORD

# install maven
RUN apt-get -y update && apt-get install -y maven

WORKDIR /scrabble

# add project directory
ADD . /scrabble

# maven build
RUN ["mvn", "clean", "install", "-e", "-DskipTests", "-DskipITs", "-Denvironment=prod"]

# move deployable to webapps
RUN cp /scrabble/scrabble-api/target/scrabble-api.war /usr/local/tomcat/webapps/scrabble-api.war

# create scrabble directory
RUN mkdir /tmp/scrabble

# create dictionary directory
RUN mkdir /tmp/scrabble/dictionary

# copy dictionaries to dictionary path
RUN cp -R /scrabble/scrabble-api/src/main/resources/dictionary /tmp/scrabble/dictionary

# set connection properties
ENV JAVA_OPTS="-Dpostgres.connection.url=$POSTGRES_CONNECTION_URL -Dpostgres.username=$POSTGRES_USERNAME \
               -Dpostgres.password=$POSTGRES_PASSWORD -Dredis.host=$REDIS_HOST -Dredis.port=$REDIS_PORT \
               -Dredis.username=$REDIS_USERNAME -Dredis.password=$REDIS_PASSWORD"

# run tomcat
EXPOSE 8080
CMD ["catalina.sh", "run"]