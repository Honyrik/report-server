package ru.tehnobear.essence.share.plugin;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface AuthorizationPlugin {
    Mono<String> authorization(ServerWebExchange exchange, String user);
}
