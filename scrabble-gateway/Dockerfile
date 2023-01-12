# syntax = docker/dockerfile:1.2
FROM tomcat:9.0.70-jdk11

# get connection properties
ARG SCRABBLE_API_ENDPOINT
ARG REDIS_HOST
ARG REDIS_PORT
ARG REDIS_USERNAME
ARG REDIS_PASSWORD
ARG REDIS_USE_SSL

# install maven
RUN apt-get -y update && apt-get install -y maven

WORKDIR /scrabble

# add project directory
ADD . /scrabble

# maven build
RUN ["mvn", "clean", "install", "-e", "-DskipTests", "-DskipITs", "-Denvironment=prod"]

# move deployable to webapps
RUN cp /scrabble/scrabble-gateway/target/scrabble-gateway.war /usr/local/tomcat/webapps/scrabble-gateway.war

# create scrabble directory
RUN mkdir /tmp/scrabble

# create user directory for user avatars
RUN mkdir /tmp/scrabble/user

# copy user avatars to user images path
RUN cp -R /scrabble/scrabble-gateway/src/main/resources/user /tmp/scrabble/user

# set connection properties
ENV JAVA_OPTS="-Dscrabble.api.endpoint=$SCRABBLE_API_ENDPOINT -Dredis.host=$REDIS_HOST -Dredis.port=$REDIS_PORT \
               -Dredis.username=$REDIS_USERNAME -Dredis.password=$REDIS_PASSWORD -Dredis.use.ssl=$REDIS_USE_SSL"

# run tomcat
EXPOSE 8080
CMD ["catalina.sh", "run"]