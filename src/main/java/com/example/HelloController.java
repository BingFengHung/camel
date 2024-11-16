package com.example;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // @GetMapping("/performance/{thread}")
    // public String writePerformance(@PathVariable("thread") int thread) {
    //     String result = producerTemplate.requestBodyAndHeader("direct:writePerformance", null, "thread", thread, String.class);
    //     return result;
    // }

    @GetMapping("/performance/{thread}/{count}")
    public String writePerformance(@PathVariable("thread") int thread, @PathVariable("count") int count) {
        // 假設你也想將 id 作為 header 傳遞
        Map<String, Object> headers = new HashMap<>();
        headers.put("thread", thread);
        headers.put("count", count);

        String result = producerTemplate.requestBodyAndHeaders("direct:writePerformance", null, headers, String.class);
        return result;
    }
}
