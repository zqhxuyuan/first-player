# --- First database schema

# --- !Ups

set ignorecase true;

create table company (
  id                        bigint not null,
  name                      varchar(255) not null,
  constraint pk_company primary key (id))
;

create table computer (
  id                        bigint not null,
  name                      varchar(255) not null,
  introduced                timestamp,
  discontinued              timestamp,
  company_id                bigint,
  constraint pk_computer primary key (id))
;

create sequence company_seq start with 1000;

create sequence computer_seq start with 1000;

alter table computer add constraint fk_computer_company_1 foreign key (company_id) references company (id) on delete restrict on update restrict;
create index ix_computer_company_1 on computer (company_id);


CREATE TABLE `transactions` (
  `id` bigint not null AUTO_INCREMENT primary key,
  `symbol` varchar(10) DEFAULT NULL,
  `ttype` varchar(10) DEFAULT NULL,
  `quantity` int(10) DEFAULT NULL,
  `date_time` timestamp not null default now(),
  `notes` varchar(20) DEFAULT NULL
);

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists company;

drop table if exists computer;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists company_seq;

drop sequence if exists computer_seq;

