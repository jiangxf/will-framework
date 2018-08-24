package org.will.framework.dlock.sedis;

import com.qunar.redis.storage.Sedis;
import org.slf4j.LoggerFactory;
import org.will.framework.dlock.DLock;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 16:30
 */
public class SedisDLock extends DLock {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SedisDLock.class);

    private Sedis sedis;

    public SedisDLock(Sedis sedis, String lockKey) {
        super(lockKey);
        this.sedis = sedis;
    }

    /**
     * 创建锁，保证原子操作
     *
     * @return
     */
    @Override
    protected boolean createLock(long expireMS) throws Exception {
        long rs = sedis.setnx(lockKey, "-1");
        if (rs == 1) {
            sedis.set(lockKey, "-1", "XX", "EX", expireMS); // 设置过期时间
            return true;
        }
        return false;
    }

    /**
     * 释放锁，保证原子操作
     *
     * @return
     */
    @Override
    protected void releaseLock(String value) throws Exception {
        sedis.del(lockKey);
    }
}
