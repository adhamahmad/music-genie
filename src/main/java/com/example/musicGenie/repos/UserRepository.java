package com.example.musicGenie.repos;

import java.util.Optional;

import com.example.musicGenie.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("select u.email from User u where u.id = :id")
    Optional<String> findEmailById(@Param("id") Long id);
}
