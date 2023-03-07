CREATE TABLE clients
(
    client_id INT PRIMARY KEY AUTO_INCREMENT,
    name      VARCHAR(50) NOT NULL,
    phone     VARCHAR(20) NOT NULL
);

CREATE TABLE therapists
(
    therapist_id INT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(50)  NOT NULL,
    phone        VARCHAR(20)  NOT NULL,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(255) NOT NULL
);

CREATE TABLE services
(
    service_id   INT PRIMARY KEY AUTO_INCREMENT,
    THERAPIST_ID INTEGER        not null
        references THERAPISTS,
    name         VARCHAR(100)   NOT NULL,
    description  TEXT,
    duration     INT            NOT NULL,
    price        DECIMAL(10, 2) NOT NULL
);

CREATE TABLE appointments
(
    appointment_id INT PRIMARY KEY AUTO_INCREMENT,
    client_id      INT      NOT NULL,
    therapist_id   INT      NOT NULL,
    service_id     INT      NOT NULL,
    start_time     DATETIME NOT NULL,
    end_time       DATETIME NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients (client_id),
    FOREIGN KEY (therapist_id) REFERENCES therapists (therapist_id),
    FOREIGN KEY (service_id) REFERENCES services (service_id)
);

CREATE TABLE availability
(
    availability_id INT PRIMARY KEY AUTO_INCREMENT,
    therapist_id    INT      NOT NULL,
    start_time      DATETIME NOT NULL,
    end_time        DATETIME NOT NULL,
    FOREIGN KEY (therapist_id) REFERENCES therapists (therapist_id)
);