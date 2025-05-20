package com.cache.cacheservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MY_ENTITY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Unique id can be the database id here, or any other unique field.
    // getId() will serve as getId() from requirements.
}
