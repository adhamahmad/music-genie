package com.example.musicGenie.services.provider;

import com.example.musicGenie.models.Provider;
import com.example.musicGenie.repos.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderService {

    private final ProviderRepository providerRepository;

    @Transactional(readOnly = true)
    public Provider getByName(String name) {
        return providerRepository.findByName(name)
                                 .orElseThrow(() -> new IllegalStateException("Provider not found: " + name));
    }

    @Transactional(readOnly = true)
    public boolean exists(String name) {
        return providerRepository.findByName(name).isPresent();
    }

    @Transactional(readOnly = true)
    public Long getIdByName(String name) {
        return providerRepository.findIdByName(name)
                                 .orElseThrow(() -> new IllegalStateException("Provider not found: " + name));
    }
}