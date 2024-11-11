package influxdb;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;

@Component
public class RetrievalRouter extends RouteBuilder {
	@Autowired
	private InfluxDBClient influxDBClient;

	String bucket = "Test";
	String org = "Self";

	@Override
	public void configure() throws Exception {
		from("direct:influxdb-retrieval")
		.process(exchange -> {
			System.out.println("InfluxDB Query Start:");
        	long totalMilliseconds = 0;
            for (int i = 0; i < 50; i++) {
            	totalMilliseconds += queryPointsSynchronoulsly2(i);
            	System.out.println(totalMilliseconds);
            }
            
            long minutes = (totalMilliseconds / 1000) / 60;
			long seconds = (totalMilliseconds / 1000) % 60;
			long milliseconds = totalMilliseconds % 1000;

			System.out.println("InfluxDB Query 執行時間: " + minutes + "分 " + seconds + "秒 " + milliseconds + "毫秒");
			System.out.println("InfluxDB Query 總花費時間：" + totalMilliseconds);

			StringBuilder sb = new StringBuilder();
			sb.append("InfluxDB Query 執行時間: " + minutes + "分 " + seconds + "秒 " + milliseconds + "毫秒\n");
			sb.append("InfluxDB Query 總花費時間：" + totalMilliseconds);
			// 輸出結果
			exchange.getMessage().setBody(sb.toString());
		});
	}
	
	
	public long queryPointsSynchronoulsly2(int times) {
			System.out.println("Times:" + (times + 1));
			QueryApi queryApi = influxDBClient.getQueryApi();
			long startTime = 0;
			startTime = System.nanoTime();
			
			String fluxQuery = "from(bucket: \"Test\")\n" +
	                "  |> range(start: -30d)\n" +
	                "  |> filter(fn: (r) =>\n" +
	                "      r._measurement == \"results\" and r.file_name == \"file_" + times + "\"\n" +
	                "  )";

					System.out.println(fluxQuery);

			queryApi.queryRaw(fluxQuery, org);
			
	        long endTime = System.nanoTime();
		    
		    return  (endTime - startTime) / 1_000_000;
		}
}