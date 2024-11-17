package performance;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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

import org.bson.Document;

@Component
public class WritePerformanceRouter extends RouteBuilder {

    private static final String MONGO_DB_URI = "mongodb://admin:aaaa999999@localhost:27017/test";
    private static final String INFLUX_DB_URI = "http://localhost:8086/api/v2/write?org=Self&bucket=Test&precision=ns&bridgeEndpoint=true&throwExceptionOnFailure=false&httpMethod=POST";
    private static final String INFLUX_TOKEN = "ov_lucq0aqCTzawoIheig2KU9Ki7CnFOFVO23kgHFUy0myr1yowt2S_TI5seoHEExdg43fCeUpqas41XNBijEg==";
    private String influxDBFilename; 
    private String mongoDBFilename; 
    private String mongoDBTsFilename; 
    private Integer count;
        // 設置 MongoDB 連接的 Bean
    @Bean(name = "myMongoClient2")
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://admin:aaaa999999@localhost:27017");
    }

    @Override
    public void configure() {
        from("direct:mongoDBTimeSeriesInsert")
            .process(exchange ->{ 
                long startTime = System.nanoTime();  
                exchange.setProperty("startTime", startTime);
                System.out.println("~~~~~~");
            })
            .log("Inserting data into MongoDB Time Series: ${body}")
            .to("mongodb:myMongoClient2?database=test&collection=ts&operation=insert")
            .process(exchange -> {
                long startTime = exchange.getProperty("startTime", Long.class);
                long endTime = System.nanoTime();
                long latency = (endTime - startTime) / 1_000_000;
                log(String.valueOf(latency), mongoDBTsFilename);
                System.out.println("MongoDBTs 插入延遲: " + (endTime - startTime) / 1_000_000 + " ms");
            });

         // MongoDB 路由配置
        from("direct:mongodbInsert2")
            .process(exchange ->{ 
                long startTime = System.nanoTime();  
                exchange.setProperty("startTime", startTime);
            })
            .log("Inserting data into MongoDB: ${body}")
            .to("mongodb:myMongoClient2?database=test&collection=mycollection&operation=insert")
            .process(exchange -> {
                long startTime = exchange.getProperty("startTime", Long.class);
                long endTime = System.nanoTime();
                long latency = (endTime - startTime) / 1_000_000;
                log(String.valueOf(latency), mongoDBFilename);
                System.out.println("MongoDB 插入延遲: " + (endTime - startTime) / 1_000_000 + " ms");
            });

        from("direct:influxdbInsert2")
            .process(exchange ->{ 
                long startTime = System.nanoTime();  
                exchange.setProperty("startTime", startTime);
            })
            .setHeader("Authorization", constant("Token " + INFLUX_TOKEN))
            .setHeader("Content-Type", constant("text/plain; charset=utf-8"))
            .to(INFLUX_DB_URI) 
            .process(exchange -> {
                long startTime = exchange.getProperty("startTime", Long.class);
                long endTime = System.nanoTime();
                long latency = (endTime - startTime) / 1_000_000;
                log(String.valueOf(latency), influxDBFilename);
                System.out.println("InfluxDB 插入延遲: " + (endTime - startTime) / 1_000_000 + " ms");
            });

        from("direct:writePerformance")
        .process(exchange -> {
            Integer threadCount = exchange.getIn().getHeader("thread", Integer.class); // 確保是 Integer 類型
            count = exchange.getIn().getHeader("count", Integer.class);
            final int THREAD_COUNT = threadCount;
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            CamelContext context = exchange.getContext();

            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(() -> {
                    influxDBFilename ="influxdb_t"+ threadCount + "d" + count + ".txt"; 
                    mongoDBFilename="mongodb_t"+ threadCount + "d" + count + ".txt"; 
                    mongoDBTsFilename="mongodbTs_t"+ threadCount + "d" + count + ".txt"; 

                    //insertDataIntoMongoDB(context);
                    //insertDataIntoInfluxDB(context);
                    insertDataIntoMongoDBTs(context);
                });
            }

            executor.shutdown(); // 關閉執行緒池
            while (!executor.isTerminated()) {}
            });
    }
    
    private void insertDataIntoMongoDBTs(CamelContext context) {
        ProducerTemplate producerTemplate = context.createProducerTemplate();

        List<String> parts = new ArrayList<>();

        for (var i = 0; i < count; i++) {
            parts.add("\"ch" + (i + 1) + "\":" + (Math.random() * 100));
        }

        String values = String.join(",", parts); // 使用 String.join 連接列表中的元素

        String jsonData = "{" + "\"device\":\"sensor1\"," + values + ", \"timestamp\": {\"$date\": \"" + new Date().toInstant().toString() + "\"}}";

        Document dbObject = Document.parse(jsonData); 
        producerTemplate.sendBody("direct:mongoDBTimeSeriesInsert", dbObject);
        //producerTemplate.sendBody("direct:mongodbTsInsert2", jsonData);
    }

    private void insertDataIntoMongoDB(CamelContext context) {
        ProducerTemplate producerTemplate = context.createProducerTemplate();

        List<String> parts = new ArrayList<>();

        for (var i = 0; i < count; i++) {
            parts.add("\"ch" + (i + 1) + "\":" + (Math.random() * 100));
        }

        String values = String.join(",", parts); // 使用 String.join 連接列表中的元素

        String jsonData = "{\"device\":\"sensor1\"," + values+ ", \"timestamp\":" + System.currentTimeMillis() + "}";

        producerTemplate.sendBody("direct:mongodbInsert2", jsonData);
        //log("MongoDB 插入延遲: " + (endTime - startTime) / 1_000_000 + " ms", mongoDBFilename);
        //log((endTime - startTime) / 1_000_000 + "", mongoDBFilename);
    }

    private final Object lock = new Object();

    private void log(String message, String filename) { 
        synchronized(lock) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(new java.io.File("test_data",filename), true))) {
                writer.write(message + "\n");
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }
        }
    }

    private void insertDataIntoInfluxDB(CamelContext context) {
        ProducerTemplate producerTemplate = context.createProducerTemplate();
        String influxData = "weather,sensor=sensor1 " + generateInfluxFields() + " ";// + (System.currentTimeMillis() * 1000000);

        //long startTime = System.nanoTime();
        producerTemplate.sendBody("direct:influxdbInsert2", influxData);
        //long endTime = System.nanoTime();
        //log("InfluxDB 插入延遲: " + (endTime - startTime) / 1_000_000 + " ms", influxDBFilename);
        //log((endTime - startTime) / 1_000_000 + "", influxDBFilename);
    }

    private String generateInfluxFields() {
        List<String> parts = new ArrayList<>();
        for (var i = 0; i < count; i++) {
            parts.add("ch" + (i + 1) + "=" + (Math.random() * 100)); // 去掉引號
        }
        return String.join(",", parts);
    }
    
}