package org.will.framework.dlock;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 16:30
 */
public interface DLockTemplate {

    /**
     * @param lockKey   锁id(对应业务唯一ID)
     * @param timeoutMS  单位毫秒
     * @param dLockCallback 回调函数
     * @return
     */
    Object execute(String lockKey, long timeoutMS, long expireMS, DLockCallback dLockCallback);
}
