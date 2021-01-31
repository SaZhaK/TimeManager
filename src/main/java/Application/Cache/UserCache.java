package Application.Cache;

import Application.Entities.User;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class UserCache {

    private static final int MAX_CACHE_SIZE = 5;
    private static final int EXPIRE_AFTER_ACCESS_TIME = 5;
    private static final Cache<Long, User> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(EXPIRE_AFTER_ACCESS_TIME, TimeUnit.MINUTES)
            .maximumSize(MAX_CACHE_SIZE)
            .build();

    public UserCache() {
    }

    public static User getUser(long id) {
        return cache.getIfPresent(id);
    }

    public static void cacheUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Can not cache null user");
        }

        cache.put(user.getId(), user);
    }
}
