package com.gamecity.scrabble.service.impl;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.gamecity.scrabble.dao.UserDao;
import com.gamecity.scrabble.entity.BaseAuthority;
import com.gamecity.scrabble.entity.Language;
import com.gamecity.scrabble.entity.Role;
import com.gamecity.scrabble.entity.User;
import com.gamecity.scrabble.entity.UserType;
import com.gamecity.scrabble.service.UserRoleService;
import com.gamecity.scrabble.service.UserService;
import com.gamecity.scrabble.service.exception.GenericException;
import com.gamecity.scrabble.service.exception.UserException;
import com.gamecity.scrabble.service.exception.error.UserError;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestUserService extends AbstractServiceTest {

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService = new UserServiceImpl(userRoleService);

    private User user;

    @BeforeEach
    void beforeEach() {
        ((UserServiceImpl) userService).setBaseDao(userDao);
        this.user = User.builder().email("tester@gamecity.com").username("tester").password("Scrabble.102").build();
    }

    @Test
    void test_not_valid_email() {
        user.setEmail("tester");

        try {
            userService.save(user);

            fail("Invalid email is accepted");
        } catch (UserException e) {
            assertThat(e.getCode(), equalTo(UserError.EMAIL_ADDRESS_NOT_VALID.getCode()));
        }
    }

    @Test
    void test_null_email() {
        user.setEmail(null);

        try {
            userService.save(user);

            fail("Invalid email is accepted");
        } catch (GenericException e) {
            assertThat(e.getMessage(), equalTo("Email cannot be null"));
        }
    }

    @Test
    void test_empty_email() {
        user.setEmail("");

        try {
            userService.save(user);

            fail("Invalid email is accepted");
        } catch (GenericException e) {
            assertThat(e.getMessage(), equalTo("Email cannot be null"));
        }
    }

    @Test
    void test_not_unique_email() {
        user.setEmail("tester@gamecity.com");

        when(userDao.getByEmail(eq("tester@gamecity.com"))).thenReturn(mock(User.class));

        try {
            userService.save(user);

            fail("Non unique email is accepted");
        } catch (UserException e) {
            assertThat(e.getCode(), equalTo(UserError.EMAIL_ADDRESS_IN_USE.getCode()));
        }
    }

    @Test
    void test_not_unique_username() {
        user.setUsername("admin");

        when(userDao.getByUsername(eq("admin"))).thenReturn(mock(User.class));

        try {
            userService.save(user);

            fail("Non unique username is accepted");
        } catch (UserException e) {
            assertThat(e.getCode(), equalTo(UserError.USERNAME_IN_USE.getCode()));
        }
    }

    @Test
    void test_username_is_null() {
        user.setUsername(null);

        try {
            userService.save(user);

            fail("Username with null value is accepted");
        } catch (GenericException e) {
            assertThat(e.getMessage(), equalTo("Name cannot be null"));
        }
    }

    @Test
    void test_username_is_empty() {
        user.setUsername("");

        try {
            userService.save(user);

            fail("Username with empty string is accepted");
        } catch (GenericException e) {
            assertThat(e.getMessage(), equalTo("Name cannot be null"));
        }
    }

    @Test
    void test_username_is_not_alphabetic() {
        user.setUsername("12313");

        try {
            userService.save(user);

            fail("Non alphabetic username is accepted");
        } catch (UserException e) {
            assertThat(e.getCode(), equalTo(UserError.USERNAME_NOT_VALID.getCode()));
        }
    }

    @Test
    void test_password_is_null() {
        user.setPassword(null);

        try {
            userService.save(user);

            fail("Null password is accepted");
        } catch (GenericException e) {
            assertThat(e.getMessage(), equalTo("Password cannot be null"));
        }
    }

    @Test
    void test_password_is_empty() {
        user.setPassword("");

        try {
            userService.save(user);

            fail("Empty password is accepted");
        } catch (GenericException e) {
            assertThat(e.getMessage(), equalTo("Password cannot be null"));
        }
    }

    @Test
    void test_password_is_not_strong() {
        user.setPassword("test");

        try {
            userService.save(user);

            fail("Weak password is accepted");
        } catch (UserException e) {
            assertThat(e.getCode(), equalTo(UserError.PASSWORD_NOT_STRONG.getCode()));
        }
    }

    @Test
    void test_create_user() {
        when(userDao.save(user)).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final User savedUser = userService.save(user);

        assertThat(savedUser.getUsername(), equalTo("tester"));
        assertThat(savedUser.getEmail(), equalTo("tester@gamecity.com"));
        assertThat(savedUser.getPassword(), not("Scrabble.102"));
        assertThat(savedUser.getPreferredLanguage(), equalTo(Language.en));
        assertThat(savedUser.isAccountNonExpired(), equalTo(true));
        assertThat(savedUser.isAccountNonLocked(), equalTo(true));
        assertThat(savedUser.isCredentialsNonExpired(), equalTo(true));
        assertThat(savedUser.isEnabled(), equalTo(true));
        assertThat(savedUser.getType(), equalTo(UserType.NORMAL));

        verify(userDao, times(1)).save(savedUser);
        verify(userRoleService, times(1)).add(savedUser.getId(), Role.USER);
    }

    @Test
    void test_find_existing_user() {
        final String username = "tester";

        when(userDao.getByUsername(eq(username))).thenReturn(mock(User.class));

        assertThat(userService.loadUserByUsername(username), notNullValue());
    }

    @Test
    void test_not_existing_user_not_found() {
        String username = "tester";

        try {
            userService.loadUserByUsername(username);

            fail("Not existing user is found");
        } catch (UserException e) {
            assertThat(e.getCode(), equalTo(UserError.NOT_FOUND.getCode()));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_find_user_with_roles() {
        final String username = "tester";

        final User mockUser = mock(User.class);

        when(userRoleService.getRolesByUser(mockUser.getId())).thenReturn(Arrays.asList(Role.USER));
        when(userDao.getByUsername(eq(username))).thenReturn(mockUser);

        final User existingUser = userService.loadUserByUsername(username);

        assertThat(existingUser, notNullValue());

        final ArgumentCaptor<List<BaseAuthority>> authorities = ArgumentCaptor.forClass(List.class);

        verify(mockUser).setAuthorities(authorities.capture());

        assertThat(authorities.getValue().size(), equalTo(1));
        assertThat(authorities.getValue().get(0).getAuthority(), equalTo("ROLE_USER"));
    }

    @Test
    void test_get_user() {
        when(userDao.get(eq(DEFAULT_USER_ID))).thenReturn(User.builder()
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build());

        assertThat(userService.get(DEFAULT_USER_ID), notNullValue());
    }

    @Test
    void test_user_by_id_not_found() {
        try {
            userService.get(DEFAULT_USER_ID);

            fail("Not existing user is found");
        } catch (UserException e) {
            assertThat(e.getCode(), equalTo(UserError.NOT_FOUND.getCode()));
        }
    }

    @Test
    void test_get_disabled_user() {
        when(userDao.get(eq(DEFAULT_USER_ID))).thenReturn(User.builder()
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(false)
                .build());

        try {
            userService.get(DEFAULT_USER_ID);

            fail("Disabled user is found");
        } catch (UserException e) {
            assertThat(e.getCode(), equalTo(UserError.ACCOUNT_DISABLED.getCode()));
        }
    }

    @Test
    void test_get_account_expired_user() {
        when(userDao.get(eq(DEFAULT_USER_ID))).thenReturn(User.builder()
                .accountNonExpired(false)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build());

        try {
            userService.get(DEFAULT_USER_ID);

            fail("Account expired user is found");
        } catch (UserException e) {
            assertThat(e.getCode(), equalTo(UserError.ACCOUNT_EXPIRED.getCode()));
        }
    }

    @Test
    void test_get_account_locked_user() {
        when(userDao.get(eq(DEFAULT_USER_ID))).thenReturn(User.builder()
                .accountNonExpired(true)
                .accountNonLocked(false)
                .credentialsNonExpired(true)
                .enabled(true)
                .build());

        try {
            userService.get(DEFAULT_USER_ID);

            fail("Account locked user is found");
        } catch (UserException e) {
            assertThat(e.getCode(), equalTo(UserError.ACCOUNT_LOCKED.getCode()));
        }
    }

    @Test
    void test_get_credentials_expired_user() {
        when(userDao.get(eq(DEFAULT_USER_ID))).thenReturn(User.builder()
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(false)
                .enabled(true)
                .build());

        try {
            userService.get(DEFAULT_USER_ID);

            fail("Credentials expired user is found");
        } catch (UserException e) {
            assertThat(e.getCode(), equalTo(UserError.CREDENTIALS_EXPIRED.getCode()));
        }
    }

    @Test
    void test_update_user() {
        final User existingUser = User.builder()
                .id(DEFAULT_USER_ID)
                .email("tester@gamecity.com")
                .username("tester")
                .password("Scrabble.102")
                .build();

        user.setId(DEFAULT_USER_ID);
        user.setUsername("Failed");
        user.setEmail("failed@gamecity.com");
        user.setPreferredLanguage(Language.fr);

        when(userDao.get(eq(DEFAULT_USER_ID))).thenReturn(existingUser);
        when(userDao.save(eq(existingUser))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final User updatedUser = userService.save(user);

        assertThat(updatedUser.getUsername(), not(equalTo("Failed")));
        assertThat(updatedUser.getEmail(), not(equalTo("failed@gamecity.com")));
        assertThat(updatedUser.getPreferredLanguage(), equalTo(Language.fr));
    }

    @Test
    void test_update_user_with_empty_password() {
        user.setId(DEFAULT_USER_ID);
        user.setPassword("");

        final User mockUser = mock(User.class);

        when(userDao.get(eq(DEFAULT_USER_ID))).thenReturn(mockUser);

        userService.save(user);

        verify(mockUser, times(0)).setPassword(any());
    }

    @Test
    void test_update_user_with_weak_password() {
        user.setId(DEFAULT_USER_ID);
        user.setPassword("Test");

        final User mockUser = mock(User.class);

        when(userDao.get(eq(DEFAULT_USER_ID))).thenReturn(mockUser);

        try {
            userService.save(user);

            fail("Weak password is accepted");
        } catch (UserException e) {
            assertThat(e.getCode(), equalTo(UserError.PASSWORD_NOT_STRONG.getCode()));
        }
    }

}
