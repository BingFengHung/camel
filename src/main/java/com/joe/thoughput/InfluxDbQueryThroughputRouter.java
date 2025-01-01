package com.joe.thoughput;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class InfluxDbQueryThroughputRouter extends RouteBuilder {

  @Override
    public void configure() throws Exception {
        // 查詢吞吐量測試路由
        from("timer:influxQueryTest?period=10") // 每 10 毫秒發送一次查詢請求
            .routeId("InfluxDBQueryTestRoute")
            .process(exchange -> {
                String fluxQuery = """
                  from(bucket: "Test") 
                  |> range(start: -1h) 
                  |> filter(fn: (r) => r._measurement == "airSensors")
                """;
                exchange.getIn().setBody(fluxQuery);
            }) 
            .setHeader(Exchange.HTTP_METHOD, constant("POST")) 
            .setHeader("Authorization", constant("Token cMdYwxWNZmrpjBjCbzQjQ2LIsBevI-0kakNZ_jmrlvEGCChXzle3m54hEwcZwgAX-BNv6io397EbWUeLRdNgOg=="))
            .setHeader("Content-Type", constant("application/vnd.flux"))
            .setHeader("Accept", constant("application/json"))
            .to("http://localhost:8086/api/v2/query?org=Self&bucket=Test")
            //.log("Query response received: ${body}");
            .log("Influx Query success");
    }
}
