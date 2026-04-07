package ru.yandex.practicum.telemetry.collector.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.telemetry.collector.exception.KafkaPublishException;
import ru.yandex.practicum.telemetry.collector.service.CollectorService;

@SpringBootTest
@AutoConfigureMockMvc
class CollectorControllerErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollectorService collectorService;

    @Test
    void shouldReturnInternalServerErrorWhenKafkaFails() throws Exception {
        doThrow(new KafkaPublishException("boom", new RuntimeException("boom")))
                .when(collectorService)
                .saveSensorEvent(any());

        String json = """
                {
                  "id": "sensor-1",
                  "hubId": "hub-1",
                  "linkQuality": 75,
                  "motion": true,
                  "voltage": 220,
                  "type": "MOTION_SENSOR_EVENT"
                }
                """;

        mockMvc.perform(post("/events/sensors")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnBadRequestForScenarioWithShortNameAndEmptyLists() throws Exception {
        String json = """
                {
                  "hubId": "hub-1",
                  "name": "ab",
                  "conditions": [],
                  "actions": [],
                  "type": "SCENARIO_ADDED"
                }
                """;

        mockMvc.perform(post("/events/hubs")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
