package io.github.stcarolas.oda.files;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("file_access")
public record FileAccess(
  @Id String id,
  String recipientId,
  String filename,
  String bucket,
  Boolean allowed
) {}
