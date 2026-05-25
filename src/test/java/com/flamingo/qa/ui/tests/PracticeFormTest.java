package com.flamingo.qa.ui.tests;

import com.flamingo.qa.ui.core.BaseUiTest;
import com.flamingo.qa.ui.pages.PracticeFormPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Testing")
@Feature("DemoQA Practice Form")
@Tag("ui")
class PracticeFormTest extends BaseUiTest {

    @Test
    @Story("Student registration form")
    @DisplayName("UI: submit student registration form and verify success modal")
    void shouldSubmitStudentRegistrationForm() {
        PracticeFormPage formPage = new PracticeFormPage(page);
        formPage.open();

        formPage.submitStudentRegistrationForm(
                "Ada",
                "Lovelace",
                "ada.lovelace@example.com",
                "1234567890",
                Paths.get("src/test/resources/upload/sample-upload.txt").toAbsolutePath()
        );

        Map<String, String> submittedValues = formPage.submittedValues();
        assertThat(formPage.successModalTitle()).isEqualTo("Thanks for submitting the form");
        assertThat(submittedValues)
                .containsEntry("Student Name", "Ada Lovelace")
                .containsEntry("Student Email", "ada.lovelace@example.com")
                .containsEntry("Gender", "Female")
                .containsEntry("Mobile", "1234567890")
                .containsEntry("Date of Birth", "15 May,1995")
                .containsEntry("Subjects", "Maths")
                .containsEntry("Hobbies", "Sports")
                .containsEntry("Picture", "sample-upload.txt")
                .containsEntry("Address", "123 Test Street")
                .containsEntry("State and City", "NCR Delhi");
    }
}
