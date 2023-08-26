package com.gamecity.scrabble.api.model;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;
import lombok.Setter;

/**
 * The {@link org.springframework.security.core.userdetails.User user} object to use in Spring
 * authorization and authentication processes
 * 
 * @author ekarakus
 */
@Getter
@Setter
public class User extends org.springframework.security.core.userdetails.User {

    private static final long serialVersionUID = 3044333693857953700L;

    private Long id;
    private String preferredLanguage;

    /**
     * Creates a new User object
     * 
     * @param id                    <code>id</code> of the user
     * @param username              <code>username</code> of the user
     * @param password              <code>password</code> of the user
     * @param preferredLanguage     <code>preferredLanguage</code> of the user
     * @param enabled               whether the user is enabled
     * @param accountNonExpired     whether the user account is not expired
     * @param credentialsNonExpired whether the user credentials is not expired
     * @param accountNonLocked      whether the user account is not locked
     * @param authorities           <code>authorities</code> of the user
     */
    public User(Long id, String username, String password, String preferredLanguage, boolean enabled,
            boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.id = id;
        this.preferredLanguage = preferredLanguage;
    }

}
