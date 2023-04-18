# Online Appointment scheduling software


## Based
#### Java + Spring Boot + Spring Security + JWT + RabbitMQ + MongoDB
# Swagger
http://45.159.249.5:4444/swagger-ui/index.html#/
<br>

# Install
```
mvn package -f auth-service/pom.xml
mvn package -f client-service/pom.xml
mvn package -f management-service/pom.xml
docker-compose up
```