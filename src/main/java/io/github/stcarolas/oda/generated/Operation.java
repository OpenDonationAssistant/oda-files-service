package io.github.stcarolas.oda.generated;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class Operation {

  private boolean done;
  private OperationResponse response;

  @Serdeable
  public static record OperationResponse(String image) {}

  public Operation(boolean done, OperationResponse response) {
    this.done = done;
    this.response = response;
  }

  public boolean isDone() {
    return done;
  }

  public OperationResponse getResponse() {
    return response;
  }
}
