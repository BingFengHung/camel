package influxdb;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

@Component
public class InsertionRouter extends RouteBuilder {
	@Autowired
	InfluxDBClient influxDBClient;
	
	@Autowired
	ArrayList<String[]> dataSet;

	WriteApiBlocking writeApi;
	String bucket = "Test";
	String org = "Self";
	String _measurement = "results";

	@Override
	public void configure() throws Exception {
		writeApi = influxDBClient.getWriteApiBlocking();
		
		//from("timer:once?repeatCount=1")
		from("direct:influxdb-insertion")
		.process(exchange -> {
			System.out.println("InfluxDB Insertion Start:");
			long totalMilliseconds = 0;
			int batchSize = 10000;
			
			for (var i = 0; i < dataSet.size(); i+= batchSize) {
				System.out.println("Tims: " + 500000 / batchSize);
				var filename = getFilename(i, batchSize);
				var data = getSampleData(i, batchSize);
				
				List<Point> points = getPoints(filename, data);
				
				var milliseconds = writePointsSync(points);
				totalMilliseconds += milliseconds;
			}

			long durationInMillis = totalMilliseconds;

			// 計算分鐘、秒鐘和毫秒
			long minutes = (durationInMillis / 1000) / 60;
			long seconds = (durationInMillis / 1000) % 60;
			long milliseconds = durationInMillis % 1000;

			// 輸出結果
			StringBuilder sb = new StringBuilder();
			
			sb.append("InfluxDB 插入執行時間: " + minutes + "分 " + seconds + "秒 " + milliseconds + "毫秒\n");
			sb.append("InfluxDB 插入總花費時間：" + totalMilliseconds);
			System.out.println("InfluxDB 插入執行時間: " + minutes + "分 " + seconds + "秒 " + milliseconds + "毫秒");
			System.out.println("InfluxDB 插入總花費時間：" + totalMilliseconds);
			
			exchange.getMessage().setBody(sb.toString());
		});
	}
	
	private String getFilename(int start, int batchSize) {
		int end = Math.min(start + batchSize, dataSet.size());
		List<String[]> batch = dataSet.subList(start, end);
		String[] arr = batch.get(0);
		return arr[1];
	}

	private List<Map<String, Object>> getSampleData(int start, int batchSize) {
		int end = Math.min(start + batchSize, dataSet.size());
		List<String[]> batch = dataSet.subList(start, end);
		List<Map<String, Object>> result = new ArrayList<>();

		for (var i = 0; i < batchSize; i++) {
			String[] arr = batch.get(i);
			Map<String, Object> myMap = new HashMap<>();
			
			for (int j = 2; j < arr.length; j++) {
				String formatNumber;

				if ((j - 1) < 10) formatNumber = String.format("0%d", j-1);
				else formatNumber = String.valueOf(j-1);
				myMap.put("ch" + formatNumber, Double.parseDouble(arr[j]));
			}
			result.add(myMap);
		}

		return result;
	}

	private List<Point> getPoints(String filename, List<Map<String, Object>> sample) {
		List<Point> points = new ArrayList<>();	
		Instant baseTime = Instant.now();
		
		for (int i = 0; i < 10000; i++) {
			Point point = Point
				.measurement(_measurement)
				.addTag("file_name", filename)
				.addFields(sample.get(i))
				.time(baseTime.plusNanos(i), WritePrecision.NS);
			
			points.add(point);
		}
		
		return points;
	}

	private long writePointsSync(List<Point> points) {
		long startTime = 0, endTime = 0;
		
		try {
			startTime = System.nanoTime();
			writeApi.writePoints(points);
			endTime = System.nanoTime();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return (endTime - startTime) / 1_000_000;
	}
}