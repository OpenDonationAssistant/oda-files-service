package io.github.stcarolas.oda.config;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;
import lombok.SneakyThrows;

@Controller("/files")
public class FilesController {

  private static final String NICKNAME_ATTRIBUTE = "preferred_username";

  MinioClient minio;

  public FilesController(
    @Value("${minio.endpoint}") String endpoint,
    @Value("${minio.username}") String username,
    @Value("${minio.password}") String password
  ) {
    this.minio =
      MinioClient
        .builder()
        .endpoint(endpoint)
        .credentials(username, password)
        .build();
  }

  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Get(value = "/{name}", produces = { MediaType.APPLICATION_OCTET_STREAM })
  @SneakyThrows
  public byte[] get(@PathVariable String name, Authentication auth) {
    return minio
      .getObject(
        GetObjectArgs
          .builder()
          .bucket(String.valueOf(auth.getAttributes().get(NICKNAME_ATTRIBUTE)))
          .object(name)
          .build()
      )
      .readAllBytes();
  }

  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Put(
    value = "/{name}",
    consumes = { MediaType.MULTIPART_FORM_DATA },
    produces = { MediaType.TEXT_PLAIN }
  )
  @SneakyThrows
  public void put(
    @PathVariable String name,
    CompletedFileUpload file,
    Authentication auth
  ) {
    try (var stream = new ByteArrayInputStream(file.getBytes())) {
      minio.putObject(
        PutObjectArgs
          .builder()
          .bucket(String.valueOf(auth.getAttributes().get(NICKNAME_ATTRIBUTE)))
          .object(name)
          .stream(stream, stream.available(), -1)
          .build()
      );
    }
  }
}
