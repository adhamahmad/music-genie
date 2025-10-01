package com.example.musicGenie.config.cache;

import java.util.List;

import com.example.musicGenie.dtos.playlist.PlaylistDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession(
        maxInactiveIntervalInSeconds = 604800 // 1 week (7 * 24 * 60 * 60)
)
public class RedisConfig {

    @Value("${SPRING_REDIS_HOST:localhost}")
    private String redisHost;

    @Value("${SPRING_REDIS_PORT:6379}")
    private String redisPort;

    @Value("${SPRING_REDIS_PASSWORD:}")
    private String redisPassword;


    @Bean
    @Primary
    public LettuceConnectionFactory connectionFactory() {
        System.out.println("=== REDIS CONFIGURATION ===");
        System.out.println("Redis Host: " + redisHost);
        System.out.println("Redis Port: " + redisPort);
        System.out.println("============================");

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(Integer.parseInt(redisPort));
        if (!redisPassword.isEmpty()) {
            config.setPassword(redisPassword);  // <-- add this line
        }

        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serializer as String
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Create a Jackson ObjectMapper that handles Java 8 time types
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());


        // Value serializer as JSON using the configured ObjectMapper
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

}
