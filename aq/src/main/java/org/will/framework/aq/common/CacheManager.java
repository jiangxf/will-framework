package org.will.framework.aq.common;

import redis.clients.jedis.JedisCommands;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-06-22
 * Time: 20:58
 */
public final class CacheManager {
    private static JedisCommands cache;

    public static JedisCommands getCache() {
        return cache;
    }

    public static void setCache(JedisCommands cache) {
        cache = cache;
    }
}
