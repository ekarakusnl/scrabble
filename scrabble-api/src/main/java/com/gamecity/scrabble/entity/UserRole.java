package com.gamecity.scrabble.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

import com.gamecity.scrabble.Constants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A user role is a representation of a {@link Role role} assigned to a {@link User user}
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Entity(name = "UserRole")
@Table(name = "user_roles", uniqueConstraints = {
        @UniqueConstraint(name = "UK_USER_ROLE", columnNames = {
                "user_id", "role", "enabled"
        })
})
@NamedQuery(name = Constants.NamedQuery.getRolesByUserId, query = "Select ur.role from UserRole ur where ur.userId = :userId")
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
