package io.github.opendonationassistant.catalog.repository;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.PageableRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface CatalogItemDataRepository
  extends PageableRepository<CatalogItemData, String> {
  Page<CatalogItemData> findByCategory(String category, Pageable pageable);
}
