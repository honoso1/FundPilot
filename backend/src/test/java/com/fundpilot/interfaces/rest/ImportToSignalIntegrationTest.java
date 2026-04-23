package com.fundpilot.interfaces.rest;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportToSignalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldImportMockThenExposeFundsAndLatestSignal() throws Exception {
        mockMvc.perform(post("/api/import/mock"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true));

        String fundsPayload = mockMvc.perform(get("/api/funds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()", greaterThan(1)))
                .andReturn().getResponse().getContentAsString();

        String firstId = JsonPath.read(fundsPayload, "$.data.items[0].id");

        mockMvc.perform(get("/api/funds/" + firstId + "/latest-signal").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").exists())
                .andExpect(jsonPath("$.data.label").exists());
    }

    @Test
    void shouldReturnValidationErrorEnvelopeForInvalidCsvPayload() throws Exception {
        mockMvc.perform(post("/api/import/csv")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"csvContent\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").exists());
    }
}
