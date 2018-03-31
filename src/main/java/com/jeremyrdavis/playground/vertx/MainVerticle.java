package com.jeremyrdavis.playground.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Future<Void> startFuture) {

    LOGGER.debug("MainVerticle.start()");

    // configuration options for all Verticles
    JsonObject configOptions = new JsonObject()
      .put("queue.name.db", "db.queue")
      .put("config-message", "hello, config!");

    Future<String> dbVerticleFuture = Future.future();
    vertx.deployVerticle(DBVerticle.class, new DeploymentOptions().setConfig(configOptions), dbVerticleFuture.completer());

    dbVerticleFuture.compose(id -> {
      Future<String> httpVerticleDeployment = Future.future();
      vertx.deployVerticle(
        HttpVerticle.class,  // <4>
        new DeploymentOptions().setConfig(configOptions),    // <5>
        httpVerticleDeployment.completer());

      return httpVerticleDeployment;  // <6>
    }).setHandler(res ->{
      if (res.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(res.cause());
      }

    });

    LOGGER.debug("MainVerticle started");

  }
}
