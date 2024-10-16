package usermanagement.userManagement.service;

import io.lettuce.core.RedisException;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import usermanagement.userManagement.exception.RedisCustomeException;

import java.util.Map;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Save a single field in a hash
    public void hSet(String hashKey, String field, Object value) throws RedisCustomeException {
        try {
            redisTemplate.opsForHash().put(hashKey, field, value);
        } catch (Exception e) {
            throw new RedisCustomeException("Exception occured while saving data");
        }
    }

    // Retrieve a single field from a hash
    public Object hGet(String hashKey, String field)  {
        try {
            return redisTemplate.opsForHash().get(hashKey, field);
        } catch (Exception e) {
            //throw new RedisCustomeException("Exception occurred while fetching data");
        }
        return null;
    }

    // Retrieve all fields and values from a hash
    public Map<Object, Object> hGetAll(String hashKey) {
        return redisTemplate.opsForHash().entries(hashKey);
    }

    // Delete a field from a hash
    public void hDelete(String hashKey, String field) throws RedisCustomeException {
        try {
            redisTemplate.opsForHash().delete(hashKey, field);
        } catch (Exception e) {
            throw new RedisCustomeException("Exception occured while deleting data");
        }
    }

    // Check if a field exists in a hash
    public boolean hExists(String hashKey, String field) {
        return redisTemplate.opsForHash().hasKey(hashKey, field);
    }

    // Increment a field in a hash
    public Long hIncrement(String hashKey, String field, long increment) {
        return redisTemplate.opsForHash().increment(hashKey, field, increment);
    }
}
