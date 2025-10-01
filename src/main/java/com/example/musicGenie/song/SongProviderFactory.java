package com.example.musicGenie.song;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class SongProviderFactory {
    private final Map<String, SongProvider> providers;

    public SongProviderFactory(Map<String, SongProvider> providers) {
        this.providers = providers;
    }

    public SongProvider getProvider(String providerName) {
        SongProvider provider = providers.get(providerName + "SongProvider");
        if (provider == null) {
            throw new IllegalArgumentException("No song provider found for: " + providerName);
        }
        return provider;
    }
}
