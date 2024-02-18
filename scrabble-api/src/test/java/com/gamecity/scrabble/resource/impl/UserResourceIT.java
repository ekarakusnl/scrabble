package com.gamecity.scrabble.resource.impl;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import com.gamecity.scrabble.model.rest.ExceptionDto;
import com.gamecity.scrabble.model.rest.UserDto;
import com.gamecity.scrabble.util.JsonUtils;
import com.google.common.io.Resources;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class UserResourceIT extends AbstractIntegrationTest {

    @Test
    void test_get_user() {
        final Response response = target("/users/by/user").request().get();

        if (Status.OK.getStatusCode() != response.getStatus()) {
            assertThat(response.readEntity(String.class), response.getStatus(), equalTo(Status.OK.getStatusCode()));
        }

        assertThat(response.getHeaderString(HttpHeaders.ETAG), notNullValue());

        final UserDto responseDto = response.readEntity(UserDto.class);

        assertThat(responseDto.getUsername(), equalTo("user"));
        assertThat(responseDto.getId(), notNullValue());

        response.close();
    }

    @Test
    void test_create_user() throws IOException {
        final URL resource = UserResourceIT.class.getResource("/json/user.json");

        final UserDto sampleUserDto = JsonUtils.toDto(Resources.toString(resource, StandardCharsets.UTF_8),
                UserDto.class);

        final Response response = target("/users").request()
                .put(Entity.entity(sampleUserDto, MediaType.APPLICATION_JSON));

        if (Status.OK.getStatusCode() != response.getStatus()) {
            assertThat(response.readEntity(String.class), response.getStatus(), equalTo(Status.OK.getStatusCode()));
        }

        assertThat(response.getHeaderString(HttpHeaders.ETAG), notNullValue());

        final UserDto responseDto = response.readEntity(UserDto.class);

        assertThat(responseDto.getUsername(), equalTo("friedrich"));
        assertThat(responseDto.getId(), notNullValue());

        response.close();
    }

    @Test
    void test_update_user_password() {
        final Response response = target("/users/by/user").request().get();

        final String etag = response.getHeaderString(HttpHeaders.ETAG);

        final UserDto responseDto = response.readEntity(UserDto.class);

        final String actualPassword = responseDto.getPassword();

        final String newPassword = "Test!123";
        responseDto.setPassword(newPassword);

        final Response updatedResponse = target("/users/1").request()
                .header(HttpHeaders.IF_MATCH, etag)
                .put(Entity.entity(responseDto, MediaType.APPLICATION_JSON));

        if (Status.OK.getStatusCode() != updatedResponse.getStatus()) {
            assertThat(updatedResponse.readEntity(String.class), updatedResponse.getStatus(),
                    equalTo(Status.OK.getStatusCode()));
        }

        assertThat(updatedResponse.getHeaderString(HttpHeaders.ETAG), notNullValue());

        final UserDto updatedResponseDto = updatedResponse.readEntity(UserDto.class);

        assertThat(updatedResponseDto.getPassword(), not(equalTo(actualPassword)));
        assertThat(updatedResponseDto.getPassword(), not(equalTo(newPassword)));

        updatedResponse.close();
        response.close();
    }

    @Test
    void test_update_user_fails_without_etag() {
        final Response response = target("/users/by/user").request().get();

        final UserDto responseDto = response.readEntity(UserDto.class);
        responseDto.setPassword("$2a$10$kL2cVFyQ9FIRm390RG8JienR/nJVTK8g6Lb0FH0K5Y4AEsE1zZLVz");

        final Response updatedResponse = target("/users/1").request()
                .put(Entity.entity(responseDto, MediaType.APPLICATION_JSON));

        assertThat(updatedResponse.readEntity(String.class), equalTo("If-Match header is missing"));

        updatedResponse.close();
        response.close();
    }

    @Test
    void test_user_does_not_exist() {
        final Response response = target("/users/by/null").request().get();

        assertThat(response.getStatus(), equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));

        final ExceptionDto responseDto = response.readEntity(ExceptionDto.class);

        assertThat(responseDto.getMessage(), equalTo("User is not found!"));

        response.close();
    }

}
