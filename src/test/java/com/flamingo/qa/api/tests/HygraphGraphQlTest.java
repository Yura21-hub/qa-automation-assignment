package com.flamingo.qa.api.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.flamingo.qa.api.client.GraphQlClient;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Testing")
@Feature("Hygraph GraphQL")
@Tag("api")
class HygraphGraphQlTest {
    private static final String KNOWN_MOVIE_ID = "clq16555f0nqq0ak8fvxe2c0d";
    private static final String MISSING_MOVIE_ID = "clnotexisting0000000000000000";

    private final GraphQlClient graphQl = new GraphQlClient();

    @Test
    @Story("Positive GraphQL scenarios")
    @DisplayName("GraphQL: query a movie list with pagination limit")
    void shouldQueryMoviesWithLimit() {
        JsonNode response = graphQl.execute("""
                query MoviesPage($first: Int!, $skip: Int!) {
                  movies(first: $first, skip: $skip) {
                    id
                    title
                  }
                }
                """, Map.of("first", 2, "skip", 0));

        assertNoGraphQlErrors(response);
        JsonNode movies = response.at("/data/movies");
        assertThat(movies.isArray()).isTrue();
        assertThat(movies.size()).isEqualTo(2);
        assertThat(movies.get(0).path("id").asText()).isNotBlank();
        assertThat(movies.get(0).path("title").asText()).isNotBlank();
    }

    @Test
    @Story("Positive GraphQL scenarios")
    @DisplayName("GraphQL: query a single movie by ID")
    void shouldQuerySingleMovieById() {
        JsonNode response = graphQl.execute("""
                query {
                  movie(where: { id: "clq16555f0nqq0ak8fvxe2c0d" }) {
                    id
                    title
                  }
                }
                """);

        assertNoGraphQlErrors(response);
        assertThat(response.at("/data/movie/id").asText()).isEqualTo(KNOWN_MOVIE_ID);
        assertThat(response.at("/data/movie/title").asText()).isEqualTo("Jaws");
    }

    @Test
    @Story("Positive GraphQL scenarios")
    @DisplayName("GraphQL: query a movie using variables")
    void shouldQueryMovieUsingVariables() {
        JsonNode response = graphQl.execute("""
                query MovieById($id: ID!) {
                  movie(where: { id: $id }) {
                    id
                    title
                  }
                }
                """, Map.of("id", KNOWN_MOVIE_ID));

        assertNoGraphQlErrors(response);
        assertThat(response.at("/data/movie/id").asText()).isEqualTo(KNOWN_MOVIE_ID);
        assertThat(response.at("/data/movie/title").asText()).isEqualTo("Jaws");
    }

    @Test
    @Story("Positive GraphQL scenarios")
    @DisplayName("GraphQL: query a movie fragment with nested publisher fields")
    void shouldQueryFragmentWithNestedFields() {
        JsonNode response = graphQl.execute("""
                fragment MovieSummary on Movie {
                  id
                  title
                  publishedBy {
                    name
                  }
                  moviePoster {
                    url
                  }
                }

                query MovieSummaries {
                  movies(first: 1) {
                    ...MovieSummary
                  }
                }
                """);

        assertNoGraphQlErrors(response);
        JsonNode movie = response.at("/data/movies/0");
        assertThat(movie.path("id").asText()).isNotBlank();
        assertThat(movie.path("publishedBy").path("name").asText()).isNotBlank();
        assertThat(movie.has("moviePoster")).isTrue();
    }

    @Test
    @Story("Negative GraphQL scenarios")
    @DisplayName("GraphQL: invalid ID returns null data for requested entity")
    void shouldReturnNullDataForInvalidId() {
        JsonNode response = graphQl.execute("""
                query MissingMovie($id: ID!) {
                  movie(where: { id: $id }) {
                    id
                    title
                  }
                }
                """, Map.of("id", MISSING_MOVIE_ID));

        assertNoGraphQlErrors(response);
        assertThat(response.at("/data/movie").isNull()).isTrue();
    }

    @Test
    @Story("Negative GraphQL scenarios")
    @DisplayName("GraphQL: malformed query returns parse error and null data")
    void shouldReturnParseErrorForMalformedQuery() {
        JsonNode response = graphQl.execute("query { movies { id title ");

        assertThat(response.path("data").isNull()).isTrue();
        assertThat(response.path("errors").isArray()).isTrue();
        assertThat(response.path("errors").get(0).path("message").asText())
                .containsIgnoringCase("parse");
    }

    @Test
    @Story("Negative GraphQL scenarios")
    @DisplayName("GraphQL: non-existent field returns validation error")
    void shouldReturnValidationErrorForUnknownField() {
        JsonNode response = graphQl.execute("""
                query {
                  movies(first: 1) {
                    id
                    notARealField
                  }
                }
                """);

        assertThat(response.path("data").isNull()).isTrue();
        assertThat(response.path("errors").isArray()).isTrue();
        assertThat(response.path("errors").get(0).path("message").asText())
                .contains("notARealField")
                .contains("is not defined");
    }

    private void assertNoGraphQlErrors(JsonNode response) {
        assertThat(response.has("errors"))
                .as("GraphQL errors: %s", response.path("errors"))
                .isFalse();
    }
}
