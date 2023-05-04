FROM openjdk:19
ARG JAR_FILE=/target/*.jar
COPY ${JAR_FILE} telegram-service.jar
COPY src/main/resources/frontend.html /root/frontend.html
ENTRYPOINT ["nohup", "java","-jar","/telegram-service.jar"]