FROM maven:3.9.9-eclipse-temurin-8 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY app/pom.xml app/pom.xml
COPY tools/pom.xml tools/pom.xml
COPY app/src app/src
COPY tools/src tools/src

RUN mvn -q -DskipTests -pl app -am package

FROM tomcat:9.0-jdk8-temurin
ENV CATALINA_HOME=/usr/local/tomcat
COPY --from=build /workspace/app/target/greatanalyzer.war ${CATALINA_HOME}/webapps/greatanalyzer.war
EXPOSE 8080
