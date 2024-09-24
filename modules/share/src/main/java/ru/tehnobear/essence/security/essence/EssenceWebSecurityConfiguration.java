package ru.tehnobear.essence.security.essence;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.PathContainer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Set;
import java.util.stream.Collectors;


@ConditionalOnProperty(name = "app.security.type", havingValue = "essence")
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
@Slf4j
@SecuritySchemes({
        @SecurityScheme(name = "essenceCookieSession", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.COOKIE, paramName = "essence.sid"),
        @SecurityScheme(name = "essenceQuerySession", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.QUERY, paramName = "session"),
        @SecurityScheme(name = "basic", type = SecuritySchemeType.HTTP, scheme = "Basic"),
        @SecurityScheme(name = "token", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = "Authorization")
})
public class EssenceWebSecurityConfiguration {
    @Autowired
    public void setExclude(
            @Value("#{'${app.security.exclude}'.split(',')}")
            String[] val
    ) {
        exclude = Set.of(val).stream().map(value -> {
            PathPatternParser pp = new PathPatternParser();
            return pp.parse(value.trim());
        }).collect(Collectors.toSet());
    }
    private Set<PathPattern> exclude;
    private final EssenceAuthenticationManager manager;

    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, @Nonnull WebFilterChain chain) {
        return chain.filter(exchange)
                .contextWrite(context -> withSecurityContext(context, exchange));
    }

    private Context withSecurityContext(Context mainContext, ServerWebExchange exchange) {
        return mainContext.putAll(
                getMonoForContext(exchange)
                        .as(ReactiveSecurityContextHolder::withSecurityContext)
                        .readOnly());
    }

    private Mono<SecurityContextImpl> getMonoForContext(ServerWebExchange exchange) {
        return manager.checkAuth(exchange).map(SecurityContextImpl::new);
    }

    @Bean
    public SecurityWebFilterChain filterChainEssence(ServerHttpSecurity http) {
        http.addFilterBefore(this::filter, SecurityWebFiltersOrder.HTTP_BASIC)
            .cors(ServerHttpSecurity.CorsSpec::disable)
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .authorizeExchange(req -> req.matchers(blockUnsecured()).permitAll().anyExchange().authenticated());
        return http.build();
    }

    ServerWebExchangeMatcher blockUnsecured() {
        return exchange -> {
            var uri = exchange.getRequest().getURI();
            var pc = PathContainer.parsePath(uri.getPath());
            boolean match = exclude.stream().anyMatch(val-> val.matches(pc));
            return match ? ServerWebExchangeMatcher.MatchResult.match() : ServerWebExchangeMatcher.MatchResult.notMatch();
        };
    }
}
