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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImportToSignalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldImportMockThenExposeFundsAndLatestSignal() throws Exception {
        mockMvc.perform(post("/api/import/mock"))
                .andExpect(status().isAccepted());

        String fundsPayload = mockMvc.perform(get("/api/funds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThan(1)))
                .andReturn().getResponse().getContentAsString();

        String firstId = JsonPath.read(fundsPayload, "$[0].id");

        mockMvc.perform(get("/api/funds/" + firstId + "/latest-signal").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").exists())
                .andExpect(jsonPath("$.label").exists());
    }
}
