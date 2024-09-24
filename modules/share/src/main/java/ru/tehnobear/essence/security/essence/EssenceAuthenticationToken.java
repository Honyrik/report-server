package ru.tehnobear.essence.security.essence;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class EssenceAuthenticationToken extends AbstractAuthenticationToken {
    private UserDetails principal;
    public EssenceAuthenticationToken(UserDetails principal) {
        super(principal.getAuthorities());
        super.setAuthenticated(true);
        this.principal = principal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
