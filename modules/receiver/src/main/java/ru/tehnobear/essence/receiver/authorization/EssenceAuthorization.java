package ru.tehnobear.essence.receiver.authorization;

import org.springframework.http.HttpCookie;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.TAuthorization;
import ru.tehnobear.essence.security.essence.EssenceAuthenticationManager;
import ru.tehnobear.essence.security.essence.EssenceAuthenticationProperties;
import ru.tehnobear.essence.share.exception.ForbiddenException;
import ru.tehnobear.essence.share.exception.UnauthorizedException;
import ru.tehnobear.essence.share.plugin.AuthorizationPlugin;
import ru.tehnobear.essence.share.util.Util;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class EssenceAuthorization implements AuthorizationPlugin {
    private static final String QUERY_KEY = "query";
    private static final String SESSION_KEY = "session";
    protected final String name;
    protected final EssenceAuthenticationProperties properties;
    private final WebClient client;
    public EssenceAuthorization(TAuthorization authorization, Map<String, Object> param) {
        this.name = authorization.getCvName();
        this.properties = Util.objectMapper.convertValue(param, EssenceAuthenticationProperties.class);
        this.client = WebClient.create(this.properties.getUrl());
    }
    @Override
    public Mono<String> authorization(ServerWebExchange exchange, String user) {
        var sessionCookie = Optional.ofNullable(exchange.getRequest()
                        .getCookies()
                        .get(this.properties.getCookieKey()))
                .orElse(new ArrayList<>()).stream().findFirst().orElse(null);
        var sessionQuery = Optional.ofNullable(exchange.getRequest()
                        .getQueryParams()
                        .get(this.properties.getSessionKey()))
                .orElse(new ArrayList<>()).stream().findFirst().orElse(null);
        if (sessionQuery != null || sessionCookie != null) {
            return getSession(sessionCookie, sessionQuery);
        }
        throw new UnauthorizedException("Not auth");
    }

    private Mono<String> getSession(HttpCookie sessionCookie, String sessionQuery) {
        var build = client.post()
                .uri(uriBuilder -> uriBuilder.queryParam(QUERY_KEY, this.properties.getSessionQuery()).build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        if (sessionQuery != null) {
            build.body(BodyInserters.fromFormData(SESSION_KEY, sessionQuery));
        }
        if (sessionCookie != null) {
            build.cookie(sessionCookie.getName(), sessionCookie.getValue());
        }

        return build
            .retrieve()
            .bodyToMono(EssenceAuthenticationManager.EssenceResult.class)
            .map(res -> {
                if (res.getSuccess() && !res.getData().isEmpty()) {
                    var user = res.getData().stream().map(val -> val.get(this.properties.getUserKey())).findFirst().get();
                    return (String) user;
                }
                throw new ForbiddenException("Not auth");
            });
    }
}
