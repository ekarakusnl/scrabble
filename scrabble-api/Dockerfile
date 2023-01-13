# syntax = docker/dockerfile:1.2
FROM maven:3.8.3-jdk-11-slim as build

# set work directory
WORKDIR /scrabble

# add project directory
ADD . /scrabble

# maven build
RUN mvn clean install -e -DskipTests -DskipITs -Denvironment=prod

FROM tomcat:9.0.70-jdk11

# get connection properties
ARG POSTGRES_CONNECTION_URL
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

# copy dictionaries to dictionary path
COPY --from=build /scrabble/scrabble-api/src/main/resources/dictionary /tmp/scrabble/dictionary

# set connection properties
ENV JAVA_OPTS="-Dpostgres.connection.url=$POSTGRES_CONNECTION_URL -Dpostgres.username=$POSTGRES_USERNAME \
               -Dpostgres.password=$POSTGRES_PASSWORD -Dredis.host=$REDIS_HOST -Dredis.port=$REDIS_PORT \
               -Dredis.username=$REDIS_USERNAME -Dredis.password=$REDIS_PASSWORD -Dredis.use.ssl=$REDIS_USE_SSL"

# run tomcat
EXPOSE 8080
CMD ["catalina.sh", "run"]