package io.github.stcarolas.oda.catalog.repository;

import io.github.stcarolas.oda.catalog.model.CatalogItem;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.List;

@Serdeable
public class CatalogItemRepository {

  private final CatalogItemDataRepository repository;

  @Inject
  public CatalogItemRepository(CatalogItemDataRepository repository) {
    this.repository = repository;
  }

  public List<CatalogItem> list() {
    return repository.findAll().stream().map(this::convert).toList();
  }

  private CatalogItem convert(CatalogItemData data) {
    return new CatalogItem(data);
  }
}
