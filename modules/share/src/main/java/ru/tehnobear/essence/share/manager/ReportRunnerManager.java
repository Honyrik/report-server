package ru.tehnobear.essence.share.manager;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import ru.tehnobear.essence.dao.entries.TDQueue;
import ru.tehnobear.essence.dao.entries.TQueue;
import ru.tehnobear.essence.share.dto.ReportBody;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.util.Util;

import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

@Component
public class ReportRunnerManager {
    private final String HMAC_ALG = "HmacSHA512";
    private WebClient client;
    @Value("${server.port:8080}")
    private String port;
    @Value("${app.report.secret}")
    public String secret;

    public ReportRunnerManager() {
        var httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(EpollChannelOption.TCP_KEEPIDLE, 300)
                .option(EpollChannelOption.TCP_KEEPINTVL, 60)
                .option(EpollChannelOption.TCP_KEEPCNT, 8)
                .responseTimeout(Duration.ofHours(24));
        this.client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
    public Mono<Result> run(TQueue queue) {
        var url = URI.create(getUrl(queue.getQueue(), String.format("http://localhost:%s/report/run", port)));
        try {
            var sign = Util.hmac(HMAC_ALG, queue.getCkId().toString(), secret);
            return client
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ReportBody.builder().ckId(queue.getCkId()).sing(sign).build())
                .retrieve()
                .bodyToMono(Result.class);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw ReportException.fromFormat("Error sign", e);
        }
    }
    private String getUrl(TDQueue queue, String defaultUrl) {
        if (queue.getCvRunnerUrl() != null && !queue.getCvRunnerUrl().trim().isEmpty()) {
            return queue.getCvRunnerUrl().trim();
        }
        if (queue.getParent() != null) {
            return getUrl(queue.getParent(), defaultUrl);
        }
        return defaultUrl;
    }
}
