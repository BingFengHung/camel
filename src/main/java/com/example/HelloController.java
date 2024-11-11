package com.example;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
     @Autowired
    private ProducerTemplate producerTemplate;

    @GetMapping("/influxdb-insert")
    public String influxDBInsert() {
        String result = producerTemplate.requestBody("direct:influxdb-insertion", null, String.class);
        return result;
    }

    @GetMapping("/influxdb-query")
    public String influxDBQuery() {
        String result = producerTemplate.requestBody("direct:influxdb-retrieval", null, String.class);
        return result;
    }

    @GetMapping("/mongodb-doc-insert")
    public String mongoDBDocInsert() {
        String result = producerTemplate.requestBody("direct:mongodb-doc-insert", null, String.class);
        return result;
    }

    @GetMapping("/mongodb-doc-query")
    public String mongoDBDocQuery() {
        String result = producerTemplate.requestBody("direct:mongodb-doc-retrieval", null, String.class);
        return result;
    }
    @GetMapping("/mongodb-ts-insert")
    public String mongoDBTsInsert() {
        String result = producerTemplate.requestBody("direct:mongodb-ts-insert", null, String.class);
        return result;
    }

    @GetMapping("/mongodb-ts-query")
    public String mongoDBTsQuery() {
        String result = producerTemplate.requestBody("direct:mongodb-ts-retrieval", null, String.class);
        return result;
    }

}
