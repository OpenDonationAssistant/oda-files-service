package io.github.stcarolas.oda.files;

import io.micronaut.core.annotation.Nullable;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/files")
public class FilesController {

  private static final String NICKNAME_ATTRIBUTE = "preferred_username";

  private final Logger log = LoggerFactory.getLogger(FilesController.class);
  private final MinioClient minio;

  @Inject
  public FilesController(MinioClient minio) {
    this.minio = minio;
  }

  @Secured(SecurityRule.IS_ANONYMOUS)
  @Get(value = "/{name}", produces = { MediaType.APPLICATION_OCTET_STREAM })
  public byte[] get(@PathVariable String name, @Nullable Authentication auth)
    throws Exception {
    var owner = String.valueOf(auth.getAttributes().get(NICKNAME_ATTRIBUTE));
    try {
      return minio
        .getObject(GetObjectArgs.builder().bucket(owner).object(name).build())
        .readAllBytes();
    } catch (Exception e) {
      log.error("Error getting file: {}, owner: {}", name, owner, e);
      throw e;
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
    var owner = String.valueOf(auth.getAttributes().get(NICKNAME_ATTRIBUTE));
    log.info("Uploading {} for {}, public: {}", name, owner, isPublic);
    var mimeType = name.endsWith("css")
      ? "text/css"
      : MediaType.APPLICATION_OCTET_STREAM;
    var bucket = isPublic != null && isPublic ? "public" : owner;
    try (var stream = new ByteArrayInputStream(file.getBytes())) {
      minio.putObject(
        PutObjectArgs
          .builder()
          .bucket(bucket)
          .object(name)
          .contentType(mimeType)
          .stream(stream, stream.available(), -1)
          .build()
      );
    }
  }
}
