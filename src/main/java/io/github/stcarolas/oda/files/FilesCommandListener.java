package io.github.stcarolas.oda.files;

import io.github.opendonationassistant.RabbitConfiguration;
import io.github.opendonationassistant.events.files.CreateBucketCommand;
import io.github.opendonationassistant.events.files.FilesCommand;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import io.micronaut.serde.ObjectMapper;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RabbitListener
public class FilesCommandListener {

  private Logger log = LoggerFactory.getLogger(FilesCommandListener.class);

  private final MinioClient minio;

  @Inject
  public FilesCommandListener(MinioClient minio) {
    log.info("Created files listener");
    this.minio = minio;
  }

  @Queue(RabbitConfiguration.Queue.COMMANDS_FILES)
  public void listenCommands(FilesCommand command) throws Exception {
    log.info(
      "Received command: {}",
      ObjectMapper.getDefault().writeValueAsString(command)
    );
    switch (command) {
      case CreateBucketCommand createBucketCommand -> {
        MakeBucketArgs args = MakeBucketArgs
          .builder()
          .bucket(createBucketCommand.getName())
          .build();
        minio.makeBucket(args);
      }
      default -> throw new RuntimeException("Unknown command: " + command);
    }
  }
}
