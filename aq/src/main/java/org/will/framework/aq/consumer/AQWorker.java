package org.will.framework.aq.consumer;

import org.will.framework.aq.AQMessage;
import org.will.framework.aq.exception.EmptyQueueException;

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

    public AQWorker(String threadName, AQConsumer aqConsumer) {
        super(threadName);
        this.aqConsumer = aqConsumer;
        this.millis = DEFAULT_TIMEOUT_MS;
    }

    @Override
    protected void doRun() {
        long beginMillis = 0L;
        AQMessage aqMessage = null;

        while (true) {
//            aqConsumer.acquire();

            // 从消息队列中取数据
            try {
                aqMessage = getMessage();
            } catch (EmptyQueueException ex) {
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

    protected abstract void processMessage(AQMessage aqRequest);

    protected final AQConsumer aqConsumer;

    protected final int millis;

    protected AQMessage getMessage() throws EmptyQueueException {
        int tryCount = 0;
        while (true) {
            AQMessage aqMessage = aqConsumer.recv();
            if (aqMessage == null) {
                if (++tryCount >= 2) {
                    if (aqConsumer.getWorkerThreadMap().size() <= aqConsumer.getNeedThread()) {
                        tryCount = 0;

                        doSleep(millis + ThreadLocalRandom.current().nextInt(5) * 1000);
                        continue;
                    }
                    throw new EmptyQueueException("队列消费完毕");
                } else {
                    doSleep(millis + ThreadLocalRandom.current().nextInt(5) * 1000);
                    continue;
                }
            } else {
                return aqMessage;
            }
        }
    }
}
