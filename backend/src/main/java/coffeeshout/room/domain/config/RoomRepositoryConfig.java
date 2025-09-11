package coffeeshout.room.domain.config;

import coffeeshout.room.domain.repository.RoomRepository;
import coffeeshout.room.domain.repository.RedisRoomRepository;
import coffeeshout.room.domain.repository.MemoryRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RoomRepositoryConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "room.repository.type", havingValue = "redis", matchIfMissing = true)
    public RoomRepository redisRoomRepository(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        return new RedisRoomRepository(redisTemplate, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "room.repository.type", havingValue = "memory")
    public RoomRepository memoryRoomRepository() {
        return new MemoryRoomRepository();
    }
}