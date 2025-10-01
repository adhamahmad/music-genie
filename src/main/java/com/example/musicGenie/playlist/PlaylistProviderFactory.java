package com.example.musicGenie.playlist;

import java.util.Map;

import com.example.musicGenie.mappers.auth.ProviderUserMapper;
import org.springframework.stereotype.Component;

@Component
public class PlaylistProviderFactory {
    private final Map<String, PlaylistProvider> providers;

    public PlaylistProviderFactory(Map<String, PlaylistProvider> providers) {
        this.providers = providers;
    }

    public PlaylistProvider getProvider(String providerName) {
        PlaylistProvider provider = providers.get(providerName + "PlaylistProvider");
        if (provider == null) {
            throw new IllegalArgumentException("No playlist provider found for: " + providerName);
        }
        return provider;
    }
}
