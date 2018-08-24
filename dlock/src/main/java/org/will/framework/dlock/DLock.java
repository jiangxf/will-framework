package org.will.framework.dlock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 16:30
 */
public abstract class DLock{

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DLock.class);

    private final static int RETRY_AWAIT_MS = 100;

    private final static ConcurrentMap<Thread, List<LockData>> THREAD_LOCK_MAP = Maps.newConcurrentMap();

    protected String lockKey;

    public DLock(String lockKey) {
        this.lockKey = lockKey;
    }

    /**
     * 尝试获取锁
     * @param timeoutMS 尝试获取锁的过期时间
     * @param expireMS 锁自动过期时间
     * @return
     */
    public boolean tryLock(long timeoutMS, long expireMS) {
        Thread curThread = Thread.currentThread();
        List<LockData> lockDatas = THREAD_LOCK_MAP.get(curThread);

        LockData lockData = LockData.filter(lockDatas, lockKey);
        if (lockData != null) {
            lockData.incrementAndGet();
            return true;
        }

        try {
            if (!doLock(timeoutMS, expireMS)) {
                return false;
            }
        } catch (Exception ex) {
            // 适配异常
            return true;
        }

        if (lockDatas == null) {
            lockDatas = Lists.newArrayList();
            THREAD_LOCK_MAP.put(curThread, lockDatas);
        }
        LockData newLockData = new LockData(curThread, lockKey);
        lockDatas.add(newLockData);

        return true;
    }

    /**
     * 创建 redis 锁的核心代码
     *
     * @param timeoutMS
     * @param expireMS
     * @return
     */
    protected boolean doLock(long timeoutMS, long expireMS) {
        final long startMillis = System.currentTimeMillis();
        final long millisToWait = timeoutMS > 0 ? timeoutMS : 0;

        boolean lockStatus = false;
        while (!lockStatus) {
            lockStatus = createLock(expireMS);

            // 成功占有锁
            if (lockStatus) {
                break;
            }

            // 等待超时，抢锁失败
            if (System.currentTimeMillis() > startMillis + millisToWait + RETRY_AWAIT_MS) {
                break;
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(RETRY_AWAIT_MS));
        }
        return lockStatus;
    }

    /**
     * 创建锁，保证原子操作
     *
     * @return
     */
    protected abstract boolean createLock(long expireMS);

    /**
     * 解锁
     */
    public void unlock() {
        Thread curThread = Thread.currentThread();
        List<LockData> lockDatas = THREAD_LOCK_MAP.get(curThread);

        // 创建锁失败，不能解锁
        LockData lockData = LockData.filter(lockDatas, lockKey);
        if (lockData == null) {
            logger.error("You do not own the lock: " + lockKey);
            return;
        }

        // 同一个线程共享锁，计数减1
        int newLockCount = lockData.decrementAndGet();
        if (newLockCount > 0) {
            return;
        }

        // 计数不对，不能解锁
        if (newLockCount < 0) {
            logger.error("Lock count has gone negative for lock: " + lockKey);
            return;
        }

        try {
            doUnlock(lockKey, lockData.lockVal);
        } finally {
            THREAD_LOCK_MAP.remove(curThread);
        }
    }

    protected abstract void doUnlock(String key, String value);

    private static class LockData {
        final Thread curThread;
        final String lockVal;
        final AtomicInteger lockCount = new AtomicInteger(1);

        private LockData(Thread curThread, String lockVal) {
            this.curThread = curThread;
            this.lockVal = lockVal;
        }

        private int incrementAndGet() {
            return lockCount.incrementAndGet();
        }

        private int decrementAndGet() {
            return lockCount.decrementAndGet();
        }

        private static LockData filter(List<LockData> lockDatas, String lockKey) {
            if (lockDatas == null || lockDatas.size() == 0) {
                return null;
            }

            for (LockData lockData : lockDatas){
                if(lockData.lockVal == lockKey){
                    return lockData;
                }
            }
            return null;
        }
    }
}
