package org.will.framework.aq.consumer;

import org.will.framework.aq.AQMessage;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-25
 * Time: 11:48
 */
public abstract class AQWorker extends AQThread {

    public static final int DEFAULT_TIMEOUT_MS = 3000;
    protected final AQConsumer aqConsumer;
    protected final int millis;

    public AQWorker(String threadName, AQConsumer aqConsumer) {
        super(threadName);
        this.aqConsumer = aqConsumer;
        this.millis = DEFAULT_TIMEOUT_MS;
    }

    @Override
    protected void doRun() throws InterruptedException{
        long beginMillis = 0L;
        AQMessage aqMessage = null;

        while (!isInterrupted()) {
            aqConsumer.acquire();

            // 从消息队列中取数据
            aqMessage = getMessage();
            if (aqMessage == null) {
                return;
            }

            beginMillis = System.currentTimeMillis();

            // 处理请求
            processMessage(aqMessage);

            // 统计当前处理的请求执行时间
            long interval = System.currentTimeMillis() - beginMillis;
            aqConsumer.getElapseQueue().offer(interval);
        }
    }

    @Override
    protected void doFinish(Exception ex) {
        this.aqConsumer.getWorkerThreadMap().remove(getName());
    }

    protected AQMessage getMessage() throws InterruptedException {
        int tryCount = 0;
        while (true) {
            AQMessage aqMessage = aqConsumer.recv();
            if (aqMessage == null) {
                if (++tryCount >= 2) {
                    if (aqConsumer.getWorkerThreadMap().size() <= aqConsumer.getNeedThread()) {
                        tryCount = 0;
                        randomSleep();
                        continue;
                    }
                    return null;
                } else {
                    randomSleep();
                    continue;
                }
            } else {
                return aqMessage;
            }
        }
    }

    protected abstract void processMessage(AQMessage aqRequest);

    protected void randomSleep() throws InterruptedException {
        sleep(millis + ThreadLocalRandom.current().nextInt(5) * 1000);
    }
}
