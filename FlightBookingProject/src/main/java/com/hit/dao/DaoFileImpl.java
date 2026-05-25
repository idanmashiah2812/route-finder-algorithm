package com.hit.dao;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

public class DaoFileImpl<T> implements IDao<T> {

    private final String filePath;
    private final Map<String, T> memoryCache;

    public DaoFileImpl(String filePath) {
        this.filePath = filePath;
        this.memoryCache = new HashMap<>();
        loadFromFile();
    }

    @Override
    public void save(T entity) {
        try {
            Method getIdMethod = entity.getClass().getMethod("getId");
            String id = (String) getIdMethod.invoke(entity);
            memoryCache.put(id, entity);
            saveToFile();
        } catch (Exception e) {
            throw new RuntimeException("Error saving entity: " + e.getMessage(), e);
        }
    }

    @Override
    public T find(String id) {
        return memoryCache.get(id);
    }

    @Override
    public void delete(String id) {
        memoryCache.remove(id);
        saveToFile();
    }

    private void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            Map<String, T> loadedMap = (Map<String, T>) ois.readObject();
            if (loadedMap != null) {
                memoryCache.putAll(loadedMap);
            }
        } catch (IOException | ClassNotFoundException e) {
            // If file is empty or corrupted, start with empty cache
            memoryCache.clear();
        }
    }

    private void saveToFile() {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(memoryCache);
        } catch (IOException e) {
            throw new RuntimeException("Error saving to file: " + e.getMessage(), e);
        }
    }
}
