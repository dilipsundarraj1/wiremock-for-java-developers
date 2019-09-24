package com.learnwiremock.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Configuration
public class MovieAppConfig {

    @Value("${movieapp.baseUrl}")
    private String baseUrl;

    @Bean
    public WebClient webClient(){

        TcpClient tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(5))
                            .addHandlerLast(new WriteTimeoutHandler(5));
                });

        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl(baseUrl).build();

    }
}
