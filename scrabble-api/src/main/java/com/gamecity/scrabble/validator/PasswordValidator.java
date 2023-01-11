package com.gamecity.scrabble.validator;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.gamecity.scrabble.service.exception.GenericException;

/**
 * Validates passwords. A password must contain at least 1 capital letter, 1 short letter, 1 number and 1
 * special character
 * 
 * @author ekarakus
 */
public class PasswordValidator implements Validator {

    private static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9\\\\\\\\s]).{6,}";

    private final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    @Override
    public boolean isValid(Object password) {
        if (password == null || StringUtils.isEmpty((String) password)) {
            throw new GenericException("Password cannot be null");
        }

        return pattern.matcher((String) password).matches();
    }

}
