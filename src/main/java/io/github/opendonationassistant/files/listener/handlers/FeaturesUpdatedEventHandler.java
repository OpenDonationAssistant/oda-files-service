package io.github.opendonationassistant.files.listener.handlers;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.HasRecipientId;
import io.github.opendonationassistant.files.FileAccessRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class FeaturesUpdatedEventHandler
  extends AbstractMessageHandler<
    FeaturesUpdatedEventHandler.FeaturesUpdatedEvent
  > {

  private static final String DONATION_LISTENER_FILE = "DonationListener.zip";
  private static final String DONATION_LISTENER_BUCKET = "DonationListener";

  private final ODALogger log = new ODALogger(this);
  private final FileAccessRepository repository;

  public FeaturesUpdatedEventHandler(
    ObjectMapper mapper,
    FileAccessRepository repository
  ) {
    super(mapper);
    this.repository = repository;
  }

  @Override
  public void handle(FeaturesUpdatedEvent message) throws IOException {
    var hasGamesIntegration = message
      .features()
      .stream()
      .anyMatch(
        feature ->
          "GamesIntegration".equals(feature.name()) &&
          FeaturesUpdatedEvent.FeatureStatus.ENABLED.equals(feature.status())
      );

    if (hasGamesIntegration) {
      repository.grantAccess(
        UUID.randomUUID().toString(),
        message.recipientId(),
        DONATION_LISTENER_FILE,
        DONATION_LISTENER_BUCKET
      );
      log.info(
        "Granted DonationListener access",
        Map.of("recipientId", message.recipientId())
      );
    } else {
      repository
        .findByRecipientIdAndFilenameAndBucket(
          message.recipientId(),
          DONATION_LISTENER_FILE,
          DONATION_LISTENER_BUCKET
        )
        .ifPresent(access -> {
          repository.delete(access);
          log.info(
            "Revoked DonationListener access",
            Map.of("recipientId", message.recipientId())
          );
        });
    }
  }

  @Serdeable
  public static record FeaturesUpdatedEvent(
    String recipientId,
    List<Feature> features
  )
    implements HasRecipientId {
    @Serdeable
    public static record Feature(String name, FeatureStatus status) {}

    @Serdeable
    public static enum FeatureStatus {
      ENABLED,
      DISABLED,
    }
  }
}
