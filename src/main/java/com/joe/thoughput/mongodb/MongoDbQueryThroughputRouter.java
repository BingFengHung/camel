package com.joe.thoughput.mongodb;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.CamelLogger;
import org.apache.camel.support.processor.ThroughputLogger;
import org.bson.Document;
import org.springframework.stereotype.Component;


@Component
public class MongoDbQueryThroughputRouter extends RouteBuilder {
  String mongoUri = "mongodb://admin:aaaa999999@localhost:27017";
  String database = "test";
  String collection = "auo";

  @Override
  public void configure() throws Exception {
    CamelLogger logger = new CamelLogger("MongoDB Query ThroughputLogger");
    var throughputLogger = new ThroughputLogger(logger, 10000);

    from("timer:mongoQueryTest?period=10")
      .routeId("MongoDBQueryTest") 
      .threads(10)
      .bean(MongoTestDataGenerator.class, "generateSingleData")
      .process(throughputLogger) 
      .toF("mongodb://admin:aaaa999999@localhost:27017?database=%s&collection=%s&operation=findAll", database, collection)
      //.log("Data inserted into MongoDB")
      ;
  }
}


@Component
class MongoQueryGenerator {
    public Document generateQuery() {
        // 查詢條件：特定 sensor_id 和最近一小時數據
        return new Document("sensor_id", "TLM0201")
            .append("timestamp", new Document("$gte", System.currentTimeMillis() - 3600 * 1000));
    }
}
