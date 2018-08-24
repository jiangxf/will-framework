package org.will.framework.dlock;

import com.qunar.redis.storage.Sedis;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.LoggerFactory;
import org.will.framework.dlock.jedis.JedisDLock;
import org.will.framework.dlock.local.LocalDLock;
import org.will.framework.dlock.sedis.SedisDLock;
import redis.clients.jedis.Jedis;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-08-24
 * Time: 12:13
 */
public final class DLockFactory {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DLockFactory.class);

    /**
     * 重试等待时间
     */
    private static Jedis jedis;

    private static Sedis sedis;

    /**
     * zk的客户端
     */
    private static CuratorFramework client;

    public static Jedis getJedis() {
        return jedis;
    }

    public static void setJedis(Jedis jedis) {
        DLockFactory.jedis = jedis;
    }

    public static Sedis getSedis() {
        return sedis;
    }

    public static void setSedis(Sedis sedis) {
        DLockFactory.sedis = sedis;
    }

    public static DLock newLock(DLockType dLockType, String lockKey) {
        if (DLockType.Jedis.equals(dLockType)) {
            return new JedisDLock(jedis, lockKey);
        } else if (DLockType.ZK.equals(dLockType)) {
            return new JedisDLock(jedis, lockKey);
        } else if (DLockType.Sedis.equals(dLockType)) {
            return new SedisDLock(sedis, lockKey);
        }
        return new LocalDLock(lockKey);
    }

    public static CuratorFramework getClient() {
        return client;
    }

    public static void setClient(CuratorFramework client) {
        DLockFactory.client = client;
    }
}
