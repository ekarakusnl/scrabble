package com.gamecity.scrabble.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.gamecity.scrabble.dao.UserDao;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.service.UserRoleService;
import com.gamecity.scrabble.service.UserService;
import com.gamecity.scrabble.service.exception.GenericException;
import com.gamecity.scrabble.service.exception.UserException;
import com.gamecity.scrabble.service.exception.error.UserError;

class TestUserService extends AbstractServiceTest {

    @InjectMocks
    private UserService userService = new UserServiceImpl();

    @Mock
    private UserDao userDao;

    @Mock
    private UserRoleService userRoleService;

    private User user;

    @BeforeEach
    void beforeEach() {
        this.user = createSampleUser();
    }

    @Test
    void test_not_valid_email() {
        user.setEmail("tester");
        try {
            userService.save(user);
            fail("Email is valid");
        } catch (UserException e) {
            assertEquals(UserError.EMAIL_ADDRESS_NOT_VALID.getCode(), e.getCode());
        }
    }

    @Test
    void test_not_unique_email() {
        user.setEmail("tester@gamecity.com");
        when(userDao.getByEmail(eq("tester@gamecity.com"))).thenReturn(mock(User.class));
        try {
            userService.save(user);
            fail("Email is unique");
        } catch (UserException e) {
            assertEquals(UserError.EMAIL_ADDRESS_IN_USE.getCode(), e.getCode());
        }
    }

    @Test
    void test_not_unique_username() {
        user.setUsername("admin");
        when(userDao.getByUsername(eq("admin"))).thenReturn(mock(User.class));
        try {
            userService.save(user);
            fail("Username is unique");
        } catch (UserException e) {
            assertEquals(UserError.USERNAME_IN_USE.getCode(), e.getCode());
        }
    }

    @Test
    void test_username_is_null() {
        user.setUsername("");
        try {
            userService.save(user);
            fail("Username length is valid");
        } catch (GenericException e) {
            assertEquals("Name cannot be null", e.getMessage());
        }
    }

    @Test
    void test_username_is_not_alphabetic() {
        user.setUsername("12313");
        try {
            userService.save(user);
            fail("Username is alphabetic");
        } catch (UserException e) {
            assertEquals(UserError.USERNAME_NOT_VALID.getCode(), e.getCode());
        }
    }

    @Test
    void test_password_is_not_strong() {
        user.setPassword("test");
        try {
            userService.save(user);
            fail("Password is strong enough");
        } catch (UserException e) {
            assertEquals(UserError.PASSWORD_NOT_STRONG.getCode(), e.getCode());
        }
    }

    @Test
    void test_create_user() {
        when(userDao.save(any(User.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final User savedUser = userService.save(user);

        assertEquals("mukawwaa", savedUser.getUsername());
        assertEquals("mukawwaa_by_scrabble@gamecity.com", savedUser.getEmail());
    }

    @Test
    void test_find_existing_user() {
        final String username = "tester";
        when(userDao.getByUsername(eq(username))).thenReturn(mock(User.class));

        assertNotNull(userService.findByUsername(username));
    }

    @Test
    void test_user_by_username_not_found() {
        String username = "tester";
        try {
            userService.findByUsername(username);
            fail("User by username is found");
        } catch (UserException e) {
            assertEquals(UserError.NOT_FOUND.getCode(), e.getCode());
        }
    }

    @Test
    void test_user_by_id_not_found() {
        try {
            userService.get(DEFAULT_USER_ID);
            fail("User by id is found");
        } catch (UserException e) {
            assertEquals(UserError.NOT_FOUND.getCode(), e.getCode());
        }
    }

    @Test
    void test_get_disabled_user() {
        when(userDao.get(eq(DEFAULT_USER_ID))).thenAnswer(invocation -> {
            final User user = new User();
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            user.setEnabled(false);
            return user;
        });
        try {
            userService.get(DEFAULT_USER_ID);
        } catch (UserException e) {
            assertEquals(UserError.ACCOUNT_DISABLED.getCode(), e.getCode());
        }
    }

    @Test
    void test_get_account_expired_user() {
        when(userDao.get(eq(DEFAULT_USER_ID))).thenAnswer(invocation -> {
            final User user = new User();
            user.setAccountNonExpired(false);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            user.setEnabled(true);
            return user;
        });
        try {
            userService.get(DEFAULT_USER_ID);
        } catch (UserException e) {
            assertEquals(UserError.ACCOUNT_EXPIRED.getCode(), e.getCode());
        }
    }

    @Test
    void test_get_account_locked_user() {
        when(userDao.get(eq(DEFAULT_USER_ID))).thenAnswer(invocation -> {
            final User user = new User();
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(false);
            user.setCredentialsNonExpired(true);
            user.setEnabled(true);
            return user;
        });
        try {
            userService.get(DEFAULT_USER_ID);
        } catch (UserException e) {
            assertEquals(UserError.ACCOUNT_LOCKED.getCode(), e.getCode());
        }
    }

    @Test
    void test_get_credentials_expired_user() {
        when(userDao.get(eq(DEFAULT_USER_ID))).thenAnswer(invocation -> {
            final User user = new User();
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(false);
            user.setEnabled(true);
            return user;
        });
        try {
            userService.get(DEFAULT_USER_ID);
        } catch (UserException e) {
            assertEquals(UserError.CREDENTIALS_EXPIRED.getCode(), e.getCode());
        }
    }

    @Test
    void test_update_user() {
        final User existingUser = createSampleUser();
        existingUser.setId(1L);

        user.setId(DEFAULT_USER_ID);
        user.setUsername("Failed");
        user.setEmail("failed@gamecity.com");

        when(userDao.get(eq(DEFAULT_USER_ID))).thenReturn(existingUser);

        when(userDao.save(eq(existingUser))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final User updatedUser = userService.save(user);

        assertNotEquals("Failed", updatedUser.getUsername());
        assertNotEquals("failed@gamecity.com", updatedUser.getEmail());
    }

}
