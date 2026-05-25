package com.flamingo.qa.api.client;

import com.flamingo.qa.api.model.AuthToken;
import com.flamingo.qa.api.model.Booking;
import com.flamingo.qa.config.TestConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class RestfulBookerClient {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password123";

    private final RequestSpecification requestSpec;

    public RestfulBookerClient() {
        RestAssuredConfig config = RestAssuredConfig.config()
                .encoderConfig(EncoderConfig.encoderConfig()
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false))
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 15000)
                        .setParam("http.socket.timeout", 15000));

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(TestConfig.restfulBookerBaseUrl())
                .setContentType("application/json")
                .setAccept("application/json")
                .addHeader("User-Agent", "qa-automation-assignment")
                .setConfig(config)
                .build();
    }

    public Response authenticate(String username, String password) {
        return given()
                .spec(requestSpec)
                .body(Map.of("username", username, "password", password))
                .post("/auth");
    }

    public String authToken() {
        Response response = authenticate(USERNAME, PASSWORD);
        assertThat(response.statusCode()).isEqualTo(200);

        String token = response.as(AuthToken.class).getToken();
        assertThat(token).isNotBlank();
        return token;
    }

    public Response createBooking(Booking booking) {
        return given()
                .spec(requestSpec)
                .body(booking)
                .post("/booking");
    }

    public Response getBooking(int bookingId) {
        return given()
                .spec(requestSpec)
                .get("/booking/{bookingId}", bookingId);
    }

    public Response updateBooking(int bookingId, Booking booking, String authToken) {
        return given()
                .spec(requestSpec)
                .cookie("token", authToken)
                .body(booking)
                .put("/booking/{bookingId}", bookingId);
    }

    public Response deleteBooking(int bookingId, String authToken) {
        return given()
                .spec(requestSpec)
                .cookie("token", authToken)
                .delete("/booking/{bookingId}", bookingId);
    }
}
