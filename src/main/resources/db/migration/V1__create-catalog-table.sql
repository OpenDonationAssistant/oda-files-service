create table catalog_item (
  id varchar(255) not null,
  name varchar(255) not null,
  url text not null,
  category varchar(255) not null,
  recipient_id varchar(255) not null,
  item_type varchar(255) not null
);

insert into catalog_item (id, name, url, category, recipient_id, item_type) values ('019b13e2-eb77-7030-8d62-31b2ccdaec49', 'background-1','https://oda-shared-static-1.hb.ru-msk.vkcloud-storage.ru/images/019b1393-3f06-7861-9078-fa098244bf08.jpg', 'background', 'ODA', 'static-image');
