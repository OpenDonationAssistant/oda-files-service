package io.github.opendonationassistant.catalog.view;

import io.github.opendonationassistant.catalog.model.CatalogItem;
import io.github.opendonationassistant.catalog.repository.CatalogItemRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

@Controller
public class CatalogController {

  private static final int MAX_PAGE_SIZE = 100;

  private final CatalogItemRepository repository;

  @Inject
  public CatalogController(CatalogItemRepository repository) {
    this.repository = repository;
  }

  @Get("/catalog")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public HttpResponse<CatalogPage> getCatalog(
    @Nullable @QueryValue("category") String category,
    @QueryValue(value = "page", defaultValue = "1") int page,
    @QueryValue(value = "size", defaultValue = "20") int size
  ) {
    int safePage = Math.max(page, 1);
    int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    var pageable = Pageable.from(safePage - 1, safeSize);
    Page<CatalogItemView> result = Optional.ofNullable(category)
      .map(value -> repository.listByCategory(value, pageable))
      .orElseGet(() -> repository.list(pageable))
      .map(CatalogItem::data)
      .map(CatalogItemView::of);
    return HttpResponse.ok(
      new CatalogPage(
        result.getContent(),
        safePage,
        safeSize,
        result.getTotalSize()
      )
    );
  }
}
