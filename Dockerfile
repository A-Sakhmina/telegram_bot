FROM openjdk:8-jdk-alpine

EXPOSE 8080

COPY target/TelegramBot-0.0.1-SNAPSHOT.jar tel_bot_app.jar

CMD ["java", "-jar", "tel_bot_app.jar"]