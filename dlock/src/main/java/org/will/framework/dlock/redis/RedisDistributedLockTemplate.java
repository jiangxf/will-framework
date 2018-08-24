package org.will.framework.dlock.redis;

import org.slf4j.LoggerFactory;
import org.will.framework.dlock.DLock;
import org.will.framework.dlock.DLockCallback;
import org.will.framework.dlock.DLockManager;
import org.will.framework.dlock.DLockTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 16:30
 */
public class RedisDistributedLockTemplate implements DLockTemplate {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RedisDistributedLockTemplate.class);

    @Override
    public Object execute(String lockKey, long timeoutMS, long expireMS, DLockCallback callback) {
        DLock dLock = null;
        boolean getLock = false;
        try {
            dLock = DLockManager.newRedisLock(lockKey);
            if (dLock.tryLock(timeoutMS, expireMS)) {
                getLock = true;
                return callback.onGetLock();
            } else {
                return callback.onTimeout();
            }
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (getLock) {
                dLock.unlock();
            }
        }
        return null;
    }
}
