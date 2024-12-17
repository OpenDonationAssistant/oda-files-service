package io.github.stcarolas.oda.generated;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import java.util.Base64;

@Controller("/generated")
public class GeneratedController {

  private ArtClient artClient;
  private String token;

  public GeneratedController(ArtClient artClient, String token) {
    this.artClient = artClient;
    this.token = token;
  }

  @Get(value = "/{name}", produces = { MediaType.APPLICATION_OCTET_STREAM })
  public byte[] get(@PathVariable String name) {
    var result = artClient.operations(token, name);
    return Base64.getDecoder().decode(result.getResponse().image());
  }

}
