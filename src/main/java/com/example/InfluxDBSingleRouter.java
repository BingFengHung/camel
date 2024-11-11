package com.example;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

@Component
public class InfluxDBSingleRouter extends RouteBuilder {
	@Autowired
	private InfluxDBClient influxDBClient;
	private ArrayList<String[]> dataSet = null; //new Sampling().GetData("influxdb_test_data.csv");
	WriteApiBlocking writeApi;
	String bucket = "Test";
	String org = "Self";

	
	private List<Long> GetTime(int start, int batchSize) {
		int end = Math.min(start + batchSize, dataSet.size()); // 確保不超出範圍
		List<String[]> batch = dataSet.subList(start, end);
		
		List<Long> result = new ArrayList<>();
		for (int i = 0; i < batchSize; i++) {
			String[] arr = batch.get(i);
			result.add(Long.parseLong(arr[0]));
		}
		
		return result;
	}
	
	private String GetFilename(int start, int batchSize) {
		int end = Math.min(start + batchSize, dataSet.size()); // 確保不超出範圍
		List<String[]> batch = dataSet.subList(start, end);
		String[] arr = batch.get(0);
		return arr[1];
	}
	
	private List<Map<String, Object>> GetD(int start, int batchSize) {
		int end = Math.min(start + batchSize, dataSet.size()); // 確保不超出範圍
		List<String[]> batch = dataSet.subList(start, end);
	
		List<Map<String, Object>> result = new ArrayList<>();
		
		for (int i = 0; i < batchSize; i++) {
			String[] arr = batch.get(i);
			Map<String, Object> myMap = new HashMap<>();
			
			// 遍歷 arr 中的元素，從索引 2 開始
			for (int j = 2; j < arr.length; j++) {
				String formattedNumber;

				if ((j-1) < 10) {
				    formattedNumber = String.format("0%d", j-1);
				} else {
				    formattedNumber = String.valueOf(j-1);
				}
				myMap.put("ch" + formattedNumber, Double.parseDouble(arr[j]));
		    }
			
			// 將每一個 myMap 加入到結果列表中
		    result.add(myMap);
		}
		
		return result;
	}
	
	public long queryPointsSynchronoulsly(int times) {
		QueryApi queryApi = influxDBClient.getQueryApi();
		long startTime = 0;
		startTime = System.nanoTime();
		
		String fluxQuery = "from(bucket: \"Test\")\n" +
                "  |> range(start: -30d)\n" +
                "  |> filter(fn: (r) =>\n" +
                "      r._measurement == \"results\" and r.file_name == \"file_" + times + "\"\n" +
                "  )";

		
		List<FluxTable> tables = queryApi.query(fluxQuery, org);
		System.out.println("Table Size：" +tables.get(0).getRecords().size());
		
		
		
        long endTime = System.nanoTime();
	    
	    return  (endTime - startTime) / 1_000_000;
	}
	
	public long queryPointsSynchronoulsly2(int times) {
			System.out.println("Times:" + times);
			QueryApi queryApi = influxDBClient.getQueryApi();
			long startTime = 0;
			startTime = System.nanoTime();
			
			String fluxQuery = "from(bucket: \"Test\")\n" +
	                "  |> range(start: -30d)\n" +
	                "  |> filter(fn: (r) =>\n" +
	                "      r._measurement == \"results\" and r.file_name == \"file_" + times + "\"\n" +
	                "  )";

			
			queryApi.queryRaw(fluxQuery, org);
			//System.out.println("Table Size：" +tables.get(0).getRecords().size());
			
			
			
	        long endTime = System.nanoTime();
		    
		    return  (endTime - startTime) / 1_000_000;
		}
	
	

	public long writePointsSynchronously(List<Point> points) {
		long startTime = 0;
		try {
	      WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
	      startTime = System.nanoTime();
	      writeApi.writePoints(points);
		} catch(Exception e) {
			e.printStackTrace();
		}
	    long endTime = System.nanoTime();
	    
	    //System.out.println("寫入耗時：" + (endTime - startTime) / 1_000_000 + " ms");
	    return  (endTime - startTime) / 1_000_000;
	}
	
	private List<Point> GetPoints(String result, List<Long> times,List<Map<String, Object>> result2) {
		 List<Point> points = new ArrayList<>();
		 
		 Instant baseTime = Instant.now(); // 基礎時間

		 
		 for (int i = 0; i < 10000; i++) {
			 var line = result;
			 Point point = Point
					 .measurement("results")
					 .addTag("file_name", line)
					 .addFields(result2.get(i))
			         .time(baseTime.plusNanos(i), WritePrecision.NS);
					 //.time(times.get(i), WritePrecision.NS);
					 //.time(time);
					 //.time(Instant.now(), WritePrecision.NS);
             points.add(point);
         }
		 
		 return points;
	}
	
	@Override
	public void configure() throws Exception {}
	
	public void configure2() throws Exception {
		writeApi = influxDBClient.getWriteApiBlocking();
		
		from("direct:query0")
		//from("timer:once?repeatCount=1")
        .process(exchange -> {
        	System.out.println("InfluxDB Query Start：");

        	long totalMilliseconds = 0;
            for (int i = 0; i < 50; i++) {
            	totalMilliseconds += queryPointsSynchronoulsly(i);
            	System.out.println(totalMilliseconds);
            }
            
            long minutes = (totalMilliseconds / 1000) / 60;
			long seconds = (totalMilliseconds / 1000) % 60;
			long milliseconds = totalMilliseconds % 1000;

			// 輸出結果
			System.out.println("InfluxDB Query 執行時間: " + minutes + "分 " + seconds + "秒 " + milliseconds + "毫秒");
			
			System.out.println("InfluxDB Query 總花費時間：" + totalMilliseconds);
        })
        .log("查詢結果：${body}");
		
		//from("timer:once?repeatCount=1")
		from("direct:query1")
		.process(ch -> {
			long totalMilliseconds = 0;
			for (int i = 0; i < dataSet.size(); i += 10000) {
				System.out.println("ININININININININ");
				var times = GetTime(i, 10000);
				var header = GetFilename(i, 10000);
				var result3 = GetD(i, 10000);
				List<Point> points = GetPoints(header, times, result3);
				System.out.println(points.get(0).toLineProtocol());
				
				System.out.println("Point: " + i / 10000 + points.size());
				//long influxStartTime =  System.nanoTime();
			    //ch.getMessage().setBody(points);
				//ch.getContext().createProducerTemplate().sendBody("influxdb2://influxDBClient?bucket=" + bucket + "&org=" + org, points);
				//for(Point point : points) {
				//ch.getContext().createProducerTemplate().sendBody("influxdb2://influxDBClient?bucket=" + bucket + "&org=" + org, points);
				//}
				//long influxEndTime = System.nanoTime(); // 记录发送到 InfluxDB 结束的时间
				var sum = writePointsSynchronously(points);
				//totalMilliseconds += influxEndTime - influxStartTime;
				totalMilliseconds += sum;
			}
			
			long durationInMillis = totalMilliseconds; // / 1_000_000; // Total duration in milliseconds

			// 計算分鐘、秒鐘和毫秒
			long minutes = (durationInMillis / 1000) / 60;
			long seconds = (durationInMillis / 1000) % 60;
			long milliseconds = durationInMillis % 1000;

			// 輸出結果
			System.out.println("InfluxDB 插入執行時間: " + minutes + "分 " + seconds + "秒 " + milliseconds + "毫秒");
			
			System.out.println("InfluxDB 插入總花費時間：" + totalMilliseconds);
		});
//		.process(ch -> {
//	        long influxStartTime = System.currentTimeMillis();  // 记录发送到 InfluxDB 开始的时间
//	        ch.getMessage().setHeader("influxStartTime", influxStartTime);
//
//	        // 发送到 InfluxDB
//	    })
//	    .to("influxdb2://influxDBClient?bucket=" + bucket + "&org=" + org)
//	    .process(ch -> {
//	    	long influxStartTime = (long) ch.getMessage().getHeader("influxStartTime");
//	        long influxEndTime = System.currentTimeMillis(); // 记录发送到 InfluxDB 结束的时间
//	        double influxDurationInMilliseconds = (influxEndTime - influxStartTime) ; 
//	        System.out.println("Writing to InfluxDB took: " + influxDurationInMilliseconds + " milliseconds");
//	    });
	}
}