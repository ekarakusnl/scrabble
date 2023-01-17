package com.gamecity.scrabble.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

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
                @UniqueConstraint(name = "UK_USER_ROLE", columnNames = { "user_id", "role", "enabled" }) })
@NamedQuery(name = Constants.NamedQuery.getRolesByUserId,
        query = "Select ur.role from UserRole ur where ur.userId = :userId")
public class UserRole extends AbstractEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Type(type = "org.hibernate.type.NumericBooleanType")
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

}
