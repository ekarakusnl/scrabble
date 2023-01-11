package com.gamecity.scrabble.validator;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.gamecity.scrabble.service.exception.GenericException;

/**
 * Validates whether a name consists of alphabetic characters
 * 
 * @author ekarakus
 */
public class AlphabeticNameValidator implements Validator {

    private static final String NAME_PATTERN = "[a-zA-Z]+";

    private final Pattern pattern = Pattern.compile(NAME_PATTERN);

    @Override
    public boolean isValid(Object name) {
        if (name == null || StringUtils.isEmpty((String) name)) {
            throw new GenericException("Name cannot be null");
        }

        return pattern.matcher((String) name).matches();
    }

}
