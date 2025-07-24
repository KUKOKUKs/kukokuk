package com.kukokuk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String redisHost;

  @Value("${spring.data.redis.port}")
  private int redisPort;

  // RedisConnection을 생성해주는 RedisConnectionFactory를 빈으로 등록
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    // Redis Connector에는 대표적으로 Lettuce와 Jedis가 있다
    return new LettuceConnectionFactory(redisHost, redisPort);
  }

  // key/value가 모두 String인 경우 간편하게 사용할 수 있음
  @Bean
  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) { // 위에서 만들어진 객체가 전달됨(DI)
    return new StringRedisTemplate(factory);
  }

  // 객체를 Redis에 저장할 경우 이걸 사용
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(factory);
    // 직렬화 방식을 지정
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

    return redisTemplate;
  }
}
