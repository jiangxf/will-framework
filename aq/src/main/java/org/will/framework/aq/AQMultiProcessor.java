package org.will.framework.aq;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 15/04/2017.
 */
public abstract class AQMultiProcessor extends AQProcessor {

    protected int multiCount = 10;

    public AQMultiProcessor(String threadName, AQConsumer aqConsumer, int multiCount) {
        super(threadName, aqConsumer);
        this.multiCount = multiCount;
    }

    @Override
    protected void doRun() {
        long beginMillis = 0L;
        int tryCount = 0;
        int i = 0;
        List<AQMessage> aqRequestList = new ArrayList<>();

        while (true) {
            beginMillis = System.currentTimeMillis();
            aqRequestList.clear();
            i = 0;
            while (i++ < multiCount) {
                AQMessage aqRequest = QAQ.dequeue(aqConsumer.getAction());
                if (aqRequest == null) {
                    break;
                } else {
                    aqRequestList.add(aqRequest);
                }
            }

            if (aqRequestList.size() <= 0) {
                if (++tryCount >= 2) {
                    if (aqConsumer.getAQProcessorMap().size() <= aqConsumer.getNeedThread().get()) {
                        tryCount = 0;

                        doSleep(millis + (System.currentTimeMillis() % 3) * 1000);
                        continue;
                    }
                    return;
                } else {
                    doSleep(millis + (System.currentTimeMillis() % 3) * 1000);
                    continue;
                }
            }
            tryCount = 0;

            // 处理请求
            List<AQResponse> aqResponseList = doRequest(aqRequestList);

            // 记录日志
            //LogCollectManager.common("Consumer <> " + aqConsumer.getAction() + " Count: " + aqRequestList.size(), "aq", aqConsumer.getAction());

            // 统计当前处理的请求执行时间
            long interval = System.currentTimeMillis() - beginMillis;
            aqConsumer.getIntervals().offer(interval);

            if (aqResponseList == null) {
                continue;
            }

            // 结果记录到应答缓存列表
            for (AQResponse aqResponse : aqResponseList) {
                QAQ.setResponse(aqConsumer.getAction(), aqResponse);
            }
        }
    }

    protected abstract List<AQResponse> doRequest(List<AQMessage> aqRequestList);
}
