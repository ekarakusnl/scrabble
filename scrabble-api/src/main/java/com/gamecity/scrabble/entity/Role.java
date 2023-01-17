package com.gamecity.scrabble.entity;

/**
 * Roles of a {@link User user}
 * 
 * @author ekarakus
 */
public enum Role {

    /**
     * Standard user role limited to game actions
     */
    USER,

    /**
     * Admin user role limited to user operations
     */
    ADMIN,

    /**
     * System user role limited to automatized operations such as scheduled jobs
     */
    SYSTEM

}
