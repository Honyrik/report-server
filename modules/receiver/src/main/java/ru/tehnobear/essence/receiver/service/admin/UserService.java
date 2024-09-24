package ru.tehnobear.essence.receiver.service.admin;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserService {
    public Mono<String> getUser() {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(val ->
                        val.getAuthentication() != null ? val.getAuthentication().getName() : "99999")
                .defaultIfEmpty("99999");
    }
}
