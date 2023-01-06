package org.example.app.repositories;

public interface Repository<T> {
    void add(T t);
    void update(T t);
}
