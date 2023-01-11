package com.gamecity.scrabble.model.rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents a user in the system
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@SuperBuilder
public class UserDto extends AbstractDto {

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("enabled")
    private boolean enabled;

    @JsonProperty("accountNonExpired")
    private boolean accountNonExpired;

    @JsonProperty("accountNonLocked")
    private boolean accountNonLocked;

    @JsonProperty("credentialsNonExpired")
    private boolean credentialsNonExpired;

    @JsonProperty("authorities")
    private List<String> authorities;

}
