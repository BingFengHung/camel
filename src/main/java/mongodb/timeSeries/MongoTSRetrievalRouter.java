package mongodb.timeSeries;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Component
public class MongoTSRetrievalRouter extends RouteBuilder {
	MongoClient client = MongoClients.create("mongodb://admin:aaaa999999@localhost:27017");
	MongoDatabase database = client.getDatabase("test");
    private String collectionName = "time"; // results;

    @Override
    public void configure() throws Exception {
         
		from("direct:mongodb-ts-retrieval")
        .process(exchange -> {
            System.out.println("MongoDB Retrival Start (Time Series):");

        	long totalMilliseconds = queryRecentBatches(500000, 10000);
        	long durationInMillis = totalMilliseconds; // / 1_000_000; // Total duration in milliseconds

 			// 計算分鐘、秒鐘和毫秒
 			long minutes = (durationInMillis / 1000) / 60;
 			long seconds = (durationInMillis / 1000) % 60;
 			long milliseconds = durationInMillis % 1000;
			StringBuilder sb = new StringBuilder();
            sb.append("MongoDB 執行時間: " + minutes + "分 " + seconds + "秒 " + milliseconds + "毫秒\n");
 			sb.append("MongoDB 總花費時間：" + totalMilliseconds);
			exchange.getMessage().setBody(sb.toString());
        });
    }

    public long queryRecentBatches(int totalRecords, int batchSize) {
        long totalDuration = 0;
        var collection = database.getCollection(collectionName);
        
        try (var cursor = collection.find().iterator()) {
            int fetchedRecords = 0;
            while (fetchedRecords < totalRecords && cursor.hasNext()) {
                List<Document> batchDocuments = new ArrayList<>();
                long startTime = System.nanoTime();
                // 获取下一批数据
                for (int i = 0; i < batchSize && cursor.hasNext(); i++) {
                    batchDocuments.add(cursor.next());
                    fetchedRecords++;
                }
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1_000_000; // 毫秒
                totalDuration += duration;
                
                System.out.println("Batch fetched: " + batchDocuments.size() + " documents in " + duration + " ms");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return totalDuration;
    }
}
