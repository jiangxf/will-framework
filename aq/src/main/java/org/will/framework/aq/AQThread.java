package org.will.framework.aq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by will on 15/04/2017.
 */
public abstract class AQThread extends Thread {
    protected Logger logger = LoggerFactory.getLogger(AQThread.this.getClass());

    // 线程名称
    protected String threadName = "";

    //
    protected AQConsumer aqConsumer = null;

    public AQThread(String threadName, AQConsumer aqConsumer) {
        this.threadName = threadName;
        this.aqConsumer = aqConsumer;
    }

    @Override
    public final void run() {
        try {
            doRun();
        } catch (Exception e) {
            logger.error(threadName + " 线程断开 Error :{}", e);
        } finally {
            logger.info(threadName + " 线程断开");
            aqConsumer.getAQProcessorMap().remove(threadName);
        }
    }

    protected abstract void doRun();

    protected final void doSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }
}
