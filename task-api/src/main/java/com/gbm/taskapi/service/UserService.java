package com.gbm.taskapi.service;

import com.gbm.taskapi.dto.UserCacheDto;
import com.gbm.taskapi.helper.UserMapper;
import com.gbm.taskapi.model.User;
import com.gbm.taskapi.repository.UserRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // Cache user by ID
    @Cacheable(value = "users", key = "#id", unless = "#result == null")
    public Optional<UserCacheDto> findById(Long id) {
        log.info("DB QUERY: Finding user by ID: {}", id);
        return userRepository.findById(id).map(userMapper::toCacheableDto);
    }

    // Cache user by email
    @Cacheable(value = "users", key = "'email:' + #email", unless = "#result == null")
    public Optional<UserCacheDto> findByEmail(String email) {
        log.info("DB QUERY: Finding user by email: {}", email);
        return userRepository.findByEmail(email).map(userMapper::toCacheableDto);
    }

    // Create user (don't cache on creation)
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // Update user and refresh cache
    @Transactional
    @Caching(
            put = {
                @CachePut(value = "users", key = "#user.id"),
                @CachePut(value = "users", key = "'email:' + #user.email")
            })
    public UserCacheDto updateUser(User user) {
        log.info("CACHE UPDATE: Updating user: {}", user.getId());
        return userMapper.toCacheableDto(userRepository.save(user));
    }

    // Delete user and evict from cache
    @Transactional
    @Caching(
            evict = {@CacheEvict(value = "users", key = "#id"), @CacheEvict(value = "users", key = "'email:' + #email")
            })
    public void deleteUser(Long id, String email) {
        log.info("CACHE EVICT: Deleting user: {}", id);
        userRepository.deleteById(id);
    }

    // Clear all user caches
    @CacheEvict(value = "users", allEntries = true)
    public void clearAllUserCaches() {
        log.info("CACHE EVICT: Clearing all user caches");
    }
}
