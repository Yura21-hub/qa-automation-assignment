# QA Automation Test Suite

Java/Maven test automation project for the Flamingo AQA Engineer home assignment.

## Tech Stack

- Java 17
- Maven
- JUnit 5
- REST Assured
- Playwright for Java
- AssertJ
- Jackson
- Allure JUnit 5
- Lombok

## Covered Areas

- Restful Booker API: authentication, create, read, update, delete
- Hygraph GraphQL demo schema: positive and negative queries
- DemoQA UI: student form and web table CRUD/search/sorting-behavior validation using Page Object Model

## Prerequisites

- Java 17+
- Maven 3.6+
- Chrome/Chromium browser for UI tests

Install Playwright Chromium before running UI tests:

```bash
mvn exec:java -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
```

## How to Run

Run all tests:

```bash
mvn clean test
```

Run only API tests:

```bash
mvn test -Dgroups=api
```

Run only UI tests:

```bash
mvn test -Dgroups=ui -Dheadless=true
```

Tests are configured for class-level parallel execution with a fixed parallelism of 2.

Generate Allure report after a test run:

```bash
mvn allure:report
```

Open the report from:

```text
target/site/allure-maven-plugin/index.html
```

Surefire XML and text reports are generated after every test run in:

```text
target/surefire-reports
```

GitHub Actions publishes the generated Allure HTML report as the `allure-report` workflow artifact.

## Configuration

Defaults can be overridden with Java system properties or environment variables.

| Purpose                 | System property           | Environment variable      | Default                                |
|-------------------------|---------------------------|---------------------------|----------------------------------------|
| Restful Booker base URL | `restful.booker.base.url` | `RESTFUL_BOOKER_BASE_URL` | `https://restful-booker.herokuapp.com` |
| GraphQL endpoint        | `graphql.base.url`        | `GRAPHQL_BASE_URL`        | Hygraph video schema                   |
| DemoQA base URL         | `demoqa.base.url`         | `DEMOQA_BASE_URL`         | `https://demoqa.com`                   |
| UI headless mode        | `headless`                | `HEADLESS`                | `true`                                 |
| UI timeout              | `ui.timeout.ms`           | `UI_TIMEOUT_MS`           | `15000`                                |
| Screenshot directory    | `screenshot.dir`          | `SCREENSHOT_DIR`          | `target/screenshots`                   |

Example:

```bash
mvn test -Dgroups=ui -Dheadless=false -Dui.timeout.ms=20000
```

## Project Structure

```text
src/test/java/com/flamingo/qa
  api/client      REST Assured and GraphQL clients
  api/model       Restful Booker DTOs
  api/tests       API test scenarios
  config          Environment and system property configuration
  ui/core         Playwright lifecycle and screenshot handling
  ui/pages        DemoQA Page Objects
  ui/tests        UI test scenarios
```

## Test Strategy

The suite focuses on a small number of meaningful tests rather than volume. API tests cover authentication, the full Restful Booker CRUD flow, and a data-driven booking creation scenario with cleanup where test data is created. GraphQL tests cover list pagination, entity lookup by ID, variables, fragments with nested fields, invalid IDs, malformed queries, and validation errors. Lombok keeps API DTO boilerplate low while leaving the test flow explicit.

UI tests use Page Object Model to keep selectors and workflows outside the test assertions. DemoQA pages contain ads and fixed banners, so page objects hide those noisy elements before interacting with the form and table. Web table coverage includes add, search, edit, delete, and sorting-behavior validation. The current DemoQA Web Tables build no longer exposes a sorting handler for that table, so the sorting test detects whether sorting is available and validates the current stable behavior when it is not.

## Challenges & Solutions

- Public test services can be slow or temporarily unavailable. The clients use explicit timeouts and tests avoid unnecessary repeated calls.
- API clients retry only known transient HTTP responses (`429`, `502`, `503`, `504`) once to reduce public-service flakiness without hiding functional failures.
- Restful Booker data can reset periodically. Tests create their own data and avoid depending on existing bookings.
- DemoQA has dynamic UI elements. Tests use Playwright auto-waits, stable selectors, a small custom wait helper for UI state checks, and failure screenshots in `target/screenshots`.
