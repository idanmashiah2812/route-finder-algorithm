package com.hit.dao;

import java.util.List;

public interface IDao<T> {
    void save(T entity);
    T find(String id);
    void delete(String id);
    List<T> findAll();
}
