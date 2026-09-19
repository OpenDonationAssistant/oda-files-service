package io.github.stcarolas.oda.generated;

import static io.micronaut.http.HttpHeaders.CONTENT_TYPE;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.client.annotation.Client;

@Client(id = "artmodel")
@Header(name = CONTENT_TYPE, value = "application/json")
public interface ArtClient {
  @Get("/operations/{id}")
  public Operation operations(
    @Header(name = "Authorization") String Authorization,
    @PathVariable String id
  );
}
