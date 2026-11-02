package com.kd.interviewcoach;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient client = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    public String ask(String prompt, boolean json) {
        Map<String, Object> body = new HashMap<>();
        body.put("contents",
                List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        if (json) {
            body.put("generationConfig",
                    Map.of("responseMimeType", "application/json"));
        }

        String raw = client.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent")
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return mapper.readTree(raw)
                .at("/candidates/0/content/parts/0/text")
                .asString();
    }
}