package com.learnwiremock.service;

import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import com.learnwiremock.helper.TestHelper;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesRestClientTest {

    MoviesRestClient moviesRestClient = null;
    WebClient webClient = null;

    @BeforeEach
    void setUp() {
        int port = 8081;
        final String baseUrl = String.format("http://localhost:%s/", port);
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);

    }

    @Test
    void getAllMovies() {
        //when
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println("movieList : " + movieList);

        //then
        assertTrue(!movieList.isEmpty());
    }

    @Test
    void retrieveMovieById() {
        //given
        Integer movieId = 1;

        //when
        Movie movie = moviesRestClient.retrieveMovieById(movieId);

        //then
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void retrieveMovieById_NotFound() {
        //given
        Integer movieId = 100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieById(movieId));

    }

    @Test
    void retrieveMovieByName() {
        //given
        String movieName = "Avengers";

        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(movieName);

        //then
        String expectedCastName = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(expectedCastName, movieList.get(0).getCast());
    }

    @Test
    void retrieveMovieByName_Not_Found() {
        //given
        String movieName = "ABC";

        //whenretrieveMovieByYear
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByName(movieName));
    }


    @Test
    void retrieveMovieByYear() {
        //given
        Integer year = 2012;

        //when
        List<Movie> movieList = moviesRestClient.retreieveMovieByYear(year);

        //then
        assertEquals(2, movieList.size());

    }

    @Test
    void retrieveMovieByYear_Not_Found() {
        //given
        Integer year = 1950;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retreieveMovieByYear(year));

    }

    @Test
    void addNewMovie() {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, batmanBeginsCrew, LocalDate.of(2019, 06, 20));

        //when
        Movie movie = moviesRestClient.addNewMovie(toyStory);

        //then
        assertTrue(movie.getMovie_id() != null);

    }

    @Test
    @DisplayName("Passing the Movie name and year as Null")
    void addNewMovie_InvlaidInput() {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, null, null, batmanBeginsCrew, LocalDate.of(2019, 06, 20));

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addNewMovie(toyStory));

    }

    @Test
    void updateMovie() {
        //given
        String darkNightRisesCrew = "Tom Hardy";
        Movie darkNightRises = new Movie(null, null, null, darkNightRisesCrew, null);
        Integer movieId = 3;

        //when
        Movie updatedMovie = moviesRestClient.updateMovie(movieId, darkNightRises);

        //then
        String updatedCastName = "Christian Bale, Heath Ledger , Michael Caine, Tom Hardy";
        assertTrue(updatedMovie.getCast().contains(darkNightRisesCrew));


    }

    @Test
    void updateMovie_Not_Found() {
        //given
        String darkNightRisesCrew = "Tom Hardy";
        Movie darkNightRises = new Movie(null, null, null, darkNightRisesCrew, null);
        Integer movieId = 100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class,()-> moviesRestClient.updateMovie(movieId, darkNightRises));
    }

    @Test
    void deleteMovie() {

        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, batmanBeginsCrew, LocalDate.of(2019, 06, 20));
        Movie movie = moviesRestClient.addNewMovie(toyStory);
        Integer movieId=movie.getMovie_id().intValue();

        //when
        String response = moviesRestClient.deleteMovieById(movieId);

        //then
        String expectedResponse = "Movie Deleted Successfully";
        assertEquals(expectedResponse, response);

    }

    @Test
    void deleteMovie_notFound() {

        //given
        Integer movieId=100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, ()-> moviesRestClient.deleteMovieById(movieId)) ;

    }


    @Test
    @Disabled
    void getAllMovies_Exception() {
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveAllMovies());
    }

}
