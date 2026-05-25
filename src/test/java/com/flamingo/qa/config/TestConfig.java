package com.flamingo.qa.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class TestConfig {
    private static final String RESTFUL_BOOKER_URL = "https://restful-booker.herokuapp.com";
    private static final String HYGRAPH_VIDEO_SCHEMA_URL =
            "https://us-east-1-shared-usea1-02.cdn.hygraph.com/content/clpvcopq3aavs01usft1idkgj/master";
    private static final String DEMO_QA_URL = "https://demoqa.com";

    private TestConfig() {
    }

    public static String restfulBookerBaseUrl() {
        return normalizeUrl(value("restful.booker.base.url", "RESTFUL_BOOKER_BASE_URL", RESTFUL_BOOKER_URL));
    }

    public static String graphQlBaseUrl() {
        return normalizeUrl(value("graphql.base.url", "GRAPHQL_BASE_URL", HYGRAPH_VIDEO_SCHEMA_URL));
    }

    public static String demoQaBaseUrl() {
        return normalizeUrl(value("demoqa.base.url", "DEMOQA_BASE_URL", DEMO_QA_URL));
    }

    public static boolean headless() {
        return Boolean.parseBoolean(value("headless", "HEADLESS", "true"));
    }

    public static double uiTimeoutMs() {
        return Double.parseDouble(value("ui.timeout.ms", "UI_TIMEOUT_MS", "15000"));
    }

    public static Path screenshotDir() {
        return Paths.get(value("screenshot.dir", "SCREENSHOT_DIR", "target/screenshots"));
    }

    private static String value(String propertyName, String envName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return defaultValue;
    }

    private static String normalizeUrl(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
