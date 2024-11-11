package mongodb.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Component
public class MongoInsertionRouter extends RouteBuilder {
    @Autowired
	private ArrayList<String[]> dataSet; 
    
    int batchSize = 10000;
	MongoClient client = MongoClients.create("mongodb://admin:aaaa999999@localhost:27017");
	MongoDatabase database = client.getDatabase("test");
    private String collectionName = "results"; // results;

    @Override
    public void configure() throws Exception {
		 from("direct:mongodb-doc-insert")
         .process(exchange -> {
            System.out.println("MongoDB Insertion Start (Document)：");
            long totalMilliseconds = 0;
            
            for (var i = 0; i < dataSet.size(); i+= batchSize) {
                var documents = getDocuments(i, batchSize);
                var times = writeDocumentsSynchronously(documents);
                
                totalMilliseconds += times;
            }
            
        	long durationInMillis = totalMilliseconds; // / 1_000_000; // Total duration in milliseconds

 			// 計算分鐘、秒鐘和毫秒
 			long minutes = (durationInMillis / 1000) / 60;
 			long seconds = (durationInMillis / 1000) % 60;
 			long milliseconds = durationInMillis % 1000;

 			// 輸出結果
 			System.out.println("MongoDB 執行時間: " + minutes + "分 " + seconds + "秒 " + milliseconds + "毫秒");
 			System.out.println("MongoDB 總花費時間：" + totalMilliseconds);
            
            StringBuilder sb = new StringBuilder();
            sb.append("MongoDB 執行時間: " + minutes + "分 " + seconds + "秒 " + milliseconds + "毫秒");
            sb.append("MongoDB 總花費時間：" + totalMilliseconds);
            
			exchange.getMessage().setBody(sb.toString());
         });
    }

	private List<Map<String, Object>> getDocuments(int start, int batchSize) {
        int end = Math.min(start + batchSize, dataSet.size()); // 確保不超出範圍
        List<String[]> batch = dataSet.subList(start, end);
    
        List<Map<String, Object>> documents = new ArrayList<>();
        
        var times = System.nanoTime();
        int increment = 0;
        for (String[] arr : batch) {
            Map<String, Object> document = new HashMap<>();
            
            // 遍歷 arr 中的元素，從索引 2 開始
            for (int j = 2; j < arr.length; j++) {
                document.put("ch" + (j - 1), Double.parseDouble(arr[j]));
            }
            
            //document.put("timestamp", new Date(times + increment));
            document.put("timestamp", times + increment);
            increment++;
            // 將每一個 document 加入到結果列表中
            documents.add(document);
        }
        
        return documents;
    }

	private long writeDocumentsSynchronously(List<Map<String, Object>> data) {
		for (var name : database.listCollectionNames()) {
			System.out.println(name);
		}
		
		var collection = database.getCollection(collectionName);
		
		long startTime = 0;
		try {
            List<Document> documents = new ArrayList<>();
            for (Map<String, Object> map : data) {
                documents.add(new Document(map));
            }
            startTime = System.nanoTime();
            collection.insertMany(documents);
        } catch (Exception e) {
            e.printStackTrace();
        }
		
        long endTime = System.nanoTime();
        System.out.println("寫入耗時：" + (endTime - startTime) / 1_000_000 + " ms");
        return (endTime - startTime) / 1_000_000;
	}
}
