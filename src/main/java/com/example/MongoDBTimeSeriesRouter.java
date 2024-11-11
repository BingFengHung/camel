package com.example;

import java.util.ArrayList;
import java.util.Date;
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
public class MongoDBTimeSeriesRouter extends RouteBuilder {
    @Autowired
	private ArrayList<String[]> dataSet; 
	MongoClient client = MongoClients.create("mongodb://admin:aaaa999999@localhost:27017");
	MongoDatabase database = client.getDatabase("test");
    private String db = "test"; // results;
    private String collectionName = "time"; // results;
	
	
    public void configure2() throws Exception {}
    	
    @Override 
    public void configure() throws Exception {
		 //from("timer:once?repeatCount=1") 
		 from("direct:query33")
		 .process(exchange -> {
        	 System.out.println("MongoDB Insertion Start：");
        	 long totalMilliseconds = 0;
        	 for (int i = 0; i < dataSet.size(); i+= 10000) {
        		 var doc = getDocuments(i, 10000);
        		 var times = writeDocumentsSynchronously(doc);
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
         });
		 
		from("direct:query6")
		// from("timer:once?repeatCount=1")
		 .process(exchange -> {
        	 System.out.println("MongoDB Retrival Start");
        	 long totalMilliseconds = queryRecentBatches(500000, 10000);
        	 long durationInMillis = totalMilliseconds; // / 1_000_000; // Total duration in milliseconds

 			// 計算分鐘、秒鐘和毫秒
 			long minutes = (durationInMillis / 1000) / 60;
 			long seconds = (durationInMillis / 1000) % 60;
 			long milliseconds = durationInMillis % 1000;

 			// 輸出結果
 			System.out.println("MongoDB 執行時間: " + minutes + "分 " + seconds + "秒 " + milliseconds + "毫秒");
 			System.out.println("MongoDB 總花費時間：" + totalMilliseconds);
         });
		 
         //.to("mongodb:myMongoDb?database=database&collection=myCollection&operation=insert");
		//from("timer:once?repeatCount=1")
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

    public long queryRecentBatches2(int totalRecords, int batchSize) { 
        long totalDuration = 0; 
        int fetchedRecords = 0;

        while (fetchedRecords < totalRecords) {
            long startTime = System.nanoTime();
            var collection = database.getCollection(collectionName);
            
            try (var cursor = collection.find()
                    .skip(fetchedRecords) // 跳過已查詢的筆數
                    .limit(batchSize) // 加入限制數量的條件
                    .iterator()) {

                List<Document> batchDocuments = new ArrayList<>();
                while (cursor.hasNext()) {
                    batchDocuments.add(cursor.next());
                }

                if (batchDocuments.isEmpty()) {
                    break; // 如果沒有數據，退出循環
                }

                System.out.println(batchDocuments.get(0).toJson());
                fetchedRecords += batchDocuments.size();
                long endTime = System.nanoTime();

                long duration = (endTime - startTime) / 1_000_000; // 將查詢時間轉換為毫秒
                totalDuration += duration;

                System.out.println("Batch fetched: " + batchDocuments.size() + " documents in " + duration + " ms");
            }
        }

        System.out.println("Total duration for fetching " + totalRecords + " records in batches of " + batchSize + ": " + totalDuration + " ms");
        return totalDuration;
    }


    public long queryRecentBatches(int totalRecords, int batchSize) {
        long totalDuration = 0;
        var collection = database.getCollection(collectionName);
        
        long s = System.nanoTime();
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

        long e = System.nanoTime();
        long m = (e - s) / 1_000_000; // 查询时间转换为毫秒
        System.out.println("Total duration for fetching " + totalRecords + " records in batches of " + batchSize + ": " + totalDuration + " ms");
        
        return totalDuration;
    }

    
    public long queryRecentBatches3(int totalRecords, int batchSize) {
        long totalDuration = 0;
        int fetchedRecords = 0;
        var collection = database.getCollection(collectionName);
        
        long s = System.nanoTime();
        int a = 0;
        try(var cursor = collection.find().iterator()){
             while (cursor.hasNext()) {
                 //batchDocuments.add(cursor.next());
                 cursor.next();
                 a++;
             }
        } catch(Exception e) {
        }

        long e = System.nanoTime();
        long m= (e - s) / 1_000_000; // 將查詢時間轉換為毫秒
        System.out.println("一次拿全部執行了：" + m + "ms");
        
        s = System.nanoTime();
        a = 0;

     

        while (fetchedRecords < totalRecords) {
            
            List<Document> batchDocuments = new ArrayList<>();
            long startTime = System.nanoTime();
            // 查詢最近的批次資料，限制為 batchSize
            try (var cursor = collection.find()
                    .skip(fetchedRecords) // 跳過已查詢的筆數
                     .batchSize(batchSize)
                    .limit(batchSize)
                    .iterator()) {
                
                while (cursor.hasNext()) {
                    //batchDocuments.add(cursor.next());
                    cursor.next();
                }

                long endTime = System.nanoTime();
                fetchedRecords += batchSize; //batchDocuments.size();
                
                long duration = (endTime - startTime) / 1_000_000; // 將查詢時間轉換為毫秒
                totalDuration += duration;
                
                System.out.println("Batch fetched: " + batchDocuments.size() + " documents in " + duration + " ms");
            }
        }
        
        System.out.println("Total duration for fetching " + totalRecords + " records in batches of " + batchSize + ": " + totalDuration + " ms");
        return totalDuration;
    }

    
    public long queryPointsSynchronously(int start, int batchSize) {
        System.out.println("Start: " + start + ", Batch Size: " + batchSize);
        var collection = database.getCollection(collectionName);
        // 設定查詢條件，這裡假設每個 document 的 field 格式符合插入語法
        Document filter = new Document();
        for (int j = 2; j < batchSize + 2; j++) {  // 假設 "ch" 欄位在查詢中是重要的條件
            filter.append("ch" + (j - 1), new Document("$exists", true));
        }
        
        long startTime = System.nanoTime();
        
        // 執行查詢，這裡設置批次限制為 batchSize
        List<Document> documents = collection.find(filter).limit(batchSize).into(new ArrayList<>());
        
        long endTime = System.nanoTime();
        
        // 列出符合條件的文件數量
        System.out.println("Found Documents: " + documents.size());
        System.out.println("Query Duration: " + (endTime - startTime) / 1_000_000 + " ms");
        
        return (endTime - startTime) / 1_000_000;
    }
	
	private long writeDocumentsSynchronously(List<Map<String, Object>> data) {
		var collection = database.getCollection(collectionName);
		
		long startTime = 0, endTime  = 0;
		try {
            List<Document> documents = new ArrayList<>();
            for (Map<String, Object> map : data) {
                documents.add(new Document(map));
            }
            startTime = System.nanoTime();
            collection.insertMany(documents);
            endTime = System.nanoTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
		
        System.out.println("寫入耗時：" + (endTime - startTime) / 1_000_000 + " ms");
        return (endTime - startTime) / 1_000_000;
	}
	
	private List<Map<String, Object>> getDocuments(int start, int batchSize) {
        int end = Math.min(start + batchSize, dataSet.size()); // 確保不超出範圍
        List<String[]> batch = dataSet.subList(start, end);
    
        List<Map<String, Object>> documents = new ArrayList<>();
        
        //var times = System.nanoTime();
        var times = System.currentTimeMillis();
        int increment = 0;
        for (String[] arr : batch) {
            Map<String, Object> document = new HashMap<>();
            
            // 遍歷 arr 中的元素，從索引 2 開始
            for (int j = 2; j < arr.length; j++) {
                document.put("ch" + (j - 1), Double.parseDouble(arr[j]));
            }
            
            document.put("timestamp", new Date(times + increment));
            increment++;
            // 將每一個 document 加入到結果列表中
            documents.add(document);
        }
        
        return documents;
    }
}
