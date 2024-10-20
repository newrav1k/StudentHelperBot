create table students
(
    id         bigserial primary key,
    first_name varchar(64),
    last_name  varchar(64)
);

create table directories
(
    id         bigserial primary key,
    title      varchar(128) not null,
    student_id bigint references students (id) on delete cascade ,
    UNIQUE (title, student_id)
);

create table files
(
    id           bigserial primary key,
    title        varchar(128) not null,
    content      bytea        not null,
    directory_id bigint references directories (id) on delete cascade,
    UNIQUE (title, directory_id)
);