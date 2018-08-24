package org.will.framework.dlock.local;

import com.google.common.collect.Maps;
import org.slf4j.LoggerFactory;
import org.will.framework.dlock.DLock;

import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 16:30
 */
public class LocalDLock extends DLock {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LocalDLock.class);

    private static volatile Map<String, Long> lockKeyMap = Maps.newConcurrentMap();

    public LocalDLock(String lockKey) {
        super(lockKey);
    }

    /**
     * 创建锁，保证原子操作
     *
     * @return
     */
    @Override
    protected boolean createLock(long expireMS) {
        synchronized (LocalDLock.class) {
            if (!lockKeyMap.containsKey(lockKey)) {
                lockKeyMap.put(lockKey, System.currentTimeMillis() + expireMS);
                return true;
            }

            // 判断是否过期
            if (System.currentTimeMillis() - lockKeyMap.get(lockKey) > 0) {
                lockKeyMap.put(lockKey, System.currentTimeMillis() + expireMS);
                return true;
            }

            return false;
        }
    }

    /**
     * 释放锁，保证原子操作
     *
     * @return
     */
    @Override
    protected void doUnlock(String key, String value) {
        synchronized (LocalDLock.class) {
            lockKeyMap.remove(lockKey);
        }
    }
}
