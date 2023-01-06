package org.example.app.repositories;

import org.example.app.models.User;

import java.util.List;

public interface Repository<T> {
    void insert(T t);
    void update(T t);
}
