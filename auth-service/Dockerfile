FROM openjdk:19
ARG JAR_FILE=/target/*.jar
COPY ${JAR_FILE} auth-agentService.jar
ENTRYPOINT ["nohup", "java","-jar","/auth-service.jar"]