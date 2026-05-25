package com.flamingo.qa.api.client;

import io.restassured.response.Response;

import java.time.Duration;
import java.util.function.Supplier;

final class HttpRetry {
    private static final int MAX_ATTEMPTS = 2;
    private static final Duration RETRY_DELAY = Duration.ofMillis(300);

    private HttpRetry() {
    }

    static Response execute(Supplier<Response> request) {
        RuntimeException lastRuntimeException = null;
        Response lastResponse = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                Response response = request.get();
                if (!shouldRetry(response)) {
                    return response;
                }
                lastResponse = response;
            } catch (RuntimeException exception) {
                lastRuntimeException = exception;
            }

            if (attempt < MAX_ATTEMPTS) {
                sleep();
            }
        }

        if (lastResponse != null) {
            return lastResponse;
        }
        throw lastRuntimeException;
    }

    private static boolean shouldRetry(Response response) {
        int statusCode = response.statusCode();
        return statusCode == 429 || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    private static void sleep() {
        try {
            Thread.sleep(RETRY_DELAY.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying request", exception);
        }
    }
}
