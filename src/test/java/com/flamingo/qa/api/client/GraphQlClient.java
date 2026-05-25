package com.flamingo.qa.api.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flamingo.qa.config.TestConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class GraphQlClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RequestSpecification requestSpec;

    public GraphQlClient() {
        RestAssuredConfig config = RestAssuredConfig.config()
                .encoderConfig(EncoderConfig.encoderConfig()
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false))
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 15000)
                        .setParam("http.socket.timeout", 15000));

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(TestConfig.graphQlBaseUrl())
                .setContentType("application/json")
                .setAccept("application/json")
                .setConfig(config)
                .build();
    }

    public JsonNode execute(String query) {
        return execute(query, Collections.emptyMap());
    }

    public JsonNode execute(String query, Map<String, Object> variables) {
        Response response = executeResponse(query, variables);
        assertThat(response.statusCode()).isIn(200, 400);
        return toJson(response);
    }

    public Response executeResponse(String query, Map<String, Object> variables) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("query", query);
        if (variables != null && !variables.isEmpty()) {
            requestBody.put("variables", variables);
        }

        return given()
                .spec(requestSpec)
                .body(requestBody)
                .post();
    }

    private JsonNode toJson(Response response) {
        try {
            return MAPPER.readTree(response.asString());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("GraphQL response is not valid JSON", exception);
        }
    }
}
