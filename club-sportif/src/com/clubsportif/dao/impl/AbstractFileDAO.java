package com.clubsportif.dao.impl;

import com.clubsportif.dao.GenericDAO;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Implémentation générique de persistance par sérialisation dans un fichier binaire.
 */
public abstract class AbstractFileDAO<T> implements GenericDAO<T> {

    private final Path filePath;

    protected AbstractFileDAO(String filename) {
        Path dataDir = Paths.get("data");
        try { Files.createDirectories(dataDir); } catch (IOException ignored) {}
        this.filePath = dataDir.resolve(filename);
    }

    protected abstract String getId(T entity);

    @SuppressWarnings("unchecked")
    protected synchronized Map<String, T> loadAll() {
        if (!Files.exists(filePath)) return new LinkedHashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
            return (Map<String, T>) ois.readObject();
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    protected synchronized void saveAll(Map<String, T> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("Erreur sauvegarde : " + e.getMessage(), e);
        }
    }

    @Override
    public void save(T entity) {
        Map<String, T> data = loadAll();
        data.put(getId(entity), entity);
        saveAll(data);
    }

    @Override
    public void update(T entity) { save(entity); }

    @Override
    public void delete(String id) {
        Map<String, T> data = loadAll();
        data.remove(id);
        saveAll(data);
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(loadAll().get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(loadAll().values());
    }
}
