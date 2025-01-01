package com.joe.thoughput.influxDB;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.CamelLogger;
import org.apache.camel.support.processor.ThroughputLogger;
import org.springframework.stereotype.Component;

@Component
public class InfluxDbQueryThroughputRouter extends RouteBuilder {

  @Override

    public void configure() throws Exception { 
      CamelLogger logger = new CamelLogger("InfluxDB ThroughputLogger"); 
      ThroughputLogger throughputLogger = new ThroughputLogger(logger, 10000);

        // 查詢吞吐量測試路由
        from("timer:influxQueryTest?period=10") // 每 10 毫秒發送一次查詢請求
            .routeId("InfluxDBQueryTest")
            //.autoStartup(false) // 禁止路由自動啟動
            .threads(10)
            .process(throughputLogger)
            .process(exchange -> {
                String fluxQuery = """
                  from(bucket: "Test") 
                  |> range(start: -1h) 
                  |> filter(fn: (r) => r._measurement == "airSensors")
                  |> filter(fn: (r) => r.sensor_id == "TLM0201")
                """;
                exchange.getIn().setBody(fluxQuery);
            }) 
            .setHeader(Exchange.HTTP_METHOD, constant("POST")) 
            .setHeader("Authorization", constant("Token cMdYwxWNZmrpjBjCbzQjQ2LIsBevI-0kakNZ_jmrlvEGCChXzle3m54hEwcZwgAX-BNv6io397EbWUeLRdNgOg=="))
            .setHeader("Content-Type", constant("application/vnd.flux"))
            .setHeader("Accept", constant("application/json"))
            .to("http://localhost:8086/api/v2/query?org=Self&bucket=Test")
            //.log("Query response received: ${body}");
            //.log("Influx Query success")
            ;
    }
}
