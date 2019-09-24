package com.learnwiremock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.learnwiremock.constants.MoviesAppConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.learnwiremock.constants.MoviesAppConstants.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWireMock(port = 8088)
@TestPropertySource(properties= {"movieapp.baseUrl=http://localhost:8088"})
public class MoviesRestClientTest {


    @Autowired
    MoviesRestClient moviesRestClient;

    @Test
    public void getAllMovies() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBodyFile("all-movies.json")));
        //whenx
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println("movieList : " + movieList);

        //then
        assertTrue(!movieList.isEmpty());
    }

    @Test
    public void getAllMovies_matchUrlPath() {

        //given
        stubFor(get(urlPathEqualTo(GET_ALL_MOVIES_V1))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("all-movies.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println("movieList : " + movieList);

        //then
        assertTrue(!movieList.isEmpty());
    }


    @Test
    public void retrieveMovieById() {

        //given
        //stubFor(get(urlPathEqualTo("/movieservice/v1/movie/1"))
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie.json")));

        //given
        Integer movieId = 1;

        //when
        Movie movie = moviesRestClient.retrieveMovieById(movieId);

        //then
        assertEquals("Batman Begins", movie.getName());
    }

   // @Test
    public void retrieveMovieById_withResponseTemplating() {

        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie-template.json")));


        //given
        Integer movieId = 200;

        //when
        Movie movie = moviesRestClient.retrieveMovieById(movieId);
        //then
        assertEquals("Batman Begins", movie.getName());
        assertEquals(movieId.intValue(), movie.getMovie_id().intValue());
    }


    @Test
    public void retrieveMovieById_WithPriority() {

        //given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/1"))
                .atPriority(1)
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie.json")));
        stubFor(get(urlMatching("/movieservice/v1/movie/([0-9])"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie1.json")));


        //given
        Integer movieId = 1;
        Integer movieId1 = 2;

        //when
        Movie movie = moviesRestClient.retrieveMovieById(movieId);
        Movie movie1 = moviesRestClient.retrieveMovieById(movieId1);

        //then
        assertEquals("Batman Begins", movie.getName());
        assertEquals("Batman Begins1", movie1.getName());
    }

    @Test(expected = MovieErrorResponse.class)
    public void retrieveMovieById_NotFound() {
        //given
        Integer movieId = 100;
        stubFor(get(urlPathMatching("/movieservice/v1/movie/([0-9]+)"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withBodyFile("404-movieId.json")));

        //when
        moviesRestClient.retrieveMovieById(movieId);

    }

    @Test
    public void retrieveMovieByName_UrlEqualTo() {
        //dont use urlPathEqualTo when there is queryParem involved.

        //given
        String movieName = "Avengers";
        stubFor(get(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("avengers.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(movieName);

        //then
        String expectedCastName = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(expectedCastName, movieList.get(0).getCast());
    }

    @Test
    public void retrieveMovieByName_UrlPathEqualTo_approach2() {
        //dont use urlPathEqualTo when there is queryParem involved.

        //given
        String movieName = "Avengers";
        stubFor(get(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("avengers.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(movieName);

        //then
        String expectedCastName = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(expectedCastName, movieList.get(0).getCast());
    }


    @Test(expected = MovieErrorResponse.class)
    public void retrieveMovieByName_Not_Found() {
        //given
        String movieName = "ABC";
        stubFor(get(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withBodyFile("404-moviename.json")));

        //when
        moviesRestClient.retrieveMovieByName(movieName);

    }

    @Test
    public void retrieveMovieByName_withResponseTemplating() {
        //dont use urlPathEqualTo when there is queryParem involved.

        //given
        String movieName = "Avengers";
        stubFor(get(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie-byname-template.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(movieName);
        System.out.println("movieList : " + movieList);
        //then
        String expectedCastName = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(expectedCastName, movieList.get(0).getCast());
    }


    @Test
    public void retrieveMovieByName_urlPathEqualTo() {
        //given
        String movieName = "Avengers";
        stubFor(get(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
                // .withQueryParam("movie_name", equalTo(movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("avengers.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(movieName);

        //then
        String expectedCastName = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(expectedCastName, movieList.get(0).getCast());
    }


    //@Test
    public void retrieveMovieByYear() {
        //given
        Integer year = 2012;
        stubFor(get(urlEqualTo(MOVIE_BY_YEAR_QUERY_PARAM_V1 + "?year=" + year))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("year-template.json")));

        //when
        List<Movie> movieList = moviesRestClient.retreieveMovieByYear(year);

        //then
        assertEquals(2, movieList.size());

    }

    @Test(expected = MovieErrorResponse.class)
    public void retrieveMovieByYear_Not_Found() {
        //given
        Integer year = 1950;
        stubFor(get(urlEqualTo(MOVIE_BY_YEAR_QUERY_PARAM_V1 + "?year=" + year))
                .withQueryParam("year", equalTo(year.toString()))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("404-movieyear.json")));


        //when
        moviesRestClient.retreieveMovieByYear(year);

    }

    @Test
    public void addNewMovie() {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, batmanBeginsCrew, LocalDate.of(2019, 06, 20));
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                //.withRequestBody(matchingJsonPath("$..name", containing("Toy Story 4")))
                .withRequestBody(matchingJsonPath(("$.name")))
                .withRequestBody(matchingJsonPath(("$.cast")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.CREATED.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie.json")));


        //when
        Movie movie = moviesRestClient.addNewMovie(toyStory);

        //then
        assertTrue(movie.getMovie_id() != null);

    }


    @Test
    public void addNewMovie_matchingJsonAttributeValue() throws JsonProcessingException {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, batmanBeginsCrew, LocalDate.of(2019, 06, 20));


        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath(("$.name"), equalTo("Toy Story 4")))
                .withRequestBody(matchingJsonPath(("$.cast"), containing("Tom")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.CREATED.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie.json")));


        //when
        Movie movie = moviesRestClient.addNewMovie(toyStory);

        //then
        assertTrue(movie.getMovie_id() != null);

    }

   // @Test
    public void addNewMovie_dynamicResponse() {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, batmanBeginsCrew, LocalDate.of(2019, 06, 20));
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                //.withRequestBody(matchingJsonPath("$..name", containing("Toy Story 4")))
                .withRequestBody(matchingJsonPath(("$.name")))
                .withRequestBody(matchingJsonPath(("$.cast")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.CREATED.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie-template.json")));


        //when
        Movie movie = moviesRestClient.addNewMovie(toyStory);
        System.out.println("movie : " + movie);

        //then
        assertTrue(movie.getMovie_id() != null);

    }

    @Test(expected = MovieErrorResponse.class)
    public void addNewMovie_InvlaidInput() {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, null, null, batmanBeginsCrew, LocalDate.of(2019, 06, 20));
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .willReturn(WireMock.aResponse()
                        .withBodyFile("addmovie-invalidinput.json")
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("404.json")));

        //when
        moviesRestClient.addNewMovie(toyStory);

    }

    //@Test
    public void updateMovie() {
        //given
        String darkNightRisesCrew = "Tom Hardy";
        Movie darkNightRises = new Movie(null, null, null, darkNightRisesCrew, null);
        Integer movieId = 3;
        stubFor(put(urlPathMatching("/movieservice/v1/movie/([0-9]+)"))
                .withRequestBody(matchingJsonPath("$.cast", equalTo(darkNightRisesCrew)))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("updatemovie-template.json")));


        //when
        Movie updatedMovie = moviesRestClient.updateMovie(movieId, darkNightRises);

        //then
        String updatedCastName = "Christian Bale, Heath Ledger , Michael Caine, Tom Hardy";
        assertTrue(updatedMovie.getCast().contains(darkNightRisesCrew));


    }

    @Test(expected = MovieErrorResponse.class)
    public void updateMovie_Not_Found() {
        //given
        String darkNightRisesCrew = "Tom Hardy";
        Movie darkNightRises = new Movie(null, null, null, darkNightRisesCrew, null);
        Integer movieId = 100;
        stubFor(put(urlPathMatching("/movieservice/v1/movie/([0-9]+)"))
                .withRequestBody(matchingJsonPath("$.cast", equalTo(darkNightRisesCrew)))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        //when
        moviesRestClient.updateMovie(movieId, darkNightRises);
    }

    @Test
    public void deleteMovie() {

        //given
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 4")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.CREATED.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie.json")));

        stubFor(delete(urlPathMatching("/movieservice/v1/movie/([0-9]+)"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withBody("Movie Deleted Successfully")));

        String toyStoryCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, toyStoryCrew, LocalDate.of(2019, 06, 20));
        Movie movie = moviesRestClient.addNewMovie(toyStory);
        Integer movieId = movie.getMovie_id().intValue();

        //when
        String response = moviesRestClient.deleteMovieById(movieId);

        //then
        String expectedResponse = "Movie Deleted Successfully";
        assertEquals(expectedResponse, response);

    }

    @Test(expected = MovieErrorResponse.class)
    public void deleteMovie_notFound() {

        //given
        stubFor(delete(urlPathMatching("/movieservice/v1/movie/([0-9]+)"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())));

        Integer movieId = 100;

        //when
        moviesRestClient.deleteMovieById(movieId);

    }

    @Test
    public void deleteMovieByName() {

        //given
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 5")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.CREATED.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie.json")));

        stubFor(delete(urlPathMatching("/movieservice/v1/movieName/.*"))
                .willReturn(WireMock.ok()));

        String toyStoryCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 5", 2019, toyStoryCrew, LocalDate.of(2019, 06, 20));
        Movie movie = moviesRestClient.addNewMovie(toyStory);

        //when
        String responseMessage = moviesRestClient.deleteMovieByName(movie.getName());

        //then
        assertEquals("Movie Deleted SuccessFully", responseMessage);

        verify(postRequestedFor(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 5"))));
        verify(deleteRequestedFor((urlPathMatching("/movieservice/v1/movieName/.*"))));
        verify(exactly(1), postRequestedFor(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 5"))));
        verify(moreThan(1), deleteRequestedFor((urlPathMatching("/movieservice/v1/movieName/.*"))));


    }

    @Test(expected = MovieErrorResponse.class)
    public void deleteMovieByName_NotFound() {

        //given
        String movieName = "ABC";
        stubFor(delete(urlPathMatching("/movieservice/v1/movieName/.*"))
                .willReturn(WireMock.serverError()));


        //then
        moviesRestClient.deleteMovieByName(movieName);

    }

}
