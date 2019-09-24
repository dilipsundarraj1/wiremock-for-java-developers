package com.learnwiremock.service;

import com.learnwiremock.constants.MoviesAppConstants;
import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static com.learnwiremock.constants.MoviesAppConstants.MOVIE_BY_ID_PATH_PARAM_V1;


@Slf4j
public class MoviesRestClient {

    private WebClient webClient;

    public MoviesRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Movie> retrieveAllMovies() {

        //http://localhost:8081/movieservice/v1/allMovies
        try {
        return webClient.get().uri(MoviesAppConstants.GET_ALL_MOVIES_V1)
                .retrieve()
                .bodyToFlux(Movie.class)
                .collectList()
                .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retrieveAllMovies. Status code is {} and the message is {} ", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in retrieveAllMovies and the message is {} ", ex.getMessage() + ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public Movie retrieveMovieById(Integer movieId) {

        //http://localhost:8081/movieservice/v1/movie/100
        try {
            return webClient.get().uri(MOVIE_BY_ID_PATH_PARAM_V1, movieId)
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retrieveMovieById. Status code is {} and the message is {} ", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in retrieveMovieById and the message is {} ", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public List<Movie> retrieveMoviebyName(String name) {

//        http://localhost:8081/movieservice/v1/movieName?movie_name=ABC

        String retrieveByNameUri = UriComponentsBuilder.fromUriString(MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1)
                .queryParam("movie_name", name)
                .buildAndExpand()
                .toUriString();
        try {
            return webClient.get().uri(retrieveByNameUri)
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retrieveMoviebyName. Status code is {} and the message is {} ", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in retrieveMoviebyName and the message is {} ", ex);
            throw new MovieErrorResponse(ex);
        }

    }

    public List<Movie> retrieveMoviebyYear(Integer movieYear) {

//        http://localhost:8081/movieservice/v1/movieYear?year=1950

        String retrieveByNameUri = UriComponentsBuilder.fromUriString(MoviesAppConstants.MOVIE_BY_YEAR_QUERY_PARAM_V1)
                .queryParam("year", movieYear)
                .buildAndExpand()
                .toUriString();
        try {
            return webClient.get().uri(retrieveByNameUri)
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in retrieveMoviebyYear. Status code is {} and the message is {} ", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in retrieveMoviebyYear and the message is {} ", ex);
            throw new MovieErrorResponse(ex);
        }

    }

    public Movie addMovie( Movie newMovie){

        //http://localhost:8081/movieservice/v1/movie
        try {
        return webClient.post().uri(MoviesAppConstants.ADD_MOVIE_V1)
                .syncBody(newMovie)
                .retrieve()
                .bodyToMono(Movie.class)
                .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in addMovie. Status code is {} and the message is {} ", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in addMovie and the message is {} ", ex);
            throw new MovieErrorResponse(ex);
        }

    }


    public Movie updateMovie(Integer movieId, Movie movie){
        try {
            return webClient.put().uri(MOVIE_BY_ID_PATH_PARAM_V1, movieId)
                    .syncBody(movie)
                    .retrieve()
                    .bodyToMono(Movie.class)
                    .block();
        }catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in updateMovie. Status code is {} and the message is {} ", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in updateMovie and the message is {} ", ex);
            throw new MovieErrorResponse(ex);
        }

    }

    public String deleteMovie(Integer movieId){

        try{
            return webClient.delete().uri(MOVIE_BY_ID_PATH_PARAM_V1, movieId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        }catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in deleteMovie. Status code is {} and the message is {} ", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in deleteMovie and the message is {} ", ex);
            throw new MovieErrorResponse(ex);
        }
    }

    public String deleteMovieByName(String movieName){

        try{
            String deleteMovieByNameURI = UriComponentsBuilder.fromUriString(MoviesAppConstants.MOVIE_BY_NAME_QUERY_PARAM_V1)
                    .queryParam("movie_name", movieName)
                    .buildAndExpand()
                    .toUriString();

             webClient.delete().uri(deleteMovieByNameURI)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

        }catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in deleteMovie. Status code is {} and the message is {} ", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new MovieErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex) {
            log.error("Exception in deleteMovie and the message is {} ", ex);
            throw new MovieErrorResponse(ex);
        }

        return "Movie Deleted Successfully";
    }

}
