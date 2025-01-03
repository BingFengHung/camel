package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    // "com.example", 
    // "influxdb", 
    // "mongodb.document", 
    // "mongodb.timeSeries", 
    // "performance"
    // "com.joe.thoughput.mongodb",
    // "com.joe.thoughput.influxdb",
    "com.joe.monitor",
}) 
public class MySpringBootApplication {

    /**
     * A main method to start this application.
     */
    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApplication.class, args);
    }

}