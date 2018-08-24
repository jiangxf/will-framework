package org.will.framework.dlock.zk;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.LoggerFactory;
import org.will.framework.dlock.DLock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 16:30
 */
public class ZKDLock extends DLock {

    /**
     * 所有PERSISTENT锁节点的根位置
     */
    public static final String ROOT_PATH = "/ROOT_LOCK/";
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ZKDLock.class);
    /**
     * 线程池
     */
    private static final ScheduledExecutorService EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setNameFormat("zklock-thread-%d").build());
    private static final ConcurrentSkipListSet<String> ALL_PATH_SET = new ConcurrentSkipListSet<>();
    /**
     * 每次延迟清理PERSISTENT节点的时间  Unit:MILLISECONDS
     */
    private static final long DELAY_TIME_FOR_CLEAN = 1000;

    /**
     * zk 共享锁实现
     */
    private InterProcessMutex interProcessMutex = null;


    /**
     * 锁的ID,对应zk一个PERSISTENT节点,下挂EPHEMERAL节点.
     */
    private String path;

    /**
     * zk的客户端
     */
    private CuratorFramework client;

    public ZKDLock(CuratorFramework client, String lockKey) {
        super(lockKey);

        this.client = client;
        this.path = ROOT_PATH + lockKey;
        interProcessMutex = new InterProcessMutex(client, this.path);

        ALL_PATH_SET.add(path);
    }

    /**
     * 创建锁，保证原子操作
     *
     * @return
     */
    @Override
    protected boolean createLock(long expireMS) throws Exception {
        return interProcessMutex.acquire(expireMS, TimeUnit.MILLISECONDS);
    }

    /**
     * 释放锁，保证原子操作
     *
     * @return
     */
    @Override
    protected void releaseLock(String value) throws Exception {
        try {
            interProcessMutex.release();
        } finally {
            EXECUTOR_SERVICE.schedule(new Cleaner(client), DELAY_TIME_FOR_CLEAN, TimeUnit.MILLISECONDS);
        }
    }

    static class Cleaner implements Runnable {
        CuratorFramework client;

        public Cleaner(CuratorFramework client) {
            this.client = client;
        }

        @Override
        public void run() {
            if (ALL_PATH_SET.isEmpty()) {
                return;
            }

            Set<String> tempSet = new HashSet<>();
            for (String path : ALL_PATH_SET) {
                try {
                    List list = client.getChildren().forPath(path);
                    if (list == null || list.isEmpty()) {
                        client.delete().forPath(path);
                        tempSet.add(path);
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }

            ALL_PATH_SET.removeAll(tempSet);
        }
    }
}
