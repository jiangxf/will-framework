package org.will.framework.dlock.example;

import com.qunar.redis.storage.Sedis;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.will.framework.dlock.DLock;
import org.will.framework.dlock.DLockFactory;
import org.will.framework.dlock.DLockType;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 19:51
 */
public class DLockDemo {

    protected final static Logger logger = LoggerFactory.getLogger(DLockDemo.class);

    public static void main(String[] args) {
        testSedisDLock();
    }

    private static void testLocalDLock() {
        DLock dLock = DLockFactory.newLock(DLockType.Local, "TEST");
        doDLock(dLock);
    }

    private static void testSedisDLock() {

        DLockFactory.setSedis(new Sedis("pay_ious_beta", "be1e8df1", "10.86.36.159:2181,10.86.36.176:2181,10.86.36.231:2181,10.86.37.227:2181,10.86.37.202:2181"));

        DLock dLock = DLockFactory.newLock(DLockType.Sedis, "TEST");
        doDLock(dLock);
    }

    private static void testZKDLock() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("zk.beta.corp.qunar.com:2181", retryPolicy);
        client.start();

        DLockFactory.setClient(client);

        DLock dLock = DLockFactory.newLock(DLockType.ZK, "TEST");
        doDLock(dLock);
    }

    private static void doDLock(final DLock dLock) {
        int idx = 0;
        while (idx++ < 20) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (dLock.tryLock(10000, 20000)) {
                        logger.info("获取锁成功");

                        int i = 0;
                        while (i++ < 10) {
                            doSubLock(dLock);
                        }
                        logger.info("释放锁成功");
                        dLock.unlock();
                    } else {
                        logger.warn("获取锁失败");
                    }
                }
            }
            ).start();
        }

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void doSubLock(DLock dLock) {
        if (dLock.tryLock(10000, 20000)) {
            logger.info("获取锁成功 doSubLock");

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.info("释放锁成功 doSubLock");
            dLock.unlock();
        } else {
            logger.warn("获取锁失败 doSubLock");
        }
    }
}
