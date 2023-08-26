package com.gamecity.scrabble.model.rest;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents the jwt token of a {@link UserDto user}
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class UserTokenDto extends AbstractDto {

    @JsonProperty("token")
    private String token;

    @JsonProperty("roles")
    private Set<String> roles;

    @JsonProperty("preferredLanguage")
    private String preferredLanguage;

}
