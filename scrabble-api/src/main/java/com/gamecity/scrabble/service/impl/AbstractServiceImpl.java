package com.gamecity.scrabble.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.gamecity.scrabble.dao.BaseDao;
import com.gamecity.scrabble.entity.AbstractEntity;
import com.gamecity.scrabble.service.BaseService;

abstract class AbstractServiceImpl<T extends AbstractEntity, D extends BaseDao<T>> implements BaseService<T> {

    protected D baseDao;

    @Autowired
    void setBaseDao(D baseDao) {
        this.baseDao = baseDao;
    }

    @Override
    public T get(Long id) {
        return baseDao.get(id);
    }

    @Override
    @Transactional
    public T save(T entity) {
        return baseDao.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        baseDao.delete(id);
    }

    @Override
    public List<T> list() {
        return baseDao.list();
    }

}
