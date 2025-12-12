package io.github.stcarolas.oda.catalog.model;

import io.github.stcarolas.oda.catalog.repository.CatalogItemData;

public class CatalogItem {

  private final CatalogItemData data;

  public CatalogItem(CatalogItemData data) {
    this.data = data;
  }

  public CatalogItemData data() {
    return this.data;
  }
}
