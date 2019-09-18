package com.learnwiremock.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(WireMockExtension.class)
public class MoviesRestClientServerFaultTest {

    MoviesRestClient moviesRestClient;
    WebClient webClient;

    @InjectServer
    WireMockServer wireMockServer;

    @ConfigureWireMock
    Options options = wireMockConfig().
            port(8088)
            .notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));

    TcpClient tcpClient = TcpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(5))
                        .addHandlerLast(new WriteTimeoutHandler(5));
            });

    @BeforeEach
    void setUp() {
        int port = wireMockServer.port();
        String baseUrl = String.format("http://localhost:%s/", port);
        System.out.println("baseUrl : " + baseUrl);
        //webClient = WebClient.create(baseUrl);
        webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl(baseUrl).build();
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    void retrieveAllMovies() {

        //given
        stubFor(get(anyUrl())
                .willReturn(serverError()));

        //then
        assertThrows(MovieErrorResponse.class, ()->moviesRestClient.retrieveAllMovies());

    }

    @Test
    void retrieveAllMovies_503_serviceUnAvailable() {

        //given
        stubFor(get(anyUrl())
                .willReturn(serverError()
                .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                .withBody("Service Unavailable")));

        //then
        MovieErrorResponse movieErrorResponse = assertThrows(MovieErrorResponse.class, ()->moviesRestClient.retrieveAllMovies());
        assertEquals("Service Unavailable", movieErrorResponse.getMessage());

    }

    @Test
    void retrieveAllMovies_FaultResponse() {

        //given
        stubFor(get(anyUrl())
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        //then
        MovieErrorResponse movieErrorResponse = assertThrows(MovieErrorResponse.class, ()->moviesRestClient.retrieveAllMovies());
        String errorMessage="reactor.core.Exceptions$ReactiveException: reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response";
        assertEquals(errorMessage, movieErrorResponse.getMessage());

    }

    @Test
    void retrieveAllMovies_RandomDataThenClose() {

        //given
        stubFor(get(anyUrl())
                .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        //then
         assertThrows(MovieErrorResponse.class, ()->moviesRestClient.retrieveAllMovies());

    }

    @Test
    void retrieveAllMovies_fixedDelay() {

        //given
        stubFor(get(anyUrl())
                .willReturn(ok().withFixedDelay(10000)));

        //then
        assertThrows(MovieErrorResponse.class, ()->moviesRestClient.retrieveAllMovies());

    }

    @Test
    void retrieveAllMovies_RandomDelay() {

        //given
        stubFor(get(anyUrl())
                .willReturn(ok().withUniformRandomDelay(6000,10000)));

        //then
        assertThrows(MovieErrorResponse.class, ()->moviesRestClient.retrieveAllMovies());

    }

}

