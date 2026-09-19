package io.github.stcarolas.oda.files.listener.handlers;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import jakarta.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Singleton
public class UploadFileCommandHandler
  extends AbstractMessageHandler<UploadFileCommandHandler.UploadFileCommand> {

  private final MinioClient minio;
  private final ODALogger log = new ODALogger(this);

  public UploadFileCommandHandler(ObjectMapper mapper, MinioClient minio) {
    super(mapper);
    this.minio = minio;
  }

  @Override
  public void handle(UploadFileCommand message) throws IOException {
    var content = message.content();
    try (
      var stream = new ByteArrayInputStream(content)
    ) {
      minio.putObject(
        PutObjectArgs.builder()
          .bucket(message.recipientId())
          .object(message.filename())
          .stream(stream, content.length, -1)
          .build()
      );
      log.info(
        "Uploaded file",
        Map.of(
          "recipientId",
          message.recipientId(),
          "filename",
          message.filename(),
          "size",
          content.length
        )
      );
    } catch (
      InvalidKeyException
      | ErrorResponseException
      | InsufficientDataException
      | InternalException
      | InvalidResponseException
      | NoSuchAlgorithmException
      | ServerException
      | XmlParserException
      | IOException e
    ) {
      log.error(
        "Failed to upload file",
        Map.of(
          "recipientId",
          message.recipientId(),
          "filename",
          message.filename(),
          "error",
          e.getMessage()
        )
      );
      throw new RuntimeException(e);
    }
  }

  @Serdeable
  public static record UploadFileCommand(
    String recipientId,
    byte[] content,
    String filename
  ) {}
}
