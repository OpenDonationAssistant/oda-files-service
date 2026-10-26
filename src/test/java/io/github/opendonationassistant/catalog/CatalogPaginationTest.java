package io.github.opendonationassistant.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.opendonationassistant.catalog.repository.CatalogItemData;
import io.github.opendonationassistant.catalog.repository.CatalogItemDataRepository;
import io.github.opendonationassistant.catalog.repository.CatalogItemRepository;
import io.micronaut.data.model.Pageable;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "test")
class CatalogPaginationTest {

  @Inject
  CatalogItemDataRepository dataRepository;

  @Inject
  CatalogItemRepository repository;

  @Test
  void paginatesAndFiltersByCategory() {
    // Arrange
    var category = "category-" + UUID.randomUUID();
    for (int i = 0; i < 3; i++) {
      dataRepository.save(
        new CatalogItemData(
          UUID.randomUUID().toString(),
          "item-" + i,
          "owner",
          category,
          "static-image",
          "http://example.test/" + i
        )
      );
    }

    // Act
    var firstPage = repository.listByCategory(category, Pageable.from(0, 2));
    var secondPage = repository.listByCategory(category, Pageable.from(1, 2));
    var everything = repository.list(Pageable.from(0, 100));

    // Assert
    assertEquals(2, firstPage.getContent().size());
    assertEquals(3, firstPage.getTotalSize());
    assertEquals(1, secondPage.getContent().size());
    assertEquals(3, secondPage.getTotalSize());
    assertTrue(everything.getTotalSize() >= 3);
  }
}
