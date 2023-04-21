FROM openjdk:19
ARG JAR_FILE=/target/*.jar
COPY ${JAR_FILE} management-service.jar
ENTRYPOINT ["nohup", "java","-jar","/management-service.jar"]