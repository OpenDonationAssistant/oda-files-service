package io.github.opendonationassistant.files;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface FileAccessRepository
  extends CrudRepository<FileAccess, String> {
  Optional<FileAccess> findByRecipientIdAndFilenameAndBucket(
    String recipientId,
    String filename,
    String bucket
  );

  @Query(
    value = "INSERT INTO file_access (id, recipient_id, filename, bucket, allowed) " +
    "VALUES (:id, :recipientId, :filename, :bucket, true) " +
    "ON CONFLICT (recipient_id, filename, bucket) DO NOTHING",
    nativeQuery = true
  )
  void grantAccess(
    String id,
    String recipientId,
    String filename,
    String bucket
  );
}
