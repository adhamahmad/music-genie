package com.example.musicGenie.repos;

import com.example.musicGenie.models.UserProvider;
import com.example.musicGenie.models.UserProviderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProviderRepository extends JpaRepository<UserProvider, UserProviderId> {

    Optional<UserProvider> findByIdUserIdAndIdProviderId(Long userId, Long providerId);

    Optional<UserProvider> findByProviderUserId(String providerUserId);

//    Optional<String> findRefreshTokenByIdUserIdAndIdProviderId(Long userId, Long providerId);

@Query("SELECT up.refreshToken FROM UserProvider up WHERE up.id.userId = :userId AND up.id.providerId = :providerId")
Optional<String> findRefreshTokenByIdUserIdAndIdProviderId(@Param("userId") Long userId, @Param("providerId") Long providerId);

    @Modifying
    @Query("update UserProvider up set up.refreshToken = :refreshToken " +
            "where up.id.userId = :userId and up.id.providerId = :providerId")
    void updateRefreshToken(@Param("userId") Long userId,
                            @Param("providerId") Long providerId,
                            @Param("refreshToken") String refreshToken);

    @Modifying
    @Query("update UserProvider up set up.refreshToken = null " +
            "where up.id.userId = :userId and up.id.providerId = :providerId")
    void deleteRefreshToken(@Param("userId") Long userId,
                            @Param("providerId") Long providerId);



}
