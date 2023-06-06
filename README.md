# Online Appointment service

[TapTimes](https://taptimes.ru/) is an easy-to-use online booking system for professionals like hairdressers, massage therapists, and various other service providers.
It offers simplicity, reliability, and automation. With Tap Times, you can add a booking form to your
website or share a link with clients. The system operates through a [Telegram bot](https://t.me/clientsmanagement_bot), ensuring user-friendliness.<br>
It securely stores data, automates business processes, and allows customization of schedules and services.
Integration with your website is straightforward.<br>
Overall, Laptimes streamlines appointment management
for professionals and enhances the booking experience for clients.
<hr>

## Upcoming updates
- [x] Ability to add breaks
- [ ] Profile links on personal page
- [ ] Ability to create a Custom Vanity URL
- [ ] Ability to offer discounts or promotions within the app
- [ ] Customizable SMS templates for personalized communication with clients

<hr>

## Based
#### Java + Multi-Module Maven + Spring Boot + Spring Security + JWT + RabbitMQ + MongoDB + H2 + Telegram API
<br>
<a href="https://github.com/kotomore/TapTimes/tree/main/auth-service">Authorization-Service</a><br>
<a href="https://github.com/kotomore/TapTimes/tree/main/management-service">Management-Service</a><br>
<a href="https://github.com/kotomore/TapTimes/tree/main/client-service">Client-Service</a><br>
<a href="https://github.com/kotomore/TapTimes/tree/main/telegram-service">Telegram-Service</a><br>


## Swagger
http://45.159.249.5:8091/swagger-ui/index.html
<br>
<b>Select API definition (Authorization, Management & Client)</b>

## Sample
### Clients side
https://taptimes.ru/6441a64b008d0c1864da7227
<br>

<img src="github-images/site_image.png" alt="site-image" width="60%" height="60%">

<br><br>

### Telegram bot
https://t.me/clientsmanagement_bot
<br>

<img  src="github-images/tg_image.png" alt="tg-image" width="80%" height="80%">

## Install && launch

```
git clone https://github.com/kotomore/TapTimes.git
cd TapTimes
mvn package -f pom.xml
docker-compose up
```