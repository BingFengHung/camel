package com.joe.monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MonitorRouter extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    // 使用 tasklist 取得 InfluxDB Process 的記憶體/CPU
    from("timer:influxMonitorTasklist?period=2000")
      .routeId("InfluxDBSystemMonitorTasklist")
      .process(exchange -> {
        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "tasklist /FI \"IMAGENAME eq influxd.exe\"");

        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
          StringBuilder output = new StringBuilder();
          String line;

          while((line = reader.readLine()) != null) {
            output.append(line).append(System.lineSeparator());
          }
          
          exchange.getIn().setBody(output.toString());
        }
      })
      .log("InfluxDB Resource Usage (Tasklist): \n${body}");

      from("timer:influxMonitorTypeperf?period=2000") // 每 2 秒執行一次
      .routeId("InfluxDBResourceMonitorTypeperf")
      .process(exchange -> {
          ProcessBuilder pb = new ProcessBuilder(
              "cmd", "/c",
              "typeperf \"\\Process(influxd)\\% Processor Time\" \"\\Process(influxd)\\Working Set - Private\" -sc 1"
          );
          Process process = pb.start();
          try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
              StringBuilder output = new StringBuilder();
              String line;
              while ((line = reader.readLine()) != null) {
                  output.append(line).append(System.lineSeparator());
              }
              exchange.getIn().setBody(output.toString());
          }
      })
      .log("InfluxDB CPU/Memory Usage (Typeperf):\n${body}");
  }
}
