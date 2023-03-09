create table CLIENTS
(
    CLIENT_ID INTEGER auto_increment
        primary key,
    NAME      CHARACTER VARYING(50) not null,
    PHONE     CHARACTER VARYING(20) not null
);

create table THERAPISTS
(
    THERAPIST_ID INTEGER auto_increment
        primary key,
    NAME         CHARACTER VARYING(50)  not null,
    PHONE        CHARACTER VARYING(20)  not null,
    PASSWORD     CHARACTER VARYING(255) not null,
    ROLE         CHARACTER VARYING(255) not null
);

create table AVAILABILITY
(
    AVAILABILITY_ID INTEGER auto_increment
        primary key,
    THERAPIST_ID    INTEGER not null
        references THERAPISTS ON DELETE CASCADE,
    START_TIME      TIME    not null,
    END_TIME        TIME    not null,
    AVAILABLE_DATE  DATE    not null,
    ISFULL          BOOLEAN default FALSE
);

create table SERVICES
(
    SERVICE_ID   INTEGER auto_increment
        primary key,
    NAME         CHARACTER VARYING(100) not null,
    DESCRIPTION  CHARACTER LARGE OBJECT,
    DURATION     INTEGER                not null,
    PRICE        NUMERIC(10, 2)         not null,
    THERAPIST_ID INTEGER                not null
        references THERAPISTS ON DELETE CASCADE
);

create table APPOINTMENTS
(
    APPOINTMENT_ID INTEGER auto_increment
        primary key,
    CLIENT_ID      INTEGER   not null
        references CLIENTS ON DELETE CASCADE,
    THERAPIST_ID   INTEGER   not null
        references THERAPISTS ON DELETE CASCADE,
    SERVICE_ID     INTEGER   not null
        references SERVICES ON DELETE CASCADE,
    START_TIME     TIMESTAMP not null
);

