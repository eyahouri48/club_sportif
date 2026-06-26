package com.clubsportif.dao;

import java.util.List;
import java.util.Optional;

/**
 * Interface générique pour les opérations CRUD.
 * Toutes les DAO spécifiques héritent de cette interface.
 *
 * @param <T> le type de l'entité
 */
public interface GenericDAO<T> {

    void save(T entity);

    void update(T entity);

    void delete(String id);

    Optional<T> findById(String id);

    List<T> findAll();
}
