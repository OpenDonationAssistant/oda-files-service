package io.github.opendonationassistant.files.listener.handlers;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.files.FilesCommand;
import io.github.opendonationassistant.events.files.FilesCommand.CreateBucketCommand;
import io.micronaut.serde.ObjectMapper;
import io.minio.MakeBucketArgs;
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
public class CreateBucketCommandHandler
  extends AbstractMessageHandler<FilesCommand.CreateBucketCommand> {

  private final MinioClient minio;
  private ODALogger log = new ODALogger(this);

  public CreateBucketCommandHandler(ObjectMapper mapper, MinioClient minio) {
    super(mapper);
    this.minio = minio;
  }

  @Override
  public void handle(CreateBucketCommand message) throws IOException {
    MakeBucketArgs args = MakeBucketArgs.builder()
      .bucket(message.name())
      .build();
    try {
      minio.makeBucket(args);
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
        "Failed to create bucket",
        Map.of("name", message.name(), "error", e.getMessage())
      );
      throw new RuntimeException(e);
    }
  }
}
