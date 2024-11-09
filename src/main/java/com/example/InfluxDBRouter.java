package com.example;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxTable;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import com.influxdb.client.write.Point;
import java.time.Instant;

@Component
public class InfluxDBRouter extends RouteBuilder {
	@Autowired
    private InfluxDBClient influxDBClient;
	
	@Override
    public void configure() {
        String bucket = "Test";
        String org = "Self";
        

        /*
        from("timer:write?period=10000")
        .process(exchange -> {
            Point point = Point
                .measurement("temperature")
                .addTag("location", "room1")
                .addField("value", 23.5)
                .time(Instant.now(), WritePrecision.NS);
            exchange.getMessage().setBody(point);
        })
        .to("influxdb2://influxDBClient?bucket=" + bucket + "&org=" + org)
        .log("資料已成功寫入 InfluxDB");
        */
        

        
        /*
        from("timer:query?period=20000")
        .process(exchange -> {
            QueryApi queryApi = influxDBClient.getQueryApi();
            String fluxQuery = "from(bucket: \"" + bucket + "\") |> range(start: -1h)";
            List<FluxTable> tables = queryApi.query(fluxQuery, org);
            
            StringBuilder result = new StringBuilder();
            for (FluxTable table : tables) {
                table.getRecords().forEach(record -> {
                    result.append("Time: ").append(record.getTime()).append(", ");
                    result.append("Fields: ").append(record.getValues()).append("\n");
                });
            }
            
            exchange.getMessage().setBody(result.toString());
        })
        .log("查詢結果：${body}");
        
        
        
    
*/
        
        
        
        // 寫入資料至 InfluxDB
//      from("timer:write?period=10000")
//          .setBody().constant("temperature,location=room1 value=23.5")
//          .to("influxdb2://influxDBClient?bucket=" + bucket + "&org=" + org)
//          .log("資料已成功寫入 InfluxDB");
//      
      

      // 查詢資料
//      from("timer:query?period=20000")
//          .process(exchange -> {
//              QueryApi queryApi = influxDBClient.getQueryApi();
//              String fluxQuery = "from(bucket: \"" + bucket + "\") |> range(start: -1h)";
//              List<FluxTable> tables = queryApi.query(fluxQuery, org);
//              exchange.getMessage().setBody(tables.toString());
//          })
//          .log("查詢結果：${body}");
      
    }
}
