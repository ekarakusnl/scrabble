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
 * Role represents the authority of a {@link User user}
 * 
 * @author ekarakus
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity(name = "Role")
@Table(name = "roles", uniqueConstraints = { @UniqueConstraint(name = "UK_ROLE_TYPE", columnNames = "type") })
@NamedQuery(name = Constants.NamedQuery.getRoleByRoleType, query = "Select r from Role r where r.type = :type")
public class Role extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RoleType type;

    @Column(name = "enabled", nullable = false, columnDefinition = "tinyint default 1")
    private boolean enabled;

}
