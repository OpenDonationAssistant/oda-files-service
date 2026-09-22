alter table file_access
  add constraint file_access_pkey primary key (id);

alter table file_access
  add constraint file_access_natural_key unique (recipient_id, filename, bucket);

alter table catalog_item
  add constraint catalog_item_pkey primary key (id);

create index idx_catalog_item_category on catalog_item (category);
