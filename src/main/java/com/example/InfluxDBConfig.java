package com.example;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;

import java.util.ArrayList;

import org.apache.camel.component.influxdb2.InfluxDb2Component;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfig {
    @Bean
	public ArrayList<String[]> dataSet() { 
		return new Sampling().GetData("influxdb_test_data.csv");
	}

    @Bean
    public InfluxDBClient influxDBClient() {
        String url = "http://localhost:8086";
        String token = "_2XEEscy2IXSFp5UXghYe5-T2kHxoFt5hh_VEHXsmXHeyQJ6QGTTlMSqqst5U0M5nAaWxILp3_fMBkvON2NUCw==";
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

