package io.github.stcarolas.oda.files;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.files.CopyFileCommand;
import io.github.opendonationassistant.events.files.CreateBucketCommand;
import io.github.opendonationassistant.events.files.FilesCommand;
import io.micronaut.messaging.annotation.MessageHeader;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import io.micronaut.serde.ObjectMapper;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.inject.Inject;
import java.util.Map;

@RabbitListener
public class FilesCommandListener {

  private final ODALogger log = new ODALogger(this);
  private final MinioClient minio;

  @Inject
  public FilesCommandListener(MinioClient minio) {
    this.minio = minio;
  }

  @Queue(io.github.opendonationassistant.rabbit.Queue.Commands.FILES)
  public void listenCommands(byte[] command, @MessageHeader String type)
    throws Exception {
    ObjectMapper.getDefault().readValue(command, FilesCommand.class);
    log.info("Received FilesCommand", Map.of("type", type));
    switch (type) {
      case "create-bucket" -> {
        CreateBucketCommand createBucketCommand = ObjectMapper.getDefault()
          .readValue(command, CreateBucketCommand.class);
        MakeBucketArgs args = MakeBucketArgs.builder()
          .bucket(createBucketCommand.getName())
          .build();
        minio.makeBucket(args);
      }
      case "copy-file" -> {
        CopyFileCommand copyFileCommand = ObjectMapper.getDefault()
          .readValue(command, CopyFileCommand.class);
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
      default -> throw new RuntimeException(
        "Unknown FilesCommand type: " + type
      );
    }
  }
}
