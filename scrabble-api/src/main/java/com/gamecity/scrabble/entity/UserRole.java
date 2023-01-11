package com.gamecity.scrabble.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.gamecity.scrabble.Constants;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A user role is a representation of a {@link Role role} assigned to a {@link User user}
 * 
 * @author ekarakus
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity(name = "UserRole")
@Table(name = "user_roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_USER_ROLE", columnNames = { "username", "type", "enabled" }) })
@NamedQuery(name = Constants.NamedQuery.getRoleTypesByUsername,
        query = "Select type from UserRole where username = :username")
public class UserRole extends AbstractEntity {

    @Column(name = "username", nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RoleType type;

    @Column(name = "enabled", nullable = false, columnDefinition = "tinyint default 1")
    private boolean enabled;

}
