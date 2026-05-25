package com.flamingo.qa.ui.core;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

public class ScreenshotOnFailureExtension implements TestExecutionExceptionHandler {
    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        UiTestSession.captureFailureScreenshot(context.getRequiredTestMethod().getName());
        throw throwable;
    }
}
