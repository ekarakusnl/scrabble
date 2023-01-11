package com.gamecity.scrabble.entity;

import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;

/**
 * Authority object that extends {@link GrantedAuthority}
 * 
 * @author ekarakus
 */
@Getter
public class BaseAuthority implements GrantedAuthority {

    private static final long serialVersionUID = -3353297460325477687L;

    private String authority;

    /**
     * Base constructor taking <code>role</code> as parameter
     * 
     * @param role
     */
    public BaseAuthority(String role) {
        this.authority = role;
    }

}
