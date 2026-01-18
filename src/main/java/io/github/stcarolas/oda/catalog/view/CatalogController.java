package io.github.stcarolas.oda.catalog.view;

import io.github.stcarolas.oda.catalog.model.CatalogItem;
import io.github.stcarolas.oda.catalog.repository.CatalogItemRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

@Controller
public class CatalogController {

  private final CatalogItemRepository repository;

  @Inject
  public CatalogController(CatalogItemRepository repository) {
    this.repository = repository;
  }

  @Get("/catalog")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public HttpResponse<List<CatalogItemView>> getCatalog(
    @Nullable @QueryValue("category") String category
  ) {
    return HttpResponse.ok(
      Optional.ofNullable(category)
        .map(repository::listByCategory)
        .orElseGet(() -> repository.list())
        .stream()
        .map(CatalogItem::data)
        .map(CatalogItemView::of)
        .toList()
    );
  }
}
