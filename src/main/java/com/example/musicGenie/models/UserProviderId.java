package com.example.musicGenie.models;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProviderId implements Serializable {
    private Long userId;
    private Long providerId;

    // equals/hashCode for composite key
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProviderId)) return false;
        UserProviderId that = (UserProviderId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(providerId, that.providerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, providerId);
    }
}