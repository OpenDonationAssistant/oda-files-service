package io.github.opendonationassistant.generated;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;
import org.jspecify.annotations.Nullable;

@Controller("/generated")
public class GeneratedController {

  private final ODALogger log = new ODALogger(this);
  private final ArtClient artClient;
  private final String token;
  private final int maxAttempts;
  private final long pollDelayMillis;

  public GeneratedController(
    ArtClient artClient,
    @Value("${art.token}") String token,
    @Value("${art.poll.max-attempts:10}") int maxAttempts,
    @Value("${art.poll.delay-millis:500}") long pollDelayMillis
  ) {
    this.artClient = artClient;
    this.token = token;
    this.maxAttempts = maxAttempts;
    this.pollDelayMillis = pollDelayMillis;
  }

  @Get(value = "{name}", produces = { MediaType.APPLICATION_OCTET_STREAM })
  @Secured(SecurityRule.IS_ANONYMOUS)
  @ExecuteOn(TaskExecutors.BLOCKING)
  @io.swagger.v3.oas.annotations.Operation(hidden = true)
  public HttpResponse<?> get(@PathVariable String name) {
    @Nullable Operation operation;
    try {
      operation = poll(name);
    } catch (HttpClientResponseException e) {
      if (e.getStatus() == HttpStatus.NOT_FOUND) {
        return HttpResponse.notFound();
      }
      log.error(
        "Failed to fetch art operation",
        Map.of("name", name, "error", e.getMessage())
      );
      return HttpResponse.status(HttpStatus.BAD_GATEWAY);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Interrupted while polling art operation", Map.of("name", name));
      return HttpResponse.status(HttpStatus.SERVICE_UNAVAILABLE);
    }

    if (operation == null) {
      return HttpResponse.notFound();
    }
    if (!operation.isDone()) {
      log.info("Art operation still in progress", Map.of("name", name));
      return HttpResponse.status(HttpStatus.SERVICE_UNAVAILABLE);
    }

    Operation.OperationResponse response = operation.getResponse();
    @Nullable String image = response == null ? null : response.image();
    if (image == null) {
      log.error("Art operation completed without image", Map.of("name", name));
      return HttpResponse.status(HttpStatus.BAD_GATEWAY);
    }

    byte[] decoded;
    try {
      decoded = Base64.getDecoder().decode(image);
    } catch (IllegalArgumentException e) {
      log.error(
        "Art operation returned invalid image payload",
        Map.of("name", name, "error", e.getMessage())
      );
      return HttpResponse.status(HttpStatus.BAD_GATEWAY);
    }

    return HttpResponse.ok(
      new StreamedFile(
        new ByteArrayInputStream(decoded),
        MediaType.APPLICATION_OCTET_STREAM_TYPE
      )
    );
  }

  private @Nullable Operation poll(String name) throws InterruptedException {
    @Nullable Operation operation = null;
    for (int attempt = 0; attempt < maxAttempts; attempt++) {
      operation = artClient.operations(token, name);
      if (operation == null || operation.isDone()) {
        return operation;
      }
      if (attempt < maxAttempts - 1) {
        Thread.sleep(pollDelayMillis);
      }
    }
    return operation;
  }
}
