package com.example.musicGenie.repos;

import com.example.musicGenie.models.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findByName(String name);
    @Query("SELECT p.id FROM Provider p WHERE p.name = :name")
    Optional<Long> findIdByName(String name);
}
