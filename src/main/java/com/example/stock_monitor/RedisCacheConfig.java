
        package com.example.stock_monitor;

        import org.springframework.boot.ApplicationRunner;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.data.redis.cache.RedisCacheConfiguration;
        import org.springframework.data.redis.cache.RedisCacheManager;
        import org.springframework.data.redis.connection.RedisConnectionFactory;
        import org.springframework.data.redis.serializer.RedisSerializationContext;
        import org.springframework.data.redis.serializer.RedisSerializer;

        import java.time.Duration;

        @Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory) {

        // Default cache: 60 seconds
        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofSeconds(60))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(RedisSerializer.java()));

        // Daily market history cache: 24 hours
        RedisCacheConfiguration dailyHistoryConfig =
                defaultConfig.entryTtl(Duration.ofHours(24));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(
                        "dailyMarketHistory",
                        dailyHistoryConfig)
                .build();
    }

    // Temporary Redis connectivity test
    @Bean
    public ApplicationRunner redisConnectionTest(
            RedisConnectionFactory factory) {

        return args -> {
            try (var connection = factory.getConnection()) {
                System.out.println(
                        "Redis connection test: " + connection.ping());
            }
        };
    }
}