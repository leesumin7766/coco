package com.example.shop.config;

import java.time.Duration;

import com.example.shop.resilience.RedisCacheBypassGate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
@Slf4j
public class RedisCacheConfig {

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// 타입 정보를 JSON에 포함
		objectMapper.activateDefaultTyping(
			LaissezFaireSubTypeValidator.instance,
			ObjectMapper.DefaultTyping.NON_FINAL,
			JsonTypeInfo.As.PROPERTY
		);

		GenericJackson2JsonRedisSerializer serializer =
			new GenericJackson2JsonRedisSerializer(objectMapper);

		RedisCacheConfiguration config =
			RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofMinutes(10))
				.serializeValuesWith(
					RedisSerializationContext.SerializationPair.fromSerializer(serializer)
				)
				.disableCachingNullValues();

		return RedisCacheManager.builder(connectionFactory)
			.cacheDefaults(config)
			.build();
	}

	@Bean
	public CacheErrorHandler cacheErrorHandler(RedisCacheBypassGate redisCacheBypassGate) {
		return new CacheErrorHandler() {
			@Override
			public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
				String cacheName = cache != null ? cache.getName() : "unknown";
				redisCacheBypassGate.recordFailure(cacheName, exception);
				log.warn("Redis cache GET failed. cache={}, key={}, fallback=DB, reason={}",
					cacheName,
					key,
					exception.getMessage());
			}

			@Override
			public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
				String cacheName = cache != null ? cache.getName() : "unknown";
				redisCacheBypassGate.recordFailure(cacheName, exception);
				log.warn("Redis cache PUT failed. cache={}, key={}, reason={}",
					cacheName,
					key,
					exception.getMessage());
			}

			@Override
			public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
				String cacheName = cache != null ? cache.getName() : "unknown";
				redisCacheBypassGate.recordFailure(cacheName, exception);
				log.warn("Redis cache EVICT failed. cache={}, key={}, reason={}",
					cacheName,
					key,
					exception.getMessage());
			}

			@Override
			public void handleCacheClearError(RuntimeException exception, Cache cache) {
				String cacheName = cache != null ? cache.getName() : "unknown";
				redisCacheBypassGate.recordFailure(cacheName, exception);
				log.warn("Redis cache CLEAR failed. cache={}, reason={}",
					cacheName,
					exception.getMessage());
			}
		};
	}
}
