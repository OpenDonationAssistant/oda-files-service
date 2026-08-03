create table file_access (
    id varchar(255) not null,
    recipient_id varchar(255) not null,
    filename varchar(255) not null,
    bucket varchar(255) not null,
    allowed boolean not null
);
