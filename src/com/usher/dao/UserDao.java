package com.usher.dao;

import java.util.List;

/**
 * @Author: Usher
 * @Description:
 */
public interface UserDao<T> {

    public List<T> findAll();
    public int save(T obj);

    public int update(T obj);

    public int delete(T obj);
}
