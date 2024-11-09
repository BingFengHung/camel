package com.example;

import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class MongoDBRouter extends RouteBuilder {
	@Override
    public void configure() {
//        // MongoDB collection
//        String mongoUri = "mongodb:myMongoBean?database=test&collection=temperature_data&operation=save";
//
//        // 寫入資料至 MongoDB
//        from("timer:write?period=10000")
//        .autoStartup(false)
//            .process(exchange -> {
//                Document document = new Document();
//                document.append("location", "room1");
//                document.append("value", 23.5);
//                document.append("timestamp", System.currentTimeMillis());
//                exchange.getMessage().setBody(document);
//            })
//            .to(mongoUri)
//            .log("資料已成功寫入 MongoDB");
//
//        // 查詢資料
//        from("timer:query?period=20000")
//        .autoStartup(false)
//            .setHeader("CamelMongoDbCriteria", constant("{ location: 'room1' }"))
//            .to("mongodb:myMongoBean?database=test&collection=temperature_data&operation=findAll")
//            .log("查詢結果：${body}");
    }
}
