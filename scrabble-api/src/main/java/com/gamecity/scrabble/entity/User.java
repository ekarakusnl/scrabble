package com.gamecity.scrabble.entity;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;
import org.springframework.security.core.userdetails.UserDetails;

import com.gamecity.scrabble.Constants;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * User represents a person authorized to use the application. A user is defined with the {@link Role
 * roles} assigned to him to be able to use certain features
 * 
 * @author ekarakus
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Entity(name = "User")
@Table(name = "users",
        uniqueConstraints = { @UniqueConstraint(name = "UK_USER_NAME", columnNames = "username"),
                @UniqueConstraint(name = "UK_USER_EMAIL", columnNames = "email") })
@NamedQueries({
        @NamedQuery(name = Constants.NamedQuery.getUserByUsername,
                query = "Select u from User u where u.username = :username"),
        @NamedQuery(name = Constants.NamedQuery.getUserByEmail, query = "Select u from User u where u.email= :email") })
public class User extends AbstractEntity implements UserDetails {

    private static final long serialVersionUID = -4039028552745359265L;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Type(type = "org.hibernate.type.NumericBooleanType")
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Type(type = "org.hibernate.type.NumericBooleanType")
    @Column(name = "account_non_expired", nullable = false)
    private boolean accountNonExpired = true;

    @Type(type = "org.hibernate.type.NumericBooleanType")
    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Type(type = "org.hibernate.type.NumericBooleanType")
    @Column(name = "credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_language")
    private Language preferredLanguage;

    @Transient
    private Collection<? extends BaseAuthority> authorities;

}
