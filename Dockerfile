FROM openjdk:17-alpine
MAINTAINER Roman Shishkin <romashkin.2001@yandex.ru>

#Setting directories args
ARG APP_DIR=web-app
ARG VERSION=2.7.0-snapshot

#Copying application
WORKDIR /$APP_DIR
ARG JAR_FILE=target/web-app-3.0.0-SNAPSHOT.jar
ARG APP=app.jar
COPY $JAR_FILE $APP

#Running application
EXPOSE 8888
ENTRYPOINT java $JAVA_OPTIONS -jar app.jar