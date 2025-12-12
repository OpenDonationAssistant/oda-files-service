package io.github.stcarolas.oda.catalog.view;

import io.github.stcarolas.oda.catalog.model.CatalogItem;
import io.github.stcarolas.oda.catalog.repository.CatalogItemRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import java.util.List;

@Controller
public class CatalogController {

  private final CatalogItemRepository repository;

  @Inject
  public CatalogController(CatalogItemRepository repository) {
    this.repository = repository;
  }

  @Get("/catalog")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public HttpResponse<List<CatalogItemView>> getCatalog() {
    return HttpResponse.ok(
      repository
        .list()
        .stream()
        .map(CatalogItem::data)
        .map(CatalogItemView::of)
        .toList()
    );
  }
}
