package org.will.framework.dlock;

import org.slf4j.LoggerFactory;
import org.will.framework.dlock.redis.RedisDLock;
import redis.clients.jedis.Jedis;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-08-24
 * Time: 12:13
 */
public final class DLockManager {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DLockManager.class);

    private final static char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
            'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z'};

    /**
     * 重试等待时间
     */
    private final static int RETRY_AWAIT = 300;
    private final static int lockTimeout = 2000;
    private static Jedis jedis;

    public static Jedis getJedis() {
        return jedis;
    }

    public static void setJedis(Jedis jedis) {
        DLockManager.jedis = jedis;
    }

    public static DLock newRedisLock(String lockKey) {
        return new RedisDLock(jedis, lockKey);
    }
}
