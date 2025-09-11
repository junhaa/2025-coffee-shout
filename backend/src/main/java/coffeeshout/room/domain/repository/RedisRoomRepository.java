package coffeeshout.room.domain.repository;

import static org.springframework.util.Assert.notNull;

import coffeeshout.room.domain.JoinCode;
import coffeeshout.room.domain.Playable;
import coffeeshout.room.domain.Room;
import coffeeshout.room.domain.RoomState;
import coffeeshout.room.domain.player.Player;
import coffeeshout.room.domain.player.Players;
import coffeeshout.room.domain.roulette.Roulette;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedList;
import java.util.Queue;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisRoomRepository implements RoomRepository {

    private static final String ROOM_KEY_PREFIX = "room:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(3);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisRoomRepository(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Room> findByJoinCode(JoinCode joinCode) {
        String key = generateKey(joinCode);
        Map<Object, Object> hashFields = redisTemplate.opsForHash().entries(key);
        
        if (hashFields.isEmpty()) {
            return Optional.empty();
        }
        
        try {
            Room room = deserializeRoom(hashFields);
            return Optional.of(room);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Room 역직렬화 실패", e);
        }
    }

    @Override
    public boolean existsByJoinCode(JoinCode joinCode) {
        String key = generateKey(joinCode);
        return redisTemplate.hasKey(key);
    }

    @Override
    public Room save(Room room) {
        notNull(room, "Room은 null일 수 없습니다.");
        
        String key = generateKey(room.getJoinCode());
        
        try {
            Map<String, String> hashFields = serializeRoom(room);
            redisTemplate.opsForHash().putAll(key, hashFields);
            redisTemplate.expire(key, DEFAULT_TTL);
            return room;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Room 직렬화 실패", e);
        }
    }

    @Override
    public void deleteByJoinCode(JoinCode joinCode) {
        notNull(joinCode, "JoinCode는 null일 수 없습니다.");
        
        String key = generateKey(joinCode);
        redisTemplate.delete(key);
    }

    private String generateKey(JoinCode joinCode) {
        return ROOM_KEY_PREFIX + joinCode.getValue();
    }

    private Map<String, String> serializeRoom(Room room) throws JsonProcessingException {
        return Map.of(
            "joinCode", room.getJoinCode().getValue(),
            "host", objectMapper.writeValueAsString(room.getHost()),
            "roomState", room.getRoomState().name(),
            "players", objectMapper.writeValueAsString(room.getPlayers()),
            "roulette", objectMapper.writeValueAsString(room.getRoulette()),
            "miniGames", objectMapper.writeValueAsString(room.getAllMiniGame()),
            "finishedGames", objectMapper.writeValueAsString(room.getFinishedGames()),
            "probabilities", objectMapper.writeValueAsString(room.getProbabilities())
        );
    }

    private Room deserializeRoom(Map<Object, Object> hashFields) throws JsonProcessingException {
        // 기본 필드 추출
        String joinCodeValue = (String) hashFields.get("joinCode");
        String hostJson = (String) hashFields.get("host");
        String roomStateStr = (String) hashFields.get("roomState");
        String playersJson = (String) hashFields.get("players");
        String rouletteJson = (String) hashFields.get("roulette");
        String miniGamesJson = (String) hashFields.get("miniGames");
        String finishedGamesJson = (String) hashFields.get("finishedGames");
        
        // 객체 역직렬화
        JoinCode joinCode = new JoinCode(joinCodeValue);
        Player host = objectMapper.readValue(hostJson, Player.class);
        RoomState roomState = RoomState.valueOf(roomStateStr);
        Players players = objectMapper.readValue(playersJson, Players.class);
        Roulette roulette = objectMapper.readValue(rouletteJson, Roulette.class);
        
        Queue<Playable> miniGames = objectMapper.readValue(
            miniGamesJson, 
            new TypeReference<LinkedList<Playable>>() {}
        );
        java.util.List<Playable> finishedGames = objectMapper.readValue(
            finishedGamesJson, 
            new TypeReference<java.util.List<Playable>>() {}
        );
        
        return Room.restore(joinCode, host, roomState, players, roulette, miniGames, finishedGames);
    }
}