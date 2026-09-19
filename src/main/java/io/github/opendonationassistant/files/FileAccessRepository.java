package io.github.opendonationassistant.files;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface FileAccessRepository extends CrudRepository<FileAccess, String> {
  Optional<FileAccess> findByRecipientIdAndFilenameAndBucket(
    String recipientId,
    String filename,
    String bucket
  );
}
