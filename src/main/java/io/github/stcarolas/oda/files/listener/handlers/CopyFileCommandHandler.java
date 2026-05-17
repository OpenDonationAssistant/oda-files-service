package io.github.stcarolas.oda.files.listener.handlers;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.files.FilesCommand.CopyFileCommand;
import io.micronaut.serde.ObjectMapper;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Singleton
public class CopyFileCommandHandler
  extends AbstractMessageHandler<CopyFileCommand> {

  private final MinioClient minio;
  private ODALogger log = new ODALogger(this);

  public CopyFileCommandHandler(ObjectMapper mapper, MinioClient minio) {
    super(mapper);
    this.minio = minio;
  }

  @Override
  public void handle(CopyFileCommand message) throws IOException {
    try {
      minio.copyObject(
        CopyObjectArgs.builder()
          .source(
            CopySource.builder()
              .bucket(message.source().bucketName())
              .object(message.source().fileName())
              .build()
          )
          .bucket(message.destination().bucketName())
          .object(message.destination().fileName())
          .build()
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
      | IllegalArgumentException
      | IOException e
    ) {
      log.error(
        "Failed to create bucket",
        Map.of("command", message, "error", e.getMessage())
      );
      throw new RuntimeException(e);
    }
  }
}
