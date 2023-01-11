FROM tomcat:9.0.70-jdk11

# install maven
RUN apt-get -y update && apt-get install -y maven

WORKDIR /scrabble

# add project directory
ADD . /scrabble

# maven build
RUN ["mvn", "clean", "install", "-e", "-DskipTests", "-DskipITs", "-Denvironment=prod"]

# move deployable to webapps
RUN cp /scrabble/scrabble-api/target/scrabble-api.war /usr/local/tomcat/webapps/scrabble-api.war

# run tomcat
EXPOSE 8080
CMD ["catalina.sh", "run"]