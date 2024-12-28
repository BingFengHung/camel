package com.joe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class WriteBulkRouter extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    from("timer:writeMockTest?period=10000&repeatCount=2") 
    .setProperty("repeatCount", constant(2))
    .log("run")
    .process(new WriteMockProcessor())
    .process(new LoggerProcessor())
    .process(new AnalyzerProcessor())
    ;
  }
}

class WriteMockProcessor implements Processor {
  final int THREAD_COUNT = 89;

  @Override
  public void process(Exchange exchange) throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
    
    ConcurrentHashMap<String, Long> simulatedDatabase = new ConcurrentHashMap<>();
    exchange.setProperty("simulatedDatabase", simulatedDatabase);
    
    for (int i = 0; i < THREAD_COUNT; i++) {
      final int threadIndex = i + 1;
      executor.submit(() -> {
        try { 
          insertDataFake(simulatedDatabase, threadIndex);
        } finally {
          latch.countDown();
        }
      });
    }
    
    latch.await();

    executor.shutdown();
    
    List<String> times = new ArrayList<>();
    simulatedDatabase.forEach((key, value) -> times.add(value + ""));
    // for (int i = 1; i <= THREAD_COUNT; i++) {
    //   String propertyKey=  "ThreadIndex" + i;
    //   Object value = exchange.getProperty(propertyKey);
    //   times.add(value.toString());
    //   //stringBuilder.append(value.toString() + ",");
    // }
    
    exchange.setProperty("times", times);
  }
  
  private void insertDataFake(ConcurrentHashMap<String, Long> simulatedDatabase, int threadIndex) {
    long startTime = System.currentTimeMillis();
    
    double value = Math.random() * 200.0;
    
    try { 
      Thread.sleep((long)(Math.random() * 100));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    

    long endTime = System.currentTimeMillis();
    
    long latency = endTime - startTime;

    simulatedDatabase.put("ThreadIndex" + threadIndex, latency);
    
    
    // exchange.setProperty("ThreadIndex" + threadIndex, value);
  }
}

class LoggerProcessor implements Processor {

  @Override
  public void process(Exchange exchange) throws Exception { 
    List<String> value = (List<String>) exchange.getProperty("times");
    String content = String.join(",", value); 

    String filename = "raw.txt";
    
    try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
      writer.write(content);
      writer.newLine();
    }
    
    exchange.setProperty("filename", filename);
  }
}

class AnalyzerProcessor implements Processor {

  @Override
  public void process(Exchange exchange) throws Exception {
    long totalCount = exchange.getProperty(Exchange.TIMER_COUNTER, Long.class);
    long repeatCount = exchange.getProperty("repeatCount", Long.class);

    if (totalCount == repeatCount) { 
      String filename = (String) exchange.getProperty("filename");
      
      List<List<Double>> dataset = new ArrayList<>();
      
      try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
        String line;
        while((line = reader.readLine()) != null) {
          List<Double> numbers = Arrays.stream(line.split(","))
          .map(s -> {
            try { return Double.parseDouble(s);}
            catch (NumberFormatException e) {
              System.err.println("Invalid number format: " + s);
              return 0.0;
            }
          })
          .collect(Collectors.toList());
          
          dataset.add(numbers);
        }
        
        analyzer(dataset);
      } catch(IOException e) {
        System.err.println("Error reading the file: " +  e.getMessage());
      }
    }
  }
  
  private void analyzer(List<List<Double>> dataset) { 
    // Display raw data
    for (List<Double> row : dataset) {
      System.out.println("Row: " + row);
    }
    
    double sum = 0.0;
    double max = Double.MIN_VALUE;
    double min = Double.MAX_VALUE;
    double avg = 0;

    int count = 0;
    for (int i = 0; i < dataset.size(); i++) {
      for (int j = 0; j < dataset.get(i).size(); j++) {
        double value = dataset.get(i).get(j);
        
        sum += value;
        
        if (value > max) max = value;
        if (value < min) { 
          System.out.println("Min: " + min + " / " + value);
          min = value;         
        }

        count++;
      }
    }
    
    avg = sum / count;

    System.out.println(String.format("Max: %s / Min: %s / Average: %s", max, min, avg));
  }
}