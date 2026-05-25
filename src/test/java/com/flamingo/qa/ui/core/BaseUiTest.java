package com.flamingo.qa.ui.core;

import com.flamingo.qa.config.TestConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseUiTest {
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeEach
    void setUpBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(TestConfig.headless()));
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1440, 1000)
                .setIgnoreHTTPSErrors(true));
        context.setDefaultTimeout(TestConfig.uiTimeoutMs());
        page = context.newPage();
        UiTestSession.setPage(page);
    }

    @AfterEach
    void closeBrowser() {
        UiTestSession.clear();
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
