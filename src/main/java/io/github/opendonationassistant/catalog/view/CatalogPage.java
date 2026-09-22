package io.github.opendonationassistant.catalog.view;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

@Serdeable
public record CatalogPage(
  List<CatalogItemView> items,
  int page,
  int size,
  long total
) {}
