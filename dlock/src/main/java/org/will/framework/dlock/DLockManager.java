package org.will.framework.dlock;

import org.slf4j.LoggerFactory;
import org.will.framework.dlock.local.LocalDLock;
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

    /**
     * 重试等待时间
     */
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

    public static DLock newLocalLock(String lockKey) {
        return new LocalDLock(lockKey);
    }
}
