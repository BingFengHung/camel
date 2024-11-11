package com.example;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;

import java.util.ArrayList;

import org.apache.camel.component.influxdb2.InfluxDb2Component;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;


@Configuration
public class InfluxDBConfig {
    @Value("${influxdb.url}")
    private String url;

    @Value("${influxdb.token}")
    private String token;

    @Bean
	public ArrayList<String[]> dataSet() { 
		return new Sampling().GetData("influxdb_test_data.csv");
	}

    @Bean
    public InfluxDBClient influxDBClient() {
        String url = this.url;
        String token = this.token;
        String org = "Self";
        String bucket = "Test";
        return InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
    }
    
    @Bean
    public InfluxDb2Component influxDB2Component(InfluxDBClient influxDBClient) {
    	InfluxDb2Component component = new InfluxDb2Component();
        component.setInfluxDBClient(influxDBClient); // 设置 InfluxDBClient
        return component;
    }
}

