package com.gamecity.scrabble.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.gamecity.scrabble.dao.UserDao;
import com.gamecity.scrabble.entity.BaseAuthority;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Role;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.service.UserRoleService;
import com.gamecity.scrabble.service.UserService;
import com.gamecity.scrabble.service.exception.UserException;
import com.gamecity.scrabble.service.exception.error.UserError;
import com.gamecity.scrabble.validator.AlphabeticNameValidator;
import com.gamecity.scrabble.validator.EmailValidator;
import com.gamecity.scrabble.validator.PasswordValidator;

@Service(value = "userService")
class UserServiceImpl extends AbstractServiceImpl<User, UserDao> implements UserService, UserDetailsService {

    private static final String ROLE_PREFIX = "ROLE_";

    private UserRoleService userRoleService;

    @Autowired
    void setUserRoleService(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user = baseDao.getByUsername(username);
        if (user == null) {
            throw new UserException(UserError.NOT_FOUND);
        }

        final List<BaseAuthority> authorities = new ArrayList<>();
        user.setAuthorities(authorities);

        final List<Role> roles = userRoleService.getRolesByUser(user.getId());
        if (!roles.isEmpty()) {
            roles.forEach(roleType -> authorities.add(new BaseAuthority(ROLE_PREFIX + roleType.name())));
        }

        return user;
    }

    @Override
    @Transactional
    public User save(User user) {
        if (user.getId() != null) {
            return update(user);
        }

        if (!new EmailValidator().isValid(user.getEmail())) {
            throw new UserException(UserError.EMAIL_ADDRESS_NOT_VALID);
        }

        if (!new PasswordValidator().isValid(user.getPassword())) {
            throw new UserException(UserError.PASSWORD_NOT_STRONG);
        }

        if (!new AlphabeticNameValidator().isValid(user.getUsername())) {
            throw new UserException(UserError.USERNAME_NOT_VALID);
        }

        final User userByUsername = baseDao.getByUsername(user.getUsername());
        if (userByUsername != null) {
            throw new UserException(UserError.USERNAME_IN_USE);
        }

        final User userByEmail = baseDao.getByEmail(user.getEmail());
        if (userByEmail != null) {
            throw new UserException(UserError.EMAIL_ADDRESS_IN_USE);
        }

        // enable the user
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);

        // set the default preferred language
        user.setPreferredLanguage(Language.en);
        final User savedUser = baseDao.save(user);

        userRoleService.add(savedUser.getId(), Role.USER);

        return user;
    }

    // TOD this method can be replaced with patch operation since it only updates the password
    private User update(User user) {
        final User existingUser = baseDao.get(user.getId());

        if (StringUtils.isNotEmpty(user.getPassword())) {
            if (!new PasswordValidator().isValid(user.getPassword())) {
                throw new UserException(UserError.PASSWORD_NOT_STRONG);
            }
            existingUser.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        }
        existingUser.setPreferredLanguage(user.getPreferredLanguage());
        return baseDao.save(existingUser);
    }

    @Override
    public User get(Long id) {
        final User user = baseDao.get(id);
        if (user == null) {
            throw new UserException(UserError.NOT_FOUND);
        } else if (!user.isEnabled()) {
            throw new UserException(UserError.ACCOUNT_DISABLED);
        } else if (!user.isAccountNonExpired()) {
            throw new UserException(UserError.ACCOUNT_EXPIRED);
        } else if (!user.isAccountNonLocked()) {
            throw new UserException(UserError.ACCOUNT_LOCKED);
        } else if (!user.isCredentialsNonExpired()) {
            throw new UserException(UserError.CREDENTIALS_EXPIRED);
        }
        return user;
    }

}
