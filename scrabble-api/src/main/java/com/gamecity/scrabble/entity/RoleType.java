package com.gamecity.scrabble.entity;

/**
 * Role types of {@link User users}
 * 
 * @author ekarakus
 */
public enum RoleType {

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
