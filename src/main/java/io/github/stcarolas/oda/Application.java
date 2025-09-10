package io.github.stcarolas.oda;

import io.github.opendonationassistant.rabbit.RabbitConfiguration;
import io.micronaut.rabbitmq.connect.ChannelInitializer;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;

public class Application {

  public static void main(String[] args) {
    Micronaut.run(Application.class, args);
  }

  @Singleton
  public ChannelInitializer rabbitConfiguration() {
    return new RabbitConfiguration();
  }
}
