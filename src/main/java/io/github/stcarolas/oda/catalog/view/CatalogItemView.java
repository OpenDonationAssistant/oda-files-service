package io.github.stcarolas.oda.catalog.view;

import io.github.stcarolas.oda.catalog.repository.CatalogItemData;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record CatalogItemView(
  String id,
  String name,
  String recipientId,
  String category,
  String type,
  String url
) {
  public static CatalogItemView of(CatalogItemData item) {
    return new CatalogItemView(
      item.id(),
      item.name(),
      item.recipientId(),
      item.category(),
      item.type(),
      item.url()
    );
  }
}
