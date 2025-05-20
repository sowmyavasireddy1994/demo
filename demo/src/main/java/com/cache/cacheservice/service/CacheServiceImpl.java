package com.cache.cacheservice.service;


import com.cache.cacheservice.entity.MyEntity;
import com.cache.cacheservice.exception.ResourceNotFoundException;
import com.cache.cacheservice.repository.MyEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This service maintains an LRU cache using LinkedHashMap.
 * On capacity overflow, the least-used element is moved to the database.
 */
@Service
public class CacheServiceImpl implements CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheServiceImpl.class);

    // Max size configurable
    private final int maxSize;

    private final MyEntityRepository repository;

    private final Map<Long, MyEntity> cache;

    public CacheServiceImpl(
            MyEntityRepository repository,
            @Value("${cache.maxSize:5}") int maxSize
    ) {
        this.repository = repository;
        this.maxSize = maxSize;

        // LinkedHashMap with accessOrder = true to maintain LRU order
        this.cache = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, MyEntity> eldest) {
                if (size() > maxSize) {
                    // Move the eldest to DB
                    MyEntity entityToEvict = eldest.getValue();
                    logger.info("Evicting entity with id {} to database", entityToEvict.getId());
                    // Since we are evicting from cache, we ensure it is persisted in DB
                    // The add operation always ensures entity is saved in cache.
                    // On eviction, we must ensure it still exists in DB:
                    if (entityToEvict.getId() != null) {
                        // entity has ID means it's known in DB.
                        // If not saved, let's save it now.
                        repository.save(entityToEvict);
                    }
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void add(MyEntity e1) {
        if (e1.getId() == null) {
            // If no ID, save to DB first to generate an ID
            MyEntity saved = repository.save(e1);
            cache.put(saved.getId(), saved);
            logger.info("Entity with id {} added to cache and DB", saved.getId());
        } else {
            // If entity already has an ID, just put into cache:
            // But also ensure DB is up-to-date
            repository.save(e1);
            cache.put(e1.getId(), e1);
            logger.info("Entity with id {} updated in DB and added to cache", e1.getId());
        }
    }

    @Override
    public void remove(MyEntity e1) {
        if (e1.getId() == null) {
            logger.warn("Attempted to remove entity without id");
            return;
        }
        cache.remove(e1.getId());
        // Also remove from DB
        if (repository.existsById(e1.getId())) {
            repository.deleteById(e1.getId());
            logger.info("Entity with id {} removed from cache and DB", e1.getId());
        }
    }

    @Override
    public void removeAll() {
        cache.clear();
        repository.deleteAll();
        logger.info("All entities removed from cache and DB");
    }

    @Override
    public MyEntity get(MyEntity e1) {
        if (e1.getId() == null) {
            logger.warn("Attempted to get entity without id");
            return null;
        }
        MyEntity found = cache.get(e1.getId());
        if (found != null) {
            logger.info("Entity with id {} found in cache", e1.getId());
            return found;
        } else {
            // Check DB
            Optional<MyEntity> fromDb = repository.findById(e1.getId());
            if (fromDb.isPresent()) {
                MyEntity entity = fromDb.get();
                cache.put(entity.getId(), entity);
                logger.info("Entity with id {} fetched from DB and added to cache", e1.getId());
                return entity;
            } else {
                throw new ResourceNotFoundException("Entity with id " + e1.getId() + " not found");
            }
        }
    }

    @Override
    public void clear() {
        cache.clear();
        logger.info("Cache cleared. DB untouched.");
    }
}

