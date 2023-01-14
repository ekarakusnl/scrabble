package com.gamecity.scrabble.test.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import com.gamecity.scrabble.model.rest.UserDto;
import com.gamecity.scrabble.util.JsonUtils;
import com.google.common.io.Resources;

class UserResourceIT extends AbstractIntegrationTest {

    @Test
    void test_get_user() {
        final Response response = target("/users/by/Edi").request().get();

        if (Status.OK.getStatusCode() != response.getStatus()) {
            assertEquals(Status.OK.getStatusCode(), response.getStatus(), response.readEntity(String.class));
        }
        assertNotNull(response.getHeaderString(HttpHeaders.ETAG));

        final UserDto responseDto = response.readEntity(UserDto.class);

        assertEquals("Edi", responseDto.getUsername());
        assertNotNull(responseDto.getId());

        response.close();
    }

    @Test
    void test_create_user() throws IOException {
        final URL resource = UserResourceIT.class.getResource("/json/user.json");
        final UserDto sampleUserDto =
                JsonUtils.toDto(Resources.toString(resource, StandardCharsets.UTF_8), UserDto.class);

        final Response response =
                target("/users").request().put(Entity.entity(sampleUserDto, MediaType.APPLICATION_JSON));

        if (Status.OK.getStatusCode() != response.getStatus()) {
            assertEquals(Status.OK.getStatusCode(), response.getStatus(), response.readEntity(String.class));
        }
        assertNotNull(response.getHeaderString(HttpHeaders.ETAG));

        final UserDto responseDto = response.readEntity(UserDto.class);

        assertEquals("mukawwaa", responseDto.getUsername());
        assertNotNull(responseDto.getId());

        response.close();
    }

    @Test
    void test_update_user() {
        final Response response = target("/users/by/Edi").request().get();

        final String etag = response.getHeaderString(HttpHeaders.ETAG);
        final UserDto responseDto = response.readEntity(UserDto.class);
        responseDto.setPassword("$2a$10$kL2cVFyQ9FIRm390RG8JienR/nJVTK8g6Lb0FH0K5Y4AEsE1zZLVz");

        final Response updatedResponse = target("/users/1").request()
                .header(HttpHeaders.IF_MATCH, etag)
                .put(Entity.entity(responseDto, MediaType.APPLICATION_JSON));

        if (Status.OK.getStatusCode() != updatedResponse.getStatus()) {
            assertEquals(Status.OK.getStatusCode(), updatedResponse.getStatus(),
                    updatedResponse.readEntity(String.class));
        }
        assertNotNull(updatedResponse.getHeaderString(HttpHeaders.ETAG));

        final UserDto updatedResponseDto = updatedResponse.readEntity(UserDto.class);

        assertEquals("$2a$10$kL2cVFyQ9FIRm390RG8JienR/nJVTK8g6Lb0FH0K5Y4AEsE1zZLVz", updatedResponseDto.getPassword());

        updatedResponse.close();
        response.close();
    }

    @Test
    void test_update_user_without_etag() {
        final Response response = target("/users/by/Edi").request().get();

        final UserDto responseDto = response.readEntity(UserDto.class);
        responseDto.setPassword("$2a$10$kL2cVFyQ9FIRm390RG8JienR/nJVTK8g6Lb0FH0K5Y4AEsE1zZLVz");

        final Response updatedResponse =
                target("/users/1").request().put(Entity.entity(responseDto, MediaType.APPLICATION_JSON));

        assertEquals("If-Match header is missing", updatedResponse.readEntity(String.class));

        updatedResponse.close();
        response.close();
    }

}
