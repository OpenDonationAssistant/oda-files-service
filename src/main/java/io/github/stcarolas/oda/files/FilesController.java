package io.github.stcarolas.oda.files;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;

@Controller("/files")
public class FilesController extends BaseController {

  private ODALogger log = new ODALogger(this);
  private final MinioClient minio;

  @Inject
  public FilesController(MinioClient minio) {
    this.minio = minio;
  }

  @Secured(SecurityRule.IS_ANONYMOUS)
  @Get(value = "/{name}", produces = { MediaType.APPLICATION_OCTET_STREAM })
  public HttpResponse<byte[]> get(
    @PathVariable String name,
    @Nullable Authentication auth
  ) throws Exception {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return HttpResponse.unauthorized();
    }
    try {
      return HttpResponse.ok(
        minio
          .getObject(
            GetObjectArgs.builder().bucket(owner.get()).object(name).build()
          )
          .readAllBytes()
      );
    } catch (Exception e) {
      log.error("Failed to get item", Map.of("name", name, "owner", owner));
      return HttpResponse.notFound();
    }
  }

  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Put(
    value = "/{name}",
    consumes = { MediaType.MULTIPART_FORM_DATA },
    produces = { MediaType.TEXT_PLAIN }
  )
  public void put(
    @PathVariable String name,
    @Nullable @QueryValue("public") Boolean isPublic,
    CompletedFileUpload file,
    Authentication auth
  ) throws Exception {
    var owner = getOwnerId(auth);
    if (owner.isEmpty()) {
      return;
    }
    var mimeType = name.endsWith("css")
      ? "text/css"
      : MediaType.APPLICATION_OCTET_STREAM;
    log.info(
      "Uploading file",
      Map.of(
        "name",
        name,
        "owner",
        owner,
        "isPublic",
        Optional.ofNullable(isPublic).orElse(false),
        "mime",
        mimeType
      )
    );
    var bucket = isPublic != null && isPublic ? "public" : owner.get();
    try (var stream = new ByteArrayInputStream(file.getBytes())) {
      minio.putObject(
        PutObjectArgs.builder()
          .bucket(bucket)
          .object(name)
          .contentType(mimeType)
          .stream(stream, stream.available(), -1)
          .build()
      );
    }
  }
}
