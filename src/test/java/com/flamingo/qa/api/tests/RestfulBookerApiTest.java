package com.flamingo.qa.api.tests;

import com.flamingo.qa.api.client.RestfulBookerClient;
import com.flamingo.qa.api.model.AuthToken;
import com.flamingo.qa.api.model.Booking;
import com.flamingo.qa.api.model.BookingDates;
import com.flamingo.qa.api.model.BookingResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Testing")
@Feature("Restful Booker CRUD")
@Tag("api")
class RestfulBookerApiTest {
    private final RestfulBookerClient client = new RestfulBookerClient();

    @Test
    @Story("Authentication")
    @DisplayName("REST: authenticate and receive a reusable token")
    void shouldAuthenticateAndReturnToken() {
        Response response = client.authenticate("admin", "password123");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.as(AuthToken.class).getToken()).isNotBlank();
    }

    @Test
    @Story("Create and read booking")
    @DisplayName("REST: create a booking and retrieve it by ID")
    void shouldCreateBookingAndRetrieveItById() {
        Booking expectedBooking = booking("Ada", "Lovelace", 240, true, "Breakfast");

        BookingResponse createdBooking = createBooking(expectedBooking);
        int bookingId = createdBooking.getBookingid();

        try {
            Response getResponse = client.getBooking(bookingId);

            assertThat(getResponse.statusCode()).isEqualTo(200);
            assertThat(getResponse.as(Booking.class))
                    .usingRecursiveComparison()
                    .isEqualTo(expectedBooking);
        } finally {
            deleteQuietly(bookingId);
        }
    }

    @Test
    @Story("Update booking")
    @DisplayName("REST: update a booking and verify changed fields")
    void shouldUpdateExistingBooking() {
        Booking initialBooking = booking("Grace", "Hopper", 180, true, "Late checkout");
        BookingResponse createdBooking = createBooking(initialBooking);
        int bookingId = createdBooking.getBookingid();

        Booking updatedBooking = booking("Grace", "Hopper", 315, false, "Dinner");

        try {
            Response updateResponse = client.updateBooking(bookingId, updatedBooking, client.authToken());

            assertThat(updateResponse.statusCode()).isEqualTo(200);
            assertThat(updateResponse.as(Booking.class))
                    .usingRecursiveComparison()
                    .isEqualTo(updatedBooking);

            Response getResponse = client.getBooking(bookingId);
            assertThat(getResponse.as(Booking.class))
                    .usingRecursiveComparison()
                    .isEqualTo(updatedBooking);
        } finally {
            deleteQuietly(bookingId);
        }
    }

    @Test
    @Story("Delete booking")
    @DisplayName("REST: delete a booking and verify it is no longer available")
    void shouldDeleteExistingBooking() {
        BookingResponse createdBooking = createBooking(booking("Alan", "Turing", 150, false, "Parking"));
        int bookingId = createdBooking.getBookingid();

        Response deleteResponse = client.deleteBooking(bookingId, client.authToken());
        assertThat(deleteResponse.statusCode()).isIn(200, 201);

        Response getResponse = client.getBooking(bookingId);
        assertThat(getResponse.statusCode()).isEqualTo(404);
    }

    private BookingResponse createBooking(Booking booking) {
        Response response = client.createBooking(booking);

        assertThat(response.statusCode()).isEqualTo(200);

        BookingResponse bookingResponse = response.as(BookingResponse.class);
        assertThat(bookingResponse.getBookingid()).isPositive();
        assertThat(bookingResponse.getBooking())
                .usingRecursiveComparison()
                .isEqualTo(booking);
        return bookingResponse;
    }

    private Booking booking(String firstName, String lastName, int totalPrice, boolean depositPaid, String needs) {
        return new Booking(
                firstName,
                lastName,
                totalPrice,
                depositPaid,
                new BookingDates("2026-06-01", "2026-06-07"),
                needs
        );
    }

    private void deleteQuietly(int bookingId) {
        try {
            client.deleteBooking(bookingId, client.authToken());
        } catch (RuntimeException ignored) {
            // Cleanup should not hide the assertion failure that happened before it.
        }
    }
}
