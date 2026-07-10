package com.hit.dao;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DaoFileImpl<T> implements IDao<T> {

    private final String filePath;
    private final Map<String, T> memoryCache;
    private final ReentrantReadWriteLock fileLock;

    public DaoFileImpl(String filePath) {
        this.filePath = filePath;
        this.memoryCache = new ConcurrentHashMap<>();
        this.fileLock = new ReentrantReadWriteLock();
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
    public List<T> findAll() {
        return new ArrayList<>(memoryCache.values());
    }

    @Override
    public void delete(String id) {
        memoryCache.remove(id);
        saveToFile();
    }

    private void loadFromFile() {
        fileLock.readLock().lock();
        try {
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
        } finally {
            fileLock.readLock().unlock();
        }
    }

    private void saveToFile() {
        fileLock.writeLock().lock();
        try {
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(new HashMap<>(memoryCache));
            } catch (IOException e) {
                throw new RuntimeException("Error saving to file: " + e.getMessage(), e);
            }
        } finally {
            fileLock.writeLock().unlock();
        }
    }
}
