FROM openjdk:19
ARG JAR_FILE=/target/*.jar
COPY ${JAR_FILE} management-agentService.jar
ENTRYPOINT ["nohup", "java","-jar","/management-agentService.jar"]