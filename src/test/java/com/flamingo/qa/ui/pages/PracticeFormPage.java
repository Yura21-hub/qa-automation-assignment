package com.flamingo.qa.ui.pages;

import com.flamingo.qa.config.TestConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class PracticeFormPage {
    private final Page page;

    public PracticeFormPage(Page page) {
        this.page = page;
    }

    public void open() {
        page.navigate(TestConfig.demoQaBaseUrl() + "/automation-practice-form",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        hideNoisyPageElements();
    }

    public void submitStudentRegistrationForm(
            String firstName,
            String lastName,
            String email,
            String mobile,
            Path uploadFile
    ) {
        page.locator("#firstName").fill(firstName);
        page.locator("#lastName").fill(lastName);
        page.locator("#userEmail").fill(email);
        page.locator("label[for='gender-radio-2']").click();
        page.locator("#userNumber").fill(mobile);

        selectBirthDate("4", "1995", "015");

        page.locator("#subjectsInput").fill("Math");
        page.locator("#react-select-2-option-0").click();
        page.locator("label[for='hobbies-checkbox-1']").click();
        page.locator("#uploadPicture").setInputFiles(uploadFile);
        page.locator("#currentAddress").fill("123 Test Street");

        page.locator("#state").click();
        page.locator("div[id^='react-select'][id$='-option-0']").click();
        page.locator("#city").click();
        page.locator("div[id^='react-select'][id$='-option-0']").click();

        page.locator("#submit").scrollIntoViewIfNeeded();
        page.locator("#submit").click();
    }

    public String successModalTitle() {
        return page.locator("#example-modal-sizes-title-lg").innerText();
    }

    public Map<String, String> submittedValues() {
        Map<String, String> values = new LinkedHashMap<>();
        Locator rows = page.locator(".modal-body tbody tr");

        for (int index = 0; index < rows.count(); index++) {
            Locator row = rows.nth(index);
            values.put(row.locator("td").nth(0).innerText(), row.locator("td").nth(1).innerText());
        }

        return values;
    }

    private void selectBirthDate(String monthValue, String yearValue, String dayClassSuffix) {
        page.locator("#dateOfBirthInput").click();
        page.locator(".react-datepicker__month-select").selectOption(monthValue);
        page.locator(".react-datepicker__year-select").selectOption(yearValue);
        page.locator(".react-datepicker__day--" + dayClassSuffix + ":not(.react-datepicker__day--outside-month)").click();
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
