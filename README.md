# Online Appointment scheduling microservices

## Based
#### Java + Spring Boot + Spring Security + JWT + RabbitMQ + MongoDB
<br>
<a href="https://github.com/set404/4Clients/tree/main/auth-service">Auth-Microservice</a><br>
<a href="https://github.com/set404/4Clients/tree/main/management-service">Management-Microservice</a><br>
<a href="https://github.com/set404/4Clients/tree/main/client-service">Client-Microservice</a><br>


## Swagger
http://45.159.249.5:8091/swagger-ui/index.html
<br>
<b>Select API definition (Management & Client)</b>

## Sample
<b>Clients side - </b>http://a90527jl.beget.tech/dist/
## Install
Fill in the properties file and:
```
mvn package -f auth-service/pom.xml
mvn package -f client-service/pom.xml
mvn package -f management-service/pom.xml
docker-compose up
```