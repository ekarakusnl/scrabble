package com.gamecity.scrabble.dao.impl;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.apache.commons.lang3.tuple.Pair;

import com.gamecity.scrabble.dao.BaseDao;
import com.gamecity.scrabble.entity.AbstractEntity;

/**
 * Base DAO implementation for operations based on hibernate entities
 * 
 * @author ekarakus
 * @param <T> Type of the entity
 */
@SuppressWarnings("unchecked")
abstract class AbstractDaoImpl<T extends AbstractEntity> implements BaseDao<T> {

    @PersistenceContext
    protected EntityManager entityManager;

    private final Class<T> clazz;

    public AbstractDaoImpl() {
        final ParameterizedType type = ((ParameterizedType) this.getClass().getGenericSuperclass());
        this.clazz = (Class<T>) type.getActualTypeArguments()[0];
    }

    @Override
    public T get(Long id) {
        return entityManager.find(clazz, id);
    }

    @Override
    public T getAndLock(Long id) {
        return entityManager.find(clazz, id, LockModeType.PESSIMISTIC_READ);
    }

    @Override
    public T save(T entity) {
        final LocalDateTime now = LocalDateTime.now();
        entity.setCreatedDate(entity.getId() == null ? now : entity.getCreatedDate());
        entity.setLastUpdatedDate(now);
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public List<T> saveAll(List<T> entities) {
        final LocalDateTime now = LocalDateTime.now();
        entities.forEach(entity -> {
            entity.setCreatedDate(entity.getId() == null ? now : entity.getCreatedDate());
            entity.setLastUpdatedDate(now);
        });
        entities.forEach(entityManager::persist);
        return entities;
    }

    @Override
    public List<T> list() {
        final String queryString = "SELECT e FROM " + (clazz.getAnnotation(Entity.class)).name() + " e ORDER BY id ASC";
        return entityManager.createQuery(queryString).getResultList();
    }

    protected List<T> listByNamedQuery(String querySql, List<Pair<String, Object>> paramPairs) {
        return listByNamedQuery(querySql, paramPairs, null);
    }

    protected List<T> listByNamedQuery(String querySql, List<Pair<String, Object>> paramPairs, Integer maxResults) {
        final Query query = createQueryWithParams(querySql, paramPairs);
        if (maxResults != null) {
            query.setMaxResults(maxResults);
        }
        return query.getResultList();
    }

    protected <G> List<G> listGenericByNamedQuery(String querySql, List<Pair<String, Object>> paramPairs) {
        final Query query = createQueryWithParams(querySql, paramPairs);
        return query.getResultList();
    }

    @SuppressWarnings("unused")
    protected T getByNamedQuery(String querySql, List<Pair<String, Object>> paramPairs) {
        try {
            final Query query = createQueryWithParams(querySql, paramPairs);
            query.setMaxResults(1);
            return (T) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Query createQueryWithParams(String querySql, List<Pair<String, Object>> paramPairs) {
        final Query query = entityManager.createNamedQuery(querySql);
        paramPairs.stream().forEach(pair -> {
            query.setParameter(pair.getKey(), pair.getValue());
        });
        return query;
    }

}
