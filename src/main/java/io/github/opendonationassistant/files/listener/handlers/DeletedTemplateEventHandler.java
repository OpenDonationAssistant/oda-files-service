package io.github.opendonationassistant.files.listener.handlers;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
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
import java.util.List;
import java.util.Map;

@Singleton
public class DeletedTemplateEventHandler
  extends AbstractMessageHandler<DeletedTemplateEventHandler.DeletedTemplate> {

  private final MinioClient minio;
  private final ODALogger log = new ODALogger(this);

  public DeletedTemplateEventHandler(ObjectMapper mapper, MinioClient minio) {
    super(mapper);
    this.minio = minio;
  }

  @Override
  public void handle(DeletedTemplate message) throws IOException {
    TemplateData templateData = message.templateData();
    String showcaseUrl = templateData.showcase();
    String bucket = templateData.ownerId();

    // Extract filename from showcase URL (last path segment)
    String filename = extractFilenameFromUrl(showcaseUrl);

    log.info(
      "Deleting template file from bucket",
      Map.of("bucket", bucket, "filename", filename, "templateId", templateData.id())
    );

    try {
      minio.removeObject(
        RemoveObjectArgs.builder()
          .bucket(bucket)
          .object(filename)
          .build()
      );
      log.info(
        "Successfully deleted template file",
        Map.of("bucket", bucket, "filename", filename)
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
        "Failed to delete template file",
        Map.of("bucket", bucket, "filename", filename, "error", e.getMessage())
      );
      throw new RuntimeException(e);
    }
  }

  private String extractFilenameFromUrl(String url) {
    if (url == null || url.isEmpty()) {
      throw new IllegalArgumentException("Showcase URL cannot be null or empty");
    }
    // Extract the last path segment after the last '/'
    int lastSlashIndex = url.lastIndexOf('/');
    if (lastSlashIndex == -1 || lastSlashIndex == url.length() - 1) {
      throw new IllegalArgumentException("Invalid showcase URL format: " + url);
    }
    return url.substring(lastSlashIndex + 1);
  }

  @Serdeable
  public static record DeletedTemplate(TemplateData templateData) {}

  @Serdeable
  public static record TemplateData(
    String id,
    String ownerId,
    String widgetType,
    String showcase,
    List<Map<String, Object>> properties,
    boolean deleted
  ) {}
}
