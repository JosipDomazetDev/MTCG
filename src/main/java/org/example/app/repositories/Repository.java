package org.example.app.repositories;

public interface Repository<T> {
    void insert(T t);
    void update(T t);
}
