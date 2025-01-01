package com.joe.thoughput;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.CamelLogger;
import org.apache.camel.support.processor.ThroughputLogger;
import org.springframework.stereotype.Component;

@Component
public class InfluxDbWriteThroughputRouter extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    CamelLogger logger = new CamelLogger("ThroughputLogger");
    var throughputLogger = new ThroughputLogger(logger, 10000);

    from("timer:influxTest?period=10")
      .routeId("InfluxDBWriteTest") 
      .split().method(TestDataGenerator.class, "generateBatchData")
      .parallelProcessing().threads(10)
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
    if (counter % 10000 == 0) { // 每處理1000筆數據時記錄
            long elapsedTime = System.nanoTime() - startTime;
            double throughput = counter / (elapsedTime / 1_000_000_000.0);
            System.out.println("Throughput: " + throughput + " messages/second");
        }
  }
}

@Component
class TestDataGenerator {
    public List<String> generateBatchData() {
        List<String> batch = new ArrayList<>();
        long timestampNs = System.currentTimeMillis() * 1_000_000;
        for (int i = 0; i < 100; i++) { // 每次生成 100 條數據
            batch.add("airSensors,sensor_id=TLM" + i + " temperature=" + (20 + Math.random() * 10)
                    + ",humidity=" + (30 + Math.random() * 10) + ",co=" + (0.4 + Math.random() * 0.1) + " " + timestampNs);
        }
        return batch;
    }
}
