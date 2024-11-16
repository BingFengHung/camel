package performance;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Component
public class PerformanceRouter extends RouteBuilder {

    private static final String MONGO_DB_URI = "mongodb://admin:aaaa999999@localhost:27017/test";
    private static final String INFLUX_DB_URI = "http://localhost:8086/api/v2/write?org=Self&bucket=Test&precision=ns&bridgeEndpoint=true&throwExceptionOnFailure=false&httpMethod=POST";
    private static final String INFLUX_TOKEN = "0dfVlBoNGGs2Ujqki1gLeH9tKjKPijrKCBlBAtRiqPpdEqoF17nLa14vTlEYNrQFnTPmDEuOKsYKZkHm8nYcBQ==";
    
        // 設置 MongoDB 連接的 Bean
    @Bean(name = "myMongoClient")
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://admin:aaaa999999@localhost:27017");
    }

    @Override
    public void configure() {
         // MongoDB 路由配置
        from("direct:mongodbInsert")
            .log("Inserting data into MongoDB: ${body}")
            .to("mongodb:myMongoClient?database=test&collection=mycollection&operation=insert"); 

        from("direct:influxdbInsert")
            .setHeader("Authorization", constant("Token " + INFLUX_TOKEN))
            .setHeader("Content-Type", constant("text/plain; charset=utf-8"))
            .to(INFLUX_DB_URI); // 使用 http4 component 發送 HTTP 請求


        from("direct:performance")
        .process(exchange -> {
            final int THREAD_COUNT = 50;
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            CamelContext context = exchange.getContext();

            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(() -> {
                    insertDataIntoMongoDB(context);
                    insertDataIntoInfluxDB(context);
                });
            }

            executor.shutdown(); // 關閉執行緒池
            while (!executor.isTerminated()) {}
            });
    }

    private void insertDataIntoMongoDB(CamelContext context) {
        ProducerTemplate producerTemplate = context.createProducerTemplate();
        long startTime = System.currentTimeMillis();

        String jsonData = "{\"device\":\"sensor1\", \"value\":" + Math.random() * 100 + ", \"timestamp\":" + System.currentTimeMillis() + "}";

        producerTemplate.sendBody("direct:mongodbInsert", jsonData);

        long endTime = System.currentTimeMillis();
        System.out.println("MongoDB 插入延遲: " + (endTime - startTime) + " ms");
    }

    private void insertDataIntoInfluxDB(CamelContext context) {
        ProducerTemplate producerTemplate = context.createProducerTemplate();
        long startTime = System.currentTimeMillis();

        String influxData = "weather,sensor=sensor1 value=" + Math.random() * 100 + " " + System.currentTimeMillis() * 1000000;

        producerTemplate.sendBody("direct:influxdbInsert", influxData);

        long endTime = System.currentTimeMillis();
        System.out.println("InfluxDB 插入延遲: " + (endTime - startTime) + " ms");
    }
}