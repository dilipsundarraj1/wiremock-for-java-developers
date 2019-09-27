package com.learnwiremock.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.PrematureCloseException;
import reactor.netty.tcp.TcpClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

public class MoviesRestClientFaultTest {

    MoviesRestClient moviesRestClient = null;
    WebClient webClient;


    TcpClient tcpClient = TcpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .doOnConnected(connection ->
                    connection.addHandlerLast(new ReadTimeoutHandler(5))
                            .addHandlerLast(new WriteTimeoutHandler(5)));

    Options options = wireMockConfig()
            .port(8088)
            .notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options);

    @Before
    public void setUp() {
        //int port = 8081;
        int port = wireMockRule.port();
        final String baseUrl = String.format("http://localhost:%s/", port);

        //webClient = WebClient.create(baseUrl);
        webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl(baseUrl).build();
        moviesRestClient = new MoviesRestClient(webClient);

    }

    @Test(expected = MovieErrorResponse.class)
    public void getAllMovies_internal_server_Error() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(serverError()));
        //whenx
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();


        //then
        moviesRestClient.retrieveAllMovies();
    }

    @Test(expected = MovieErrorResponse.class)
    public void getAllMovies_503_error() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(serverError()
                        .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .withBody("Service Unavailable")));
        //whenx
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();


        //then
         moviesRestClient.retrieveAllMovies();

    }

    @Test(expected = MovieErrorResponse.class)
    public void getAllMovies_fault_Response() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        //then
         moviesRestClient.retrieveAllMovies();
        /*String expectedErrorMessage = "reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response";
        assertEquals(expectedErrorMessage, movieErrorResponse.getMessage());
*/
    }

    @Test(expected = MovieErrorResponse.class)
    public void getAllMovies_randomDataThenClose() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        //then
        moviesRestClient.retrieveAllMovies();

    }

    @Test(expected = MovieErrorResponse.class)
    public void getAllMovies_fixedDelay() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(ok()
                        .withFixedDelay(10000)));

        //then
        moviesRestClient.retrieveAllMovies();

    }

    @Test(expected = MovieErrorResponse.class)
    public void getAllMovies_RandaomDelay() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(ok()
                        .withUniformRandomDelay(6000, 10000)));

        //then
        moviesRestClient.retrieveAllMovies();

    }


}
