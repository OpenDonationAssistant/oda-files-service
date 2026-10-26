package io.github.opendonationassistant.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "test")
class FileAccessRepositoryTest {

  private static final String FILE = "DonationListener.zip";
  private static final String BUCKET = "DonationListener";

  @Inject
  FileAccessRepository repository;

  @Test
  void grantAccessIsIdempotent() {
    // Arrange
    var recipient = "recipient-" + UUID.randomUUID();

    // Act: concurrent-safe grant called twice (as two FeaturesUpdated events would)
    repository.grantAccess(
      UUID.randomUUID().toString(),
      recipient,
      FILE,
      BUCKET
    );
    repository.grantAccess(
      UUID.randomUUID().toString(),
      recipient,
      FILE,
      BUCKET
    );

    // Assert: exactly one row exists (findBy... returns Optional, so duplicates
    // would raise NonUniqueResultException) and access is allowed
    var access = repository.findByRecipientIdAndFilenameAndBucket(
      recipient,
      FILE,
      BUCKET
    );
    assertTrue(access.isPresent());
    assertEquals(Boolean.TRUE, access.get().allowed());
  }
  
}
