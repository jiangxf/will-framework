package org.will.framework.aq.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by will on 15/04/2017.
 */
public abstract class AQThread extends Thread {

    public AQThread(String threadName) {
        super(threadName);
    }

    @Override
    public final void run() {
        Exception exception = null;

        doBefore();

        try {
            logger.info("线程 {} 正常开启", getName());
            doRun();
            logger.info("线程 {} 正常断开", getName());
        } catch (Exception e) {
            exception = e;
            logger.error("线程 {} 异常断开 Error:{}", getName(), e);
        }

        doFinish(exception);
    }

    protected void doBefore() {
    }

    protected abstract void doRun();

    protected void doFinish(Exception ex) {
    }

    protected final void doSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
}
