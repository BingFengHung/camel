package com.joe.thoughput.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.CamelLogger;
import org.apache.camel.support.processor.ThroughputLogger;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Component
public class MongoDbWriteThroughputRouter extends RouteBuilder {
  String mongoUri = "mongodb://admin:aaaa999999@localhost:27017";
  String database = "test";
  String collection = "auo";

  @Bean(name = "myMongoClient2") 
  public MongoClient mongoClient() {
        return MongoClients.create("mongodb://admin:aaaa999999@localhost:27017");
    }

  @Override
  public void configure() throws Exception {
    CamelLogger logger = new CamelLogger("ThroughputLogger");
    var throughputLogger = new ThroughputLogger(logger, 1000);

    from("timer:mongoWriteTest?period=10")
      .routeId("MongoDBWriteTest") 
      .autoStartup(false) // 禁止路由自動啟動
      .threads(10)
      .bean(MongoTestDataGenerator.class, "generateSingleData")
      .process(throughputLogger) 
      .toF("mongodb://admin:aaaa999999@localhost:27017?database=%s&collection=%s&operation=insert", database, collection)
      //.log("Data inserted into MongoDB")
      ;
  }
}

/* @Component
class WriteThroughputProcessor implements Processor {
  private long startTime = System.nanoTime();
  private long counter = 0;

  @Override
  public void process(Exchange exchange) throws Exception {
    counter++; 
    if (counter % 1000 == 0) { // 每處理1000筆數據時記錄
            long elapsedTime = System.nanoTime() - startTime;
            double throughput = counter / (elapsedTime / 1_000_000_000.0);
            System.out.println("Query Throughput: " + throughput + " messages/second");
        }
  }
} */

@Component
class MongoTestDataGenerator {
  public Document generateSingleData() {
    return new Document()
    .append("sensor_id", "TLM0201")
    .append("temperature", Math.random())
    .append("humidity", Math.random())
    .append("co", Math.random())
    .append("timestamp", System.currentTimeMillis());
  }
  
  public List<Document> generateBatchData() {
    List<Document> batch = new ArrayList<>();
    
    for (int i = 0; i < 100; i++) {
      batch.add(new Document() 
      .append("sensor_id", "TLM0201")
      .append("temperature", Math.random())
      .append("humidity", Math.random())
      .append("co", Math.random())
      .append("timestamp", System.currentTimeMillis())
      );
    }
    
    return batch;
  }
}
