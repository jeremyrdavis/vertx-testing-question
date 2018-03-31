package com.jeremyrdavis.playground.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class HttpVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpVerticle.class);

  private String DB_QUEUE;

  @Override
  public void start(Future<Void> startFuture){

    LOGGER.debug(config().getString("config-message"));

    //Pull configuration from initialization method
    JsonObject httpConfig;

    DB_QUEUE = config().getString("queue.name.db");

    //Create router
    Router router = Router.router(vertx);
    router.route("/").handler(routingContext -> {
      LOGGER.debug("/ called");
      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "text/html")
        .end("<h1>Hello, World!</h1>");
    });

    router.get("/api/stuff").handler(this::getAll);

    // Create the HTTP server and pass the "accept" method to the request handler.
    vertx
      .createHttpServer()
      .requestHandler(router::accept)
      .listen(
        // Retrieve the port from the configuration,
        // default to 8080.
        config().getInteger("http.port", 8080),
        result -> {
          if (result.succeeded()) {
            LOGGER.debug("HttpVerticle deployed");
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        }
      );

  }

  private void getAll(RoutingContext routingContext){
    vertx.eventBus().send(DB_QUEUE, new JsonObject(), new DeliveryOptions().addHeader("query", "get-all"), reply ->{
      if(reply.succeeded()){
        JsonObject body = (JsonObject) reply.result().body();
        routingContext.response()
          .setStatusCode(200)
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(body));
      }else {
        System.out.println(reply.cause());
        routingContext.response()
          .setStatusCode(500)
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(reply.cause()));
      }
    });

  }
}
