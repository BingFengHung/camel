package com.joe.thoughput;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.CamelLogger;
import org.apache.camel.support.processor.ThroughputLogger;
import org.springframework.stereotype.Component;

@Component
public class ThoughputRouter extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    CamelLogger logger = new CamelLogger("ThroughputLogger");
    var throughputLogger = new ThroughputLogger(logger, 1000);

    from("timer:influxTest?period=10")
      .routeId("InfluxDBWriteTest") 
      .process(throughputLogger)
      .process(exchange -> {
        long timestampNs = System.currentTimeMillis() * 1_000_000;
        String data = "airSensors,sensor_id=TLM0201 temperature=73.97038159354763,humidity=35.23103248356096,co=0.48445310567793615 " + timestampNs;
        exchange.getIn().setBody(data);
      })
      .setHeader(Exchange.HTTP_METHOD, constant("POST")) 
      .setHeader(Exchange.CONTENT_TYPE, constant("text/plain; charset=utf-8"))
            .setHeader("Authorization", constant("Token cMdYwxWNZmrpjBjCbzQjQ2LIsBevI-0kakNZ_jmrlvEGCChXzle3m54hEwcZwgAX-BNv6io397EbWUeLRdNgOg=="))
            .setHeader("Accept", constant("application/json"))
            .to("http://localhost:8086/api/v2/write?org=Self&bucket=Test&precision=ns")
            ;
  }
}

@Component
class ThroughputProcessor implements Processor {
  private long startTime = System.nanoTime();
  private long counter = 0;

  @Override
  public void process(Exchange exchange) throws Exception {
    counter++; 
    if (counter % 1000 == 0) { // 每處理1000筆數據時記錄
            long elapsedTime = System.nanoTime() - startTime;
            double throughput = counter / (elapsedTime / 1_000_000_000.0);
            System.out.println("Throughput: " + throughput + " messages/second");
        }
  }
}
