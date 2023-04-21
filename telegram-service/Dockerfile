FROM openjdk:19
ARG JAR_FILE=/target/*.jar
COPY ${JAR_FILE} telegram-service.jar
ENTRYPOINT ["nohup", "java","-jar","/telegram-service.jar"]