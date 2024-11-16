package influxdb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.MySpringBootApplication;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CamelSpringBootTest
@SpringBootTest(classes = {MySpringBootApplication.class})
@MockEndpoints
public class InsertionRouterTest {
   @BeforeEach
    public void setUp() throws Exception {
        camelContext.start();
        assertTrue(camelContext.getStatus().isStarted(), "CamelContext should be started");
        assertTrue(camelContext.getRouteController().getRouteStatus("influxdbInsertionRoute").isStarted(), "Route should be started");

    }

    @AfterEach
    public void tearDown() throws Exception {
        camelContext.stop();
    }

    @Autowired
    private CamelContext camelContext;

    @MockBean
    private InfluxDBClient influxDBClient;

    @MockBean
    private WriteApiBlocking writeApi;

    @MockBean
    private ArrayList<String[]> dataSet;

    @Captor
    private ArgumentCaptor<List<Point>> pointCaptor;

    @Test
    void testInfluxDBInsertion() throws Exception {
        ProducerTemplate template = camelContext.createProducerTemplate();
        Mockito.when(influxDBClient.getWriteApiBlocking()).thenReturn(writeApi);
        Mockito.doNothing().when(writeApi).writePoints(any());

        // 模擬數據
        String[] sampleData = {"id", "file", "1.1", "2.2", "3.3"};
        ArrayList<String[]> mockDataSet = new ArrayList<>();
        mockDataSet.add(sampleData);
        Mockito.when(dataSet.size()).thenReturn(1);
        Mockito.when(dataSet.get(0)).thenReturn(sampleData);
        Mockito.when(dataSet.subList(0, 1)).thenReturn(mockDataSet);

        // 觸發路由
        template.sendBody("direct:influxdb-insertion", null);

        // 驗證 writeApi 被正確呼叫
        verify(writeApi, times(1)).writePoints(pointCaptor.capture());
        List<Point> capturedPoints = pointCaptor.getValue();

        // 驗證點的數量
        assertFalse(capturedPoints.isEmpty(), "No points were written.");
    }
}