package com.hit.dao;

public interface IDao<T> {
    void save(T entity);
    T find(String id);
    void delete(String id);
}
