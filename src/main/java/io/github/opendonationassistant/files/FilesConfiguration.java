package io.github.stcarolas.oda.files;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import io.minio.MinioClient;

@Factory
public class FilesConfiguration {

  @Bean
  public MinioClient produceClient(
    @Value("${minio.endpoint}") String endpoint,
    @Value("${minio.username}") String username,
    @Value("${minio.password}") String password
  ) {
    return MinioClient
      .builder()
      .endpoint(endpoint)
      .credentials(username, password)
      .build();
  }
}
