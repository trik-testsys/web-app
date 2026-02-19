FROM eclipse-temurin:17
MAINTAINER Roman Shishkin <romashkin.2001@yandex.ru>

#Setting directories args
ARG APP_DIR=web-app
ARG VERSION
ARG JAR_FILE=target/web-app-${VERSION}.jar
ARG APP=app.jar

#Copying application
WORKDIR /$APP_DIR
COPY $JAR_FILE $APP

#Running application
EXPOSE 8888
ENTRYPOINT java $JAVA_OPTIONS -jar app.jar
