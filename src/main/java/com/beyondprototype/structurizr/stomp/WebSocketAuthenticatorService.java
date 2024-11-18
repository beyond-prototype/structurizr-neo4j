package com.beyondprototype.structurizr.stomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class WebSocketAuthenticatorService {

    public UsernamePasswordAuthenticationToken getAuthenticatedOrFail(final String username, final String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationCredentialsNotFoundException("Username was null or empty.");
        }

        log.info("username:%s".formatted(username));
        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationCredentialsNotFoundException("Password was null or empty.");
        }
        //allow vscode client to access
        if (!username.startsWith("vscode")) {
            throw new BadCredentialsException("Bad credentials for user " + username);
        }
        //null credentials, we do not pass the password along
        return new UsernamePasswordAuthenticationToken(username,null, Collections.singleton((GrantedAuthority)()-> "USER"));
    }
}
