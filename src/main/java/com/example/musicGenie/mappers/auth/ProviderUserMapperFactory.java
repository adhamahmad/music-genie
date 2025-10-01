package com.example.musicGenie.mappers.auth;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProviderUserMapperFactory {

    private final Map<String, ProviderUserMapper> mappers;

    public ProviderUserMapperFactory(Map<String, ProviderUserMapper> mappers) {
        this.mappers = mappers;
    }

    public ProviderUserMapper getMapper(String providerName) {
        ProviderUserMapper mapper = mappers.get(providerName + "Mapper");
        if (mapper == null) {
            throw new IllegalArgumentException("No mapper found for provider: " + providerName);
        }
        return mapper;
    }
}
