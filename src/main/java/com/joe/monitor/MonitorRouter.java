package com.joe.monitor;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Component
public class MonitorRouter extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    // 使用 tasklist 取得 InfluxDB Process 的記憶體/CPU
    // from("timer:influxMonitorTasklist?period=2000")
    //   .routeId("InfluxDBSystemMonitorTasklist")
    //   .process(exchange -> {
    //     ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "tasklist /FI \"IMAGENAME eq influxd.exe\"");

    //     Process process = pb.start();
        
    //     try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
    //       StringBuilder output = new StringBuilder();
    //       String line;

    //       while((line = reader.readLine()) != null) {
    //         output.append(line).append(System.lineSeparator());
    //       }
          
    //       exchange.getIn().setBody(output.toString());
    //     }
    //   })
    //   .log("InfluxDB Resource Usage (Tasklist): \n${body}");

              //"chcp 65001 && typeperf \"\\Process(influxd)\\% Processor Time\" \"\\Process(influxd)\\Working Set - Private\" -sc 1 >> output.txt"
      Properties props = new Properties();
      
      // try (FileInputStream fs = new FileInputStream("./config.properties")) {
      //   System.out.println(fs);
      //   props.load(fs);
      // }
      
      Yaml yaml = new Yaml();

      try (InputStream inputStream = new FileInputStream("./config.properties.yml")) {
          Map<String, String> config = yaml.load(inputStream);
          //String cmd = (String) config.get("cmd");
          System.out.println("this is config: " + config);
          props.setProperty("cmd", config.get("cmd"));
      } catch (Exception e) {
          System.err.println("讀取 YAML 配置文件時發生錯誤：" + e.getMessage());
      }
      
      String cmd = props.getProperty("cmd");

      from("timer:influxMonitorTypeperf?period=2000") // 每 2 秒執行一次
      .routeId("InfluxDBResourceMonitorTypeperf")
      .process(exchange -> {
        System.out.println(cmd);
          ProcessBuilder pb = new ProcessBuilder(
              "cmd", "/c", cmd + " >> output.txt"
          );
          Process process = pb.start();
          
        // if (!Charset.isSupported("GBK")) {
        //     throw new IllegalArgumentException("GBK charset is not supported on this system");
        // }
          
          //try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"))) {
          try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
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
