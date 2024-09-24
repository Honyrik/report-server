package ru.tehnobear.essence.security.essence;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.security.type", havingValue = "essence")
@EnableConfigurationProperties(EssenceAuthenticationProperties.class)
public class EssenceAuthenticationManager {
    private static final String QUERY_KEY = "query";
    private static final String SESSION_KEY = "session";
    private static final String TOKEN_KEY = "cv_token";
    private static final String USER_KEY = "cv_login";
    private static final String PWD_KEY = "cv_password";
    private EssenceAuthenticationProperties properties;
    private WebClient client;
    @Autowired
    public void setProperties(EssenceAuthenticationProperties properties) {
        this.properties = properties;
        this.client = WebClient.create(this.properties.getUrl());
    }
    public Mono<Authentication> checkAuth(ServerWebExchange exchange) {
        var header = Optional.ofNullable(exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION))
                .orElse(new ArrayList<>()).stream().findFirst().orElse(null);
        if (header != null) {
            if (header.toLowerCase().startsWith("basic")) {
                return authBasic(exchange, header.substring(6));
            }
            if (header.toLowerCase().startsWith("token")) {
                return authToken(exchange, header.substring(6));
            }
        }
        var sessionCookie = Optional.ofNullable(exchange.getRequest()
                .getCookies()
                .get(this.properties.getCookieKey()))
                .orElse(new ArrayList<>()).stream().findFirst().orElse(null);
        var sessionQuery = Optional.ofNullable(exchange.getRequest()
                .getQueryParams()
                .get(this.properties.getSessionKey()))
                .orElse(new ArrayList<>()).stream().findFirst().orElse(null);
        if (sessionQuery != null || sessionCookie != null) {
            return getSession(exchange, sessionCookie, sessionQuery);
        }
        return Mono.empty();
    }

    private Mono<Authentication> authBasic(ServerWebExchange exchange, String header) {
        var decodedBytes = Base64.getDecoder().decode(header);
        var decodedString = new String(decodedBytes);
        var login = decodedString.substring(0, decodedString.indexOf(":"));
        var pwd = decodedString.substring(decodedString.indexOf(":")+1);
        return client.post()
                .uri(uriBuilder -> uriBuilder.queryParam(QUERY_KEY, this.properties.getLoginQuery()).build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(USER_KEY, login).with(PWD_KEY, pwd))
                .retrieve()
                .bodyToMono(EssenceResult.class)
                .flatMap(res -> {
                    if (res.getSuccess() && !res.getData().isEmpty()) {
                        var token = getToken(res.getData().stream().findFirst().get());
                        return Mono.just(token);
                    }
                    return Mono.empty();
                });
    }

    private Mono<Authentication> authToken(ServerWebExchange exchange, String header) {
        return client.post()
                .uri(uriBuilder -> uriBuilder.queryParam(QUERY_KEY, this.properties.getLoginQuery()).build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(TOKEN_KEY, header))
                .retrieve()
                .bodyToMono(EssenceResult.class)
                .flatMap(res -> {
                    if (res.getSuccess() && !res.getData().isEmpty()) {
                        var token = getToken(res.getData().stream().findFirst().get());
                        return Mono.just(token);
                    }
                    return Mono.empty();
                });
    }

    private Mono<Authentication> getSession(ServerWebExchange exchange, HttpCookie sessionCookie, String sessionQuery) {
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
                .bodyToMono(EssenceResult.class)
                .flatMap(res -> {
                    if (res.getSuccess() && !res.getData().isEmpty()) {
                        var token = getToken(res.getData().stream().findFirst().get());
                        return Mono.just(token);
                    }
                    return Mono.empty();
                });
    }

    private EssenceAuthenticationToken getToken(SessionData data) {
        return new EssenceAuthenticationToken(data.getUser(this.properties));
    }

    @Data
    public static class EssenceResult {
        private Boolean success;
        private List<SessionData> data;
        @JsonProperty("err_text")
        private String errText;
        private Map<String, Object> metaData;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SessionData extends HashMap<String, Object> {
        private final String ACTION_KEY = "ca_actions";
        public EssenceUserDetails getUser(EssenceAuthenticationProperties properties) {
            var authorites = ((List<Integer>) get(ACTION_KEY))
                    .stream()
                    .map(val -> properties
                            .getMapRole()
                            .stream()
                            .filter(map -> map.getAction().equals(val))
                            .map(EssenceAuthenticationProperties.MapRole::getRole)
                            .map(EssenceGrantedAuthority::new)
                            .collect(Collectors.toSet())
                    )
                    .collect(
                            () -> new HashSet<EssenceGrantedAuthority>(),
                            (list, item) -> list.addAll(item),
                            (list1, list2) -> list1.addAll(list2)
                    );
            var userId = (String) get(properties.getUserKey());

            return EssenceUserDetails
                    .builder()
                    .authorities(authorites)
                    .username(userId)
                    .build();
        }
    }

    @Builder
    @EqualsAndHashCode
    public static class EssenceGrantedAuthority implements GrantedAuthority {
        private String authority;
        @Override
        public String getAuthority() {
            return authority;
        }
    }

    @Setter
    @EqualsAndHashCode
    @Builder
    public static class EssenceUserDetails implements UserDetails {
        private String username;
        private Set<EssenceGrantedAuthority> authorities;
        @Override
        public Collection<EssenceGrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return UUID.randomUUID().toString();
        }

        @Override
        public String getUsername() {
            return this.username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return false;
        }

        @Override
        public boolean isAccountNonLocked() {
            return false;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
