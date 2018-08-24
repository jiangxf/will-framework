package org.will.framework.dlock.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.will.framework.dlock.DLock;
import org.will.framework.dlock.DLockManager;

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

        final DLock dLock = DLockManager.newLocalLock("TEST");

        int idx = 0;
        while (idx++ < 20) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (dLock.tryLock(10000, 20000)) {
                        logger.info("获取锁成功");

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

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
}
