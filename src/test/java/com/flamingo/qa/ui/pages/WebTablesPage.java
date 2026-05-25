package com.flamingo.qa.ui.pages;

import com.flamingo.qa.config.TestConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.microsoft.playwright.options.WaitForSelectorState;

public class WebTablesPage {
    private final Page page;

    public WebTablesPage(Page page) {
        this.page = page;
    }

    public void open() {
        page.navigate(TestConfig.demoQaBaseUrl() + "/webtables",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        hideNoisyPageElements();
    }

    public void addRecord(String firstName, String lastName, String age, String email, String salary, String department) {
        page.locator("#addNewRecordButton").click();
        fillRegistrationForm(firstName, lastName, age, email, salary, department);
        page.locator("#submit").click();
        waitForRegistrationModalToClose();
    }

    public void search(String value) {
        page.locator("#searchBox").fill(value);
    }

    public void clearSearch() {
        page.locator("#searchBox").fill("");
    }

    public void editDepartmentByEmail(String email, String department) {
        search(email);
        page.locator("span[title='Edit']").first().click();
        page.locator("#department").fill(department);
        page.locator("#submit").click();
        waitForRegistrationModalToClose();
        search(email);
    }

    public void deleteRecordByEmail(String email) {
        search(email);
        page.locator("span[title='Delete']").first().click();
    }

    public boolean containsEmail(String email) {
        return page.getByText(email, new Page.GetByTextOptions().setExact(true)).count() > 0;
    }

    public String rowTextByEmail(String email) {
        search(email);
        return page.locator("body").innerText();
    }

    private void waitForRegistrationModalToClose() {
        page.locator(".modal-content").waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.DETACHED));
    }

    private void fillRegistrationForm(String firstName, String lastName, String age, String email, String salary, String department) {
        page.locator("#firstName").fill(firstName);
        page.locator("#lastName").fill(lastName);
        page.locator("#userEmail").fill(email);
        page.locator("#age").fill(age);
        page.locator("#salary").fill(salary);
        page.locator("#department").fill(department);
    }

    private void hideNoisyPageElements() {
        page.addStyleTag(new Page.AddStyleTagOptions().setContent("""
                #fixedban,
                footer,
                iframe,
                .adsbygoogle {
                  display: none !important;
                  visibility: hidden !important;
                }
                """));
    }
}
