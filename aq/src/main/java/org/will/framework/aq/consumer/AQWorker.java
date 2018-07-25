package org.will.framework.aq.consumer;

import org.will.framework.aq.AQMessage;
import org.will.framework.aq.AQResponse;

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
        int tryCount = 0;

        while (true) {
            aqConsumer.acquire();

            // 从消息队列中取数据
            beginMillis = System.currentTimeMillis();
            AQMessage aqMessage = aqConsumer.recv();
            if (aqMessage == null) {
                if (++tryCount >= 2) {
                    if (aqConsumer.getWorkerThreadMap().size() <= aqConsumer.getNeedThread()) {
                        tryCount = 0;

                        doSleep(millis + (System.currentTimeMillis() % 10) * 1000);
                        continue;
                    }
                    return;
                } else {
                    doSleep(millis + (System.currentTimeMillis() % 10) * 1000);
                    continue;
                }
            }
            tryCount = 0;

            // 处理请求
            AQResponse aqResponse = processMessage(aqMessage);

            // 统计当前处理的请求执行时间
            long interval = System.currentTimeMillis() - beginMillis;
            aqConsumer.getElapseQueue().offer(interval);
            if (aqResponse == null) {
                continue;
            }

            // 结果记录到应答缓存列表
//            QAQ.setResponse(aqConsumer.getAction(), aqResponse);
        }
    }

    @Override
    protected void doFinish(Exception ex) {
        this.aqConsumer.getWorkerThreadMap().remove(getName());
    }

    protected abstract AQResponse processMessage(AQMessage aqRequest);

    protected final AQConsumer aqConsumer;

    protected final int millis;
}
