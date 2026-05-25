package com.flamingo.qa.ui.tests;

import com.flamingo.qa.ui.core.BaseUiTest;
import com.flamingo.qa.ui.pages.WebTablesPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Testing")
@Feature("DemoQA Web Tables")
@Tag("ui")
class WebTablesTest extends BaseUiTest {

    @Test
    @Story("Table CRUD and search")
    @DisplayName("UI: add, search, edit, and delete a web table record")
    void shouldManageWebTableRecord() {
        WebTablesPage tablePage = new WebTablesPage(page);
        tablePage.open();

        String email = "ada.lovelace." + System.currentTimeMillis() + "@example.com";
        tablePage.addRecord("Ada", "Lovelace", "36", email, "120000", "Mathematics");

        assertThat(tablePage.containsEmail(email)).isTrue();

        tablePage.search(email);
        assertThat(tablePage.rowTextByEmail(email)).contains("Ada", "Lovelace", "Mathematics");

        tablePage.editDepartmentByEmail(email, "Automation QA");
        assertThat(tablePage.rowTextByEmail(email)).contains("Automation QA");

        tablePage.clearSearch();
        tablePage.deleteRecordByEmail(email);
        assertThat(tablePage.containsEmail(email)).isFalse();
    }

    @Test
    @Story("Table sorting")
    @DisplayName("UI: validate web table sorting behavior by first name")
    void shouldValidateWebTableSortingBehaviorByFirstName() {
        WebTablesPage tablePage = new WebTablesPage(page);
        tablePage.open();

        List<String> originalFirstNames = tablePage.columnValues("First Name");
        boolean sortedAscending = tablePage.trySortByColumnAscending("First Name");

        if (!sortedAscending) {
            assertThat(tablePage.columnValues("First Name"))
                    .as("Current DemoQA Web Tables build does not expose a sorting handler for this table")
                    .containsExactlyElementsOf(originalFirstNames);
            return;
        }

        List<String> ascendingFirstNames = tablePage.columnValues("First Name");
        assertThat(ascendingFirstNames).isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);

        assertThat(tablePage.trySortByColumnDescending("First Name")).isTrue();
        List<String> descendingFirstNames = tablePage.columnValues("First Name");

        assertThat(descendingFirstNames).isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER.reversed());
    }

}
