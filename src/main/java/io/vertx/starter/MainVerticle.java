package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class MainVerticle extends AbstractVerticle {

  private static final String CONFIG_HTTP = "config.http";

  @Override
  public void start(Future<Void> startFuture) {

    // configuration options for all Verticles
    JsonObject configOptions = new JsonObject()
      .put("queue.name.db", "db.queue")
      .put("config-message", "hello, config!");

    Future<String> httpVerticleFuture = Future.future();
    vertx.deployVerticle(HttpVerticle.class,
      new DeploymentOptions().setConfig(configOptions),
      httpVerticleFuture.completer());

    // chain/compose the Verticles' start methods
    httpVerticleFuture.compose(id -> {
      vertx.deployVerticle(DBVerticle::new, new DeploymentOptions().setConfig(configOptions),
        httpVerticleFuture.completer());
      return httpVerticleFuture;
    }).setHandler(ar -> {
      if (ar.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(ar.cause());
      }
    });

  }
}
