# Online Appointment scheduling microservices

## Based
#### Java + Spring Boot + Spring Security + JWT + RabbitMQ + MongoDB + H2 + Telegram API
<br>
<a href="https://github.com/set404/4Clients/tree/main/auth-service">Auth-Microservice</a><br>
<a href="https://github.com/set404/4Clients/tree/main/management-service">Management-Microservice</a><br>
<a href="https://github.com/set404/4Clients/tree/main/client-service">Client-Microservice</a><br>
<a href="https://github.com/kotomore/4Clients/tree/main/telegram-service">Telegram-Microservice</a><br>


## Swagger
http://45.159.249.5:8091/swagger-ui/index.html
<br>
<b>Select API definition (Management & Client)</b>

## Sample
### Clients side
http://a90527jl.beget.tech/dist/
<br>

<img src="github-images/site_image.jpg">

<br><br>

### Telegram bot
https://t.me/clientsmanagement_bot
<br>

<img  src="github-images/tg_image.jpg">

## Install
>git clone, rename and fill application.properties.origin to <b>application.properties</b> file, choose project root dir

then
```
mvn package -f auth-service/pom.xml
mvn package -f client-service/pom.xml
mvn package -f management-service/pom.xml
mvn package -f telegram-service/pom.xml
docker-compose up
```