package io.github.opendonationassistant.catalog.repository;

import io.github.opendonationassistant.catalog.model.CatalogItem;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;

public class CatalogItemRepository {

  private final CatalogItemDataRepository repository;

  @Inject
  public CatalogItemRepository(CatalogItemDataRepository repository) {
    this.repository = repository;
  }

  public Page<CatalogItem> listByCategory(String category, Pageable pageable) {
    return repository.findByCategory(category, pageable).map(this::convert);
  }

  public Page<CatalogItem> list(Pageable pageable) {
    return repository.findAll(pageable).map(this::convert);
  }

  private CatalogItem convert(CatalogItemData data) {
    return new CatalogItem(data);
  }
}
