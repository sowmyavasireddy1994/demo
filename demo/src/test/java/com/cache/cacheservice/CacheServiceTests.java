package com.cache.cacheservice;

import com.cache.cacheservice.entity.MyEntity;
import com.cache.cacheservice.repository.MyEntityRepository;
import com.cache.cacheservice.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CacheServiceTests {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private MyEntityRepository repository;

    @BeforeEach
    void setUp() {
        cacheService.removeAll();
    }

    @Test
    void testAddAndGet() {
        MyEntity e1 = new MyEntity(null, "Test1");
        cacheService.add(e1);
        assertNotNull(e1.getId(), "ID should be assigned after add");

        MyEntity fromCache = cacheService.get(e1);
        assertEquals("Test1", fromCache.getName());

        // Check that it's in DB as well
        assertTrue(repository.findById(e1.getId()).isPresent());
    }

    @Test
    void testRemove() {
        MyEntity e1 = new MyEntity(null, "TestRemove");
        cacheService.add(e1);
        Long id = e1.getId();
        cacheService.remove(e1);

        assertFalse(repository.findById(id).isPresent());
        // Attempting to get should fail
        assertThrows(RuntimeException.class, () -> cacheService.get(e1));
    }

    @Test
    void testClearCache() {
        MyEntity e1 = new MyEntity(null, "CachedEntity");
        cacheService.add(e1);
        cacheService.clear();

        // Cache cleared. Fetching entity should retrieve from DB and re-cache it.
        MyEntity fromCache = cacheService.get(e1);
        assertEquals("CachedEntity", fromCache.getName());
    }

    @Test
    void testEviction() {
        // maxSize default is 5
        for (int i = 1; i <= 6; i++) {
            MyEntity e = new MyEntity(null, "Entity" + i);
            cacheService.add(e);
        }
        // After adding 6 entities, one should be evicted to DB
        // The first inserted entity should now only be in DB, not in cache.

        // Let's try to get the first entity
        MyEntity first = new MyEntity(1L, null);
        MyEntity fetched = cacheService.get(first);
        assertEquals("Entity1", fetched.getName());
    }
}
