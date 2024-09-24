package ru.tehnobear.essence.receiver.authorization;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.TAuthorization;
import ru.tehnobear.essence.share.plugin.AuthorizationPlugin;

import java.util.Map;

public class NoAuthorization implements AuthorizationPlugin {
    protected final String name;
    protected final Map<String, Object> param;
    public NoAuthorization(TAuthorization authorization, Map<String, Object> param) {
        this.name = authorization.getCvName();
        this.param = param;
    }
    @Override
    public Mono<String> authorization(ServerWebExchange exchange, String user) {
        return Mono.just(user);
    }
}
