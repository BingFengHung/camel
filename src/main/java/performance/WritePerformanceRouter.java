package performance;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
public class WritePerformanceRouter extends RouteBuilder {

    private static final String MONGO_DB_URI = "mongodb://admin:aaaa999999@localhost:27017/test";
    private static final String INFLUX_DB_URI = "http://localhost:8086/api/v2/write?org=Self&bucket=Test&precision=ns&bridgeEndpoint=true&throwExceptionOnFailure=false&httpMethod=POST";
    private static final String INFLUX_TOKEN = "ov_lucq0aqCTzawoIheig2KU9Ki7CnFOFVO23kgHFUy0myr1yowt2S_TI5seoHEExdg43fCeUpqas41XNBijEg==";
    private String influxDBFilename; 
    private String mongoDBFilename; 
    private Integer count;
        // 設置 MongoDB 連接的 Bean
    @Bean(name = "myMongoClient2")
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://admin:aaaa999999@localhost:27017");
    }

    @Override
    public void configure() {
         // MongoDB 路由配置
        from("direct:mongodbInsert2")
            .log("Inserting data into MongoDB: ${body}")
            .to("mongodb:myMongoClient2?database=test&collection=mycollection&operation=insert"); 

        from("direct:influxdbInsert2")
            .setHeader("Authorization", constant("Token " + INFLUX_TOKEN))
            .setHeader("Content-Type", constant("text/plain; charset=utf-8"))
            .to(INFLUX_DB_URI); // 使用 http4 component 發送 HTTP 請求

        from("direct:writePerformance")
        .process(exchange -> {
            Integer threadCount = exchange.getIn().getHeader("thread", Integer.class); // 確保是 Integer 類型
            count = exchange.getIn().getHeader("count", Integer.class);
            final int THREAD_COUNT = threadCount;
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            CamelContext context = exchange.getContext();

            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(() -> {
                    influxDBFilename ="influxdb_"+ threadCount + ".txt"; 
                    mongoDBFilename="mongodb_"+ threadCount +".txt";
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

        //String jsonData = "{\"device\":\"sensor1\", \"value\":" + Math.random() * 100 + ", \"timestamp\":" + System.currentTimeMillis() + "}";

        List<String> parts = new ArrayList<>();

        for (var i = 0; i < count; i++) {
            parts.add("\"ch" + (i + 1) + "\":" + (Math.random() * 100));
        }

        String values = String.join(",", parts); // 使用 String.join 連接列表中的元素

        String jsonData = "{\"device\":\"sensor1\"," + values+ ", \"timestamp\":" + System.currentTimeMillis() + "}";

        long startTime = System.nanoTime();
        producerTemplate.sendBody("direct:mongodbInsert2", jsonData);
        long endTime = System.nanoTime();
        //System.out.println("MongoDB 插入延遲: " + (endTime - startTime) + " ms");
        System.out.println("MongoDB 插入延遲: " + (endTime - startTime) / 1_000_000 + " ms");
        log("MongoDB 插入延遲: " + (endTime - startTime) / 1_000_000 + " ms", mongoDBFilename);
    }

    private final Object lock = new Object();

    private void log(String message, String filename) { 
        synchronized(lock) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
                writer.write(message + "\n");
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }
        }
    }

    private void insertDataIntoInfluxDB(CamelContext context) {
        ProducerTemplate producerTemplate = context.createProducerTemplate();
        String influxData = "weather,sensor=sensor1 " + generateInfluxFields() + " ";// + (System.currentTimeMillis() * 1000000);


        long startTime = System.nanoTime();
        producerTemplate.sendBody("direct:influxdbInsert2", influxData);
        long endTime = System.nanoTime();
        System.out.println("InfluxDB 插入延遲: " + (endTime - startTime) / 1_000_000 + " ms");
        log("InfluxDB 插入延遲: " + (endTime - startTime) / 1_000_000 + " ms", influxDBFilename);
    }

    private String generateInfluxFields() {
        List<String> parts = new ArrayList<>();
        for (var i = 0; i < count; i++) {
            parts.add("ch" + (i + 1) + "=" + (Math.random() * 100)); // 去掉引號
        }
        return String.join(",", parts);
    }
    
}