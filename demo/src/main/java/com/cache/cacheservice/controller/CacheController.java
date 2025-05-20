package com.cache.cacheservice.controller;


import com.cache.cacheservice.dto.MyEntityDTO;
import com.cache.cacheservice.entity.MyEntity;
import com.cache.cacheservice.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller to demonstrate the caching operations.
 */
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private CacheService cacheService;

    @PostMapping("/add")
    public MyEntityDTO add(@RequestBody MyEntityDTO dto) {
        MyEntity entity = new MyEntity(dto.getId(), dto.getName());
        cacheService.add(entity);
        return dto;
    }

    @DeleteMapping("/remove")
    public void remove(@RequestBody MyEntityDTO dto) {
        MyEntity entity = new MyEntity(dto.getId(), dto.getName());
        cacheService.remove(entity);
    }

    @DeleteMapping("/removeAll")
    public void removeAll() {
        cacheService.removeAll();
    }

    @GetMapping("/get/{id}")
    public MyEntityDTO get(@PathVariable Long id) {
        MyEntity entity = new MyEntity(id, null);
        MyEntity result = cacheService.get(entity);
        MyEntityDTO dto = new MyEntityDTO();
        dto.setId(result.getId());
        dto.setName(result.getName());
        return dto;
    }

    @PostMapping("/clear")
    public void clear() {
        cacheService.clear();
    }
}
