package com.gamecity.scrabble.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents a user profile
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@SuperBuilder
public class UserProfileDto extends AbstractDto {

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty(value = "password", access = Access.WRITE_ONLY)
    private String password;

    @JsonProperty("preferredLanguage")
    private String preferredLanguage;

}
