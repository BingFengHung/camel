package performance;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class InfluxDbBatchWriteRouter extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    from("timer:influxdb?period=1000")
    .process(exchange -> {

      List<String> lines = new ArrayList<>();
      
      for (int i = 0; i < 100; i++) {
        String measurement = "test_measurement";
        String tags = "tagKey" + i + "=tageValue" + i;
        String fields = "value=" + Math.random();
        long timestamp = System.currentTimeMillis() * 1000000;
        
        String lineProtocol = measurement + "," + tags + " " + fields + " " + timestamp;
        lines.add(lineProtocol);
      }

      String body = String.join("\n", lines);
      
      exchange.getIn().setBody(body);
      long startTime = System.currentTimeMillis();
      exchange.getIn().setHeader("startTime", startTime);
    })
    .to("http://localhost:8086/api/v2/write?org=Self&bucket=Test&precision=ns&bridgeEndpoint=true&throwExceptionOnFailure=false&httpMethod=POST&token=ov_lucq0aqCTzawoIheig2KU9Ki7CnFOFVO23kgHFUy0myr1yowt2S_TI5seoHEExdg43fCeUpqas41XNBijEg==")
    .process(exchange -> {
      long endTime = System.currentTimeMillis();
      long duration = endTime - (long)exchange.getIn().getHeader("startTime");
      System.out.println("InfluxDB Batch write time: " + duration + " ms");
    });
  }
}
