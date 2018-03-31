package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class DBVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(DBVerticle.class);

  MongoClient mongoClient;

  private static final String DB_QUEUE = "db.queue";


  public void start(Future<Void> startFuture) throws Exception{

    System.out.println(config().getString("config-message"));

    // Configure the MongoClient inline.  This should be externalized into a config file
//    mongoClient = MongoClient.createShared(vertx, new JsonObject().put("db_name", "mydb").put("connection_string", "mongodb://127.0.0.1:37017"));
    vertx.eventBus().consumer(DB_QUEUE, this::onMessage);
    startFuture.complete();
  }

  void onMessage(Message<JsonObject> message){

    LOGGER.debug("message received");

    // if our message does not contain an action
    if(!message.headers().contains("query")){
      message.fail(1, "No Query Specified!");
    }

    switch (message.headers().get("query")){

      case "get-all":
        this.getAll(message);
    }
  }

  private void getAll(Message<JsonObject> message) {

/*
        mongoClient.find("stuff", new JsonObject(), res -> {
            if(res.succeeded()){
                System.out.println(res.result());
                message.reply(new JsonObject().put("results", Json.encodePrettily(res.result())));
            }else{
                System.out.println(res.cause());
                message.reply(new JsonObject().put("result", "failed").put("cause", res.cause().getCause()));
            }
        });
*/

  }

}
