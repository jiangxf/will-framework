package org.will.framework.dlock;

import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 16:30
 */
public abstract class DLockTemplate {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DLockTemplate.class);

    /**
     * @param lockKey   锁id(对应业务唯一ID)
     * @param timeoutMS 单位毫秒
     * @param callback  回调函数
     * @return
     */
    public Object execute(String lockKey, long timeoutMS, long expireMS, DLockCallback callback) {
        DLock dLock = null;
        boolean getLock = false;
        try {
            dLock = newDLock(lockKey);
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

    protected abstract DLock newDLock(String lockKey);
}
