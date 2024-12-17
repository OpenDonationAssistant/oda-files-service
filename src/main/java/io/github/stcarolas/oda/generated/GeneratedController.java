package io.github.stcarolas.oda.generated;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.util.Base64;

@Controller("/generated")
public class GeneratedController {

  private ArtClient artClient;
  private String token;

  public GeneratedController(
    ArtClient artClient,
    @Value("${art.token}") String token
  ) {
    this.artClient = artClient;
    this.token = token;
  }

  @Get(value = "{name}", produces = { MediaType.APPLICATION_OCTET_STREAM })
  @Secured(SecurityRule.IS_ANONYMOUS)
  public byte[] get(@PathVariable String name) {
    var result = artClient.operations(token, name);
    return Base64.getDecoder().decode(result.getResponse().image());
  }
}
