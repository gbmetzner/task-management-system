package com.gbm.taskapi.runner;

import com.gbm.taskapi.helper.UserMapper;
import com.gbm.taskapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.CacheManager;

@AllArgsConstructor
@Slf4j
public class CacheWarmupRunner implements ApplicationRunner {

    private final CacheManager cacheManager;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public void run(@NonNull ApplicationArguments ignored) throws Exception {
        var cachedUsers = cacheManager.getCache("users");

        if (cachedUsers == null) {
            return;
        }

        var totalCachedUsers = userRepository.findAll().stream()
                .map(userMapper::toCacheableDto)
                .peek(u -> {
                    cachedUsers.put(u.id(), u);
                    cachedUsers.put("email:" + u.email(), u);
                })
                .count();
        log.info("CACHE WARMUP: Cached {} users", totalCachedUsers);
    }
}
