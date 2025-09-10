package io.github.stcarolas.oda.files;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.files.CopyFileCommand;
import io.github.opendonationassistant.events.files.CreateBucketCommand;
import io.github.opendonationassistant.events.files.FilesCommand;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import io.micronaut.serde.ObjectMapper;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.inject.Inject;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RabbitListener
public class FilesCommandListener {

  private final ODALogger log = new ODALogger(this);
  private final MinioClient minio;

  @Inject
  public FilesCommandListener(MinioClient minio) {
    this.minio = minio;
  }

  @Queue(io.github.opendonationassistant.rabbit.Queue.Commands.FILES)
  public void listenCommands(FilesCommand command) throws Exception {
    log.info("Received command: {}", Map.of("command", command));
    switch (command) {
      case CreateBucketCommand createBucketCommand -> {
        MakeBucketArgs args = MakeBucketArgs.builder()
          .bucket(createBucketCommand.getName())
          .build();
        minio.makeBucket(args);
      }
      case CopyFileCommand copyFileCommand -> {
        minio.copyObject(
          CopyObjectArgs.builder()
            .source(
              CopySource.builder()
                .bucket(copyFileCommand.source().bucketName())
                .object(copyFileCommand.source().fileName())
                .build()
            )
            .bucket(copyFileCommand.destination().bucketName())
            .object(copyFileCommand.destination().fileName())
            .build()
        );
      }
      default -> throw new RuntimeException("Unknown command: " + command);
    }
  }
}
