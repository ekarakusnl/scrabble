package com.gamecity.scrabble.resource.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.gamecity.scrabble.entity.AbstractEntity;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.rest.AbstractDto;
import com.gamecity.scrabble.resource.BaseResource;
import com.gamecity.scrabble.service.BaseService;

@SuppressWarnings("unchecked")
abstract class AbstractResourceImpl<T extends AbstractEntity, D extends AbstractDto, S extends BaseService<T>>
        implements BaseResource<D> {

    abstract S getBaseService();

    public Response get(Long id) {
        final T entity = getBaseService().get(id);
        final D dto = (D) Mapper.toDto(entity);
        return Response.ok(dto).tag(createETag(dto)).build();
    }

    public Response create(D dto) {
        final T entity = getBaseService().save((T) Mapper.toEntity(dto));
        final D responseDto = (D) Mapper.toDto(entity);
        return Response.ok(responseDto).tag(createETag(responseDto)).build();
    }

    public Response update(Long id, D dto, String ifMatch, Request request) {
        Assert.notNull(dto.getId(), "id cannot be null");

        if (!dto.getId().equals(id)) {
            throw new IllegalStateException("id values are not the same");
        }

        if (StringUtils.isEmpty(ifMatch)) {
            throw new IllegalStateException("If-Match header is missing");
        }

        final D existingEntityDto = (D) Mapper.toDto(getBaseService().get(id));
        final ResponseBuilder failedETagValidationResponse =
                request.evaluatePreconditions(createETag(existingEntityDto));
        if (failedETagValidationResponse != null) {
            return failedETagValidationResponse.build();
        }

        final T entity = getBaseService().save((T) Mapper.toEntity(dto));
        final D responseDto = (D) Mapper.toDto(entity);
        return Response.ok(responseDto).tag(createETag(responseDto)).build();
    }

    public Response delete(Long id) {
        getBaseService().delete(id);
        return Response.ok().build();
    }

    public Response list() {
        final List<T> list = getBaseService().list();
        return Response.ok(list.stream().map(Mapper::toDto).collect(Collectors.toList())).build();
    }

    protected EntityTag createETag(D dto) {
        final String lastUpdatedDate = DateTimeFormatter.ISO_DATE_TIME
                .format(LocalDateTime.ofInstant(dto.getLastUpdatedDate().toInstant(), ZoneId.systemDefault()));
        final String eTag = DigestUtils.sha256Hex(lastUpdatedDate);
        return new EntityTag(eTag);
    }

}
