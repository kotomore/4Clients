FROM openjdk:19
ARG JAR_FILE=/target/*.jar
COPY ${JAR_FILE} client-agentService.jar
ENTRYPOINT ["nohup", "java","-jar","/client-agentService.jar"]