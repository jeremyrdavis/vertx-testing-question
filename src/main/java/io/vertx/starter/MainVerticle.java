package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

  private static final String DB_QUEUE = "db.queue";

  @Override
  public void start(Future<Void> startFuture) {

    //Create router
    Router router = Router.router(vertx);
    router.route("/").handler(routingContext -> {
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
