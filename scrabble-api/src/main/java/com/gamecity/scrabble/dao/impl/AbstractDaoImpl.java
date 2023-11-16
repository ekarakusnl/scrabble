package com.gamecity.scrabble.dao.impl;

import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

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
        final Date now = new Date();
        entity.setCreatedDate(entity.getId() == null ? now : entity.getCreatedDate());
        entity.setLastUpdatedDate(now);
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public List<T> saveAll(List<T> entities) {
        final Date now = new Date();
        entities.forEach(entity -> {
            entity.setCreatedDate(entity.getId() == null ? now : entity.getCreatedDate());
            entity.setLastUpdatedDate(now);
        });
        entities.forEach(entityManager::persist);
        return entities;
    }

    @Override
    public void delete(Long id) {
        final T entity = get(id);
        entityManager.remove(entity);
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
        if (maxResults != null && maxResults > 0) {
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
        if (!CollectionUtils.isEmpty(paramPairs)) {
            paramPairs.stream().forEach(pair -> {
                query.setParameter(pair.getKey(), pair.getValue());
            });
        }
        return query;
    }

    @SuppressWarnings("unused")
    protected <G> G findGenericTypeByNamedQuery(String querySql, List<Pair<String, Object>> paramPairs) {
        try {
            final Query query = createQueryWithParams(querySql, paramPairs);
            query.setMaxResults(1);
            return (G) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
