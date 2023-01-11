package com.gamecity.scrabble.api.model;

import org.springframework.security.core.GrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Authority object that extends {@link GrantedAuthority}
 * 
 * @author ekarakus
 */
@Data
@AllArgsConstructor
public class BaseAuthority implements GrantedAuthority {

    private static final long serialVersionUID = -6831446138013219928L;

    private String authority;

}
