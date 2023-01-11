package com.gamecity.scrabble.service.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.gamecity.scrabble.model.rest.AbstractDto;
import com.gamecity.scrabble.model.rest.ExceptionDto;
import com.gamecity.scrabble.rest.exception.GenericException;
import com.gamecity.scrabble.service.RestService;
import com.gamecity.scrabble.util.JsonUtils;
import com.google.common.net.HttpHeaders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@SuppressWarnings("unchecked")
class RestServiceImpl implements RestService {

    @Value("${api.resource.endpoint}")
    private String apiEndpoint;

    @Override
    public <T extends AbstractDto> T get(String resource, Class<T> clazz, Object... params) {
        final ServiceCall serviceCall = new ServiceCall(resource, HttpMethod.GET, null, params);
        final String payload = doCall(serviceCall);
        return JsonUtils.toDto(payload, clazz);
    }

    @Override
    public <T extends AbstractDto> List<T> list(String resource, Class<T> clazz, Object... params) {
        final ServiceCall serviceCall = new ServiceCall(resource, HttpMethod.GET, null, params);
        final String payload = doCall(serviceCall);
        return JsonUtils.toList(payload, clazz);
    }

    @Override
    public <T extends AbstractDto> T post(String resource, Class<T> clazz, Object entity, Object... params) {
        final ServiceCall serviceCall = new ServiceCall(resource, HttpMethod.POST, entity, params);
        final String payload = doCall(serviceCall);
        return JsonUtils.toDto(payload, clazz);
    }

    @Override
    public <T extends AbstractDto> T put(String resource, Class<T> clazz, T entity, Object... params) {
        final ServiceCall serviceCall = new ServiceCall(resource, HttpMethod.PUT, entity, params);
        final String payload = doCall(serviceCall);
        return JsonUtils.toDto(payload, clazz);
    }

    @Override
    public <T extends AbstractDto> void delete(String resource, Class<T> clazz, Object... params) {
        final ServiceCall serviceCall = new ServiceCall(resource, HttpMethod.DELETE, null, params);
        doCall(serviceCall);
    }

    // ---------------------------------------------------- private methods
    // ----------------------------------------------------

    @Data
    @AllArgsConstructor
    class ServiceCall {

        private String resource;
        private HttpMethod method;
        private Object postObject;
        private Object[] params;

    }

    class ResourceURLCreator {

        private final StringBuilder resourceURL = new StringBuilder();
        private String resource;
        private Object[] params;

        public ResourceURLCreator(String resource, Object... params) {
            this.resource = resource;
            this.params = params;
        }

        public String createURL() {
            createResourceURL().addRequestParams();
            return resourceURL.toString();
        }

        private ResourceURLCreator createResourceURL() {
            resourceURL.append(apiEndpoint).append(resource).toString();
            return this;
        }

        private ResourceURLCreator addRequestParams() {
            if (params != null && params.length > 0) {
                final IntConsumer consumer = i -> resourceURL.replace(resourceURL.indexOf("{"),
                        resourceURL.indexOf("}") + 1, String.valueOf(params[i]));
                IntStream.range(0, params.length).forEach(consumer);
            }
            return this;
        }
    }

    private <T extends AbstractDto> String doCall(ServiceCall serviceCall) {
        try {
            final HttpURLConnection httpURLConnection =
                    createConnection(serviceCall.getResource(), serviceCall.getParams());

            httpURLConnection.setRequestMethod(serviceCall.getMethod().name());
            httpURLConnection.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            httpURLConnection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            if (HttpMethod.POST == serviceCall.getMethod() || HttpMethod.PUT == serviceCall.getMethod()) {
                httpURLConnection.setDoOutput(true);

                if (serviceCall.getPostObject() != null) {
                    final String payload;
                    if (serviceCall.getPostObject() instanceof List) {
                        payload = JsonUtils.toJson((List<T>) serviceCall.getPostObject());
                    } else {
                        payload = JsonUtils.toJson((T) serviceCall.getPostObject());
                    }
                    httpURLConnection.setRequestProperty(HttpHeaders.CONTENT_LENGTH,
                            "" + payload.getBytes(StandardCharsets.UTF_8.displayName()).length);
                    final DataOutputStream daos = new DataOutputStream(httpURLConnection.getOutputStream());
                    daos.write(payload.getBytes(StandardCharsets.UTF_8.displayName()));
                    daos.flush();
                    daos.close();
                }
            }

            return buildResponse(httpURLConnection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpURLConnection createConnection(String resource, Object... params) {
        final ResourceURLCreator resourceURLCreator = new ResourceURLCreator(resource, params);
        final String resourceURL = resourceURLCreator.createURL();

        log.debug("Calling the resource url {}", resourceURL);

        try {
            final URL url = new URL(resourceURL);
            return (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildResponse(HttpURLConnection httpURLConnection) {
        try {
            final int responseCode = httpURLConnection.getResponseCode();
            if (responseCode != 200) {
                final String payload = buildErrorMessage(httpURLConnection);
                closeHttpConnection(httpURLConnection);

                if (responseCode == 500) {
                    final ExceptionDto exceptionDto = JsonUtils.toDto(payload, ExceptionDto.class);
                    throw new GenericException(exceptionDto);
                }

                closeHttpConnection(httpURLConnection);
                throw new GenericException(payload);
            }

            final BufferedReader br = new BufferedReader(new InputStreamReader((httpURLConnection.getInputStream())));

            String output;
            final StringBuilder payload = new StringBuilder();
            while ((output = br.readLine()) != null) {
                payload.append(output);
            }
            closeHttpConnection(httpURLConnection);
            return payload.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeHttpConnection(HttpURLConnection httpURLConnection) {
        httpURLConnection.disconnect();
    }

    private String buildErrorMessage(HttpURLConnection httpURLConnection) {
        InputStream stream = null;
        try {
            stream = httpURLConnection.getErrorStream();
            if (stream == null) {
                stream = httpURLConnection.getInputStream();
            }
            String response;
            try (Scanner scanner = new Scanner(stream)) {
                scanner.useDelimiter("\\Z");
                response = scanner.next();
            }
            closeStream(stream);
            return response;
        } catch (IOException e) {
            closeStream(stream);
            closeHttpConnection(httpURLConnection);
            throw new RuntimeException(e);
        }
    }

    private void closeStream(InputStream stream) {
        if (stream == null) {
            return;
        }

        try {
            stream.close();
        } catch (IOException e) {
            log.error("An error occured while closing the stream", e);
        }
    }

}
