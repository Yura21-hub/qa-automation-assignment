package com.flamingo.qa.ui.pages;

import com.flamingo.qa.config.TestConfig;
import com.flamingo.qa.ui.core.Waits;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WebTablesPage {
    private final Page page;

    public WebTablesPage(Page page) {
        this.page = page;
    }

    public void open() {
        page.navigate(TestConfig.demoQaBaseUrl() + "/webtables",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        hideNoisyPageElements();
        waitForTableReady();
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

    public boolean trySortByColumnAscending(String columnName) {
        return trySortByColumn(columnName, String.CASE_INSENSITIVE_ORDER);
    }

    public boolean trySortByColumnDescending(String columnName) {
        return trySortByColumn(columnName, String.CASE_INSENSITIVE_ORDER.reversed());
    }

    public List<String> columnValues(String columnName) {
        int columnIndex = columnIndex(columnName);
        Locator rows = bodyRows();
        List<String> values = new ArrayList<>();

        for (int index = 0; index < rows.count(); index++) {
            Locator cells = rowCells(rows.nth(index));
            if (cells.count() <= columnIndex) {
                continue;
            }

            String value = cells.nth(columnIndex).innerText().trim();
            if (!value.isBlank()) {
                values.add(value);
            }
        }

        return values;
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

    private void waitForTableReady() {
        page.locator("#addNewRecordButton").waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
        Waits.until(
                () -> headers().count() > 0,
                Duration.ofMillis((long) TestConfig.uiTimeoutMs()),
                "web table headers to be available"
        );
    }

    private boolean trySortByColumn(String columnName, Comparator<String> expectedOrder) {
        Locator columnHeader = header(columnName);

        for (int attempt = 0; attempt < 3; attempt++) {
            columnHeader.click();
            try {
                Waits.until(
                        () -> isColumnSorted(columnName, expectedOrder),
                        Duration.ofSeconds(2),
                        columnName + " values to be sorted"
                );
                return true;
            } catch (AssertionError ignored) {
                // Try the next sort direction when the table toggles between ascending and descending.
            }
        }

        return false;
    }

    private Locator header(String columnName) {
        Locator headers = headers();

        for (int index = 0; index < headers.count(); index++) {
            Locator header = headers.nth(index);
            if (matchesColumn(header.innerText(), columnName)) {
                return header;
            }
        }

        throw new IllegalArgumentException("Unknown table column: " + columnName);
    }

    private int columnIndex(String columnName) {
        Locator headers = headers();

        for (int index = 0; index < headers.count(); index++) {
            if (matchesColumn(headers.nth(index).innerText(), columnName)) {
                return index;
            }
        }

        throw new IllegalArgumentException("Unknown table column: " + columnName);
    }

    private Locator headers() {
        return page.locator(".rt-thead .rt-th, table thead th");
    }

    private Locator bodyRows() {
        Locator reactRows = page.locator(".rt-tbody .rt-tr-group");
        return reactRows.count() > 0 ? reactRows : page.locator("table tbody tr");
    }

    private Locator rowCells(Locator row) {
        Locator reactCells = row.locator(".rt-td");
        return reactCells.count() > 0 ? reactCells : row.locator("td");
    }

    private boolean isColumnSorted(String columnName, Comparator<String> expectedOrder) {
        List<String> actualValues = columnValues(columnName);
        List<String> sortedValues = actualValues.stream()
                .sorted(expectedOrder)
                .toList();

        return !actualValues.isEmpty() && actualValues.equals(sortedValues);
    }

    private boolean matchesColumn(String actualHeader, String expectedColumnName) {
        return actualHeader.replaceAll("\\s+", " ").trim().contains(expectedColumnName);
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
                #RightSide_Advertisement,
                .Advertisement-Section,
                .Google-Ad,
                .col-12.mt-4.col-md-3.col-xl-3,
                div[id^='Ad.Plus'],
                footer,
                iframe,
                .adsbygoogle {
                  display: none !important;
                  visibility: hidden !important;
                  pointer-events: none !important;
                }
                """));
    }
}
