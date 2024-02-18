package com.gamecity.scrabble.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.gamecity.scrabble.dao.UserRoleDao;
import com.gamecity.scrabble.entity.Role;
import com.gamecity.scrabble.entity.UserRole;
import com.gamecity.scrabble.service.UserRoleService;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

class TestUserRoleService extends AbstractServiceTest {

    @Mock
    private UserRoleDao userRoleDao;

    @InjectMocks
    private UserRoleService userRoleService = new UserRoleServiceImpl();

    @BeforeEach
    void beforeEach() {
        ((UserRoleServiceImpl) userRoleService).setBaseDao(userRoleDao);
    }

    @Test
    void test_add_user_role() {
        when(userRoleDao.save(any())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        final UserRole userRole = userRoleService.add(DEFAULT_USER_ID, Role.USER);

        assertThat(userRole, notNullValue());
        assertThat(userRole.isEnabled(), equalTo(true));
        assertThat(userRole.getRole(), equalTo(Role.USER));
        assertThat(userRole.getUserId(), equalTo(DEFAULT_USER_ID));

        verify(userRoleDao, times(1)).save(userRole);
    }

}
