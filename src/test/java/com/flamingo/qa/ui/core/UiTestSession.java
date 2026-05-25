package com.flamingo.qa.ui.core;

import com.flamingo.qa.config.TestConfig;
import com.microsoft.playwright.Page;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public final class UiTestSession {
    private static final ThreadLocal<Page> CURRENT_PAGE = new ThreadLocal<>();

    private UiTestSession() {
    }

    public static void setPage(Page page) {
        CURRENT_PAGE.set(page);
    }

    public static void clear() {
        CURRENT_PAGE.remove();
    }

    public static void captureFailureScreenshot(String testName) {
        Page page = CURRENT_PAGE.get();
        if (page == null || page.isClosed()) {
            return;
        }

        try {
            Files.createDirectories(TestConfig.screenshotDir());
            Path screenshotPath = TestConfig.screenshotDir().resolve(safeName(testName) + "-" + Instant.now().toEpochMilli() + ".png");
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(true));
        } catch (RuntimeException | java.io.IOException ignored) {
            // Failure screenshots are diagnostic only; they should not mask the original test failure.
        }
    }

    private static String safeName(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
