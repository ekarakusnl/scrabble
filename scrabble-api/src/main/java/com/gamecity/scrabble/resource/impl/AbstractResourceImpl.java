package com.gamecity.scrabble.resource.impl;

import java.time.format.DateTimeFormatter;

import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.gamecity.scrabble.entity.AbstractEntity;
import com.gamecity.scrabble.model.Mapper;
import com.gamecity.scrabble.model.rest.AbstractDto;
import com.gamecity.scrabble.resource.BaseResource;
import com.gamecity.scrabble.service.BaseService;

@SuppressWarnings("unchecked")
abstract class AbstractResourceImpl<T extends AbstractEntity, D extends AbstractDto, S extends BaseService<T>>
        implements BaseResource<D> {

    protected S baseService;

    @Autowired
    void setBaseService(S baseService) {
        this.baseService = baseService;
    }

    @Override
    public Response get(Long id) {
        final T entity = baseService.get(id);
        final D dto = (D) Mapper.toDto(entity);
        return Response.ok(dto).tag(createETag(dto)).build();
    }

    @Override
    public Response create(D dto) {
        final T entity = baseService.save((T) Mapper.toEntity(dto));
        final D responseDto = (D) Mapper.toDto(entity);
        return Response.ok(responseDto).tag(createETag(responseDto)).build();
    }

    @Override
    public Response update(Long id, D dto, String ifMatch, Request request) {
        Assert.notNull(dto.getId(), "id cannot be null");

        // TODO add a test
        if (!dto.getId().equals(id)) {
            throw new IllegalStateException("id values are not the same");
        }

        if (StringUtils.isEmpty(ifMatch)) {
            throw new IllegalStateException("If-Match header is missing");
        }

        final D existingEntityDto = (D) Mapper.toDto(baseService.get(id));
        final ResponseBuilder failedETagValidationResponse = request
                .evaluatePreconditions(createETag(existingEntityDto));
        // TODO add a test
        if (failedETagValidationResponse != null) {
            return failedETagValidationResponse.build();
        }

        final T entity = baseService.save((T) Mapper.toEntity(dto));
        final D responseDto = (D) Mapper.toDto(entity);
        return Response.ok(responseDto).tag(createETag(responseDto)).build();
    }

    protected EntityTag createETag(D dto) {
        final String lastUpdatedDate = DateTimeFormatter.ISO_DATE_TIME.format(dto.getLastUpdatedDate());
        final String eTag = DigestUtils.sha256Hex(lastUpdatedDate);
        return new EntityTag(eTag);
    }

}
