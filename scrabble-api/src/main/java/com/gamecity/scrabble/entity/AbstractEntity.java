package com.gamecity.scrabble.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base entity class for entities
 * 
 * @author ekarakus
 */
@Data
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected Long id;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false)
    protected LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_updated_date", nullable = false)
    protected LocalDateTime lastUpdatedDate;

}
