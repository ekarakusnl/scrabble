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
ARG API_ENDPOINT
ARG JWT_SECRET
ARG LOG_PATH
ARG REDIS_HOST
ARG REDIS_PORT
ARG REDIS_USERNAME
ARG REDIS_PASSWORD
ARG REDIS_USE_SSL
ARG CDN_UPLOAD_IMAGE_ENDPOINT
ARG CDN_PRIVATE_KEY

# move deployable to webapps
COPY --from=build /scrabble/scrabble-gateway/target/scrabble-gateway.war /usr/local/tomcat/webapps/scrabble-gateway.war

# create scrabble directory
RUN mkdir /tmp/scrabble

# create logs directory
RUN mkdir /tmp/scrabble/logs

# set environment variables
ENV JAVA_OPTS="-Dapi.endpoint=$API_ENDPOINT \
               -Djwt.secret=$JWT_SECRET \
               -Dlog.path=$LOG_PATH \
               -Dredis.host=$REDIS_HOST \
               -Dredis.port=$REDIS_PORT \
               -Dredis.username=$REDIS_USERNAME \
               -Dredis.password=$REDIS_PASSWORD \
               -Dredis.use.ssl=$REDIS_USE_SSL \
               -Dcdn.upload.image.endpoint=$CDN_UPLOAD_IMAGE_ENDPOINT \
               -Dcdn.private.key=$CDN_PRIVATE_KEY"

# run tomcat
EXPOSE 8080
CMD ["catalina.sh", "run"]