package io.github.stcarolas.oda.catalog.repository;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("catalog_item")
public record CatalogItemData(
  @Id String id,
  String recipientId,
  String category,
  @MappedProperty("item_type") String type,
  String url
) {}
