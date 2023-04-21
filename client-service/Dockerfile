FROM openjdk:19
ARG JAR_FILE=/target/*.jar
COPY ${JAR_FILE} client-service.jar
ENTRYPOINT ["nohup", "java","-jar","/client-service.jar"]