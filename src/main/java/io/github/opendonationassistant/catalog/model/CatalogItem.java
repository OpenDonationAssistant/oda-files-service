package io.github.opendonationassistant.catalog.model;

import io.github.opendonationassistant.catalog.repository.CatalogItemData;

public class CatalogItem {

  private final CatalogItemData data;

  public CatalogItem(CatalogItemData data) {
    this.data = data;
  }

  public CatalogItemData data() {
    return this.data;
  }
}
