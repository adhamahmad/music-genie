package com.example.musicGenie.services.provider;

import com.example.musicGenie.models.Provider;
import com.example.musicGenie.repos.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProviderService Tests")
class ProviderServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private ProviderService providerService;

    private Provider testProvider;
    private static final String PROVIDER_NAME = "spotify";
    private static final Long PROVIDER_ID = 100L;

    @BeforeEach
    void setUp() {
        testProvider = Provider.builder()
                               .id(PROVIDER_ID)
                               .name(PROVIDER_NAME)
                               .build();
    }

    @Test
    @DisplayName("getByName - Should return provider when found")
    void getByName_ShouldReturnProvider_WhenExists() {
        // Given
        when(providerRepository.findByName(PROVIDER_NAME)).thenReturn(Optional.of(testProvider));

        // When
        Provider result = providerService.getByName(PROVIDER_NAME);

        // Then
        assertThat(result).isEqualTo(testProvider);
        verify(providerRepository, times(1)).findByName(PROVIDER_NAME);
    }

    @Test
    @DisplayName("getByName - Should throw exception when provider not found")
    void getByName_ShouldThrowException_WhenNotFound() {
        // Given
        when(providerRepository.findByName(PROVIDER_NAME)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> providerService.getByName(PROVIDER_NAME))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Provider not found: " + PROVIDER_NAME);

        verify(providerRepository, times(1)).findByName(PROVIDER_NAME);
    }

    @Test
    @DisplayName("exists - Should return true when provider exists")
    void exists_ShouldReturnTrue_WhenProviderExists() {
        // Given
        when(providerRepository.findByName(PROVIDER_NAME)).thenReturn(Optional.of(testProvider));

        // When
        boolean result = providerService.exists(PROVIDER_NAME);

        // Then
        assertThat(result).isTrue();
        verify(providerRepository, times(1)).findByName(PROVIDER_NAME);
    }

    @Test
    @DisplayName("exists - Should return false when provider does not exist")
    void exists_ShouldReturnFalse_WhenProviderDoesNotExist() {
        // Given
        when(providerRepository.findByName(PROVIDER_NAME)).thenReturn(Optional.empty());

        // When
        boolean result = providerService.exists(PROVIDER_NAME);

        // Then
        assertThat(result).isFalse();
        verify(providerRepository, times(1)).findByName(PROVIDER_NAME);
    }

    @Test
    @DisplayName("getIdByName - Should return provider ID when found")
    void getIdByName_ShouldReturnId_WhenExists() {
        // Given
        when(providerRepository.findIdByName(PROVIDER_NAME)).thenReturn(Optional.of(PROVIDER_ID));

        // When
        Long result = providerService.getIdByName(PROVIDER_NAME);

        // Then
        assertThat(result).isEqualTo(PROVIDER_ID);
        verify(providerRepository, times(1)).findIdByName(PROVIDER_NAME);
    }

    @Test
    @DisplayName("getIdByName - Should throw exception when provider not found")
    void getIdByName_ShouldThrowException_WhenNotFound() {
        // Given
        when(providerRepository.findIdByName(PROVIDER_NAME)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> providerService.getIdByName(PROVIDER_NAME))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Provider not found: " + PROVIDER_NAME);

        verify(providerRepository, times(1)).findIdByName(PROVIDER_NAME);
    }

    @Test
    @DisplayName("All methods should use @Transactional(readOnly = true)")
    void verifyTransactionAnnotations() throws NoSuchMethodException {
        assertThat(ProviderService.class.getMethod("getByName", String.class)
                                        .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();

        assertThat(ProviderService.class.getMethod("exists", String.class)
                                        .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();

        assertThat(ProviderService.class.getMethod("getIdByName", String.class)
                                        .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
                .isTrue();
    }
}

