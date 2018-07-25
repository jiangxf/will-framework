//package org.will.framework.aq;
//
///**
// * Created by will on 15/04/2017.
// */
//public abstract class AQProcessor extends AQWorker {
//
//    protected long millis = 1000;
//
//    public AQProcessor(String threadName, AQConsumer aqConsumer) {
//        super(threadName, aqConsumer);
//    }
//
//    @Override
//    protected void doRun() {
//        long beginMillis = 0L;
//        int tryCount = 0;
//
//        while (true) {
//            beginMillis = System.currentTimeMillis();
//
//            // 从消息队列中取数据
//            AQMessage aqRequest = QAQ.dequeue(aqConsumer.getAction());
//            if (aqRequest == null) {
//                if (++tryCount >= 2) {
//                    if (aqConsumer.getAQProcessorMap().size() <= aqConsumer.getNeedThread().get()) {
//                        tryCount = 0;
//
//                        doSleep(millis + (System.currentTimeMillis() % 3) * 1000);
//                        continue;
//                    }
//                    return;
//                } else {
//                    doSleep(millis + (System.currentTimeMillis() % 3) * 1000);
//                    continue;
//                }
//            }
//            tryCount = 0;
//
//            // 处理请求
//            AQResponse aqResponse = doRequest(aqRequest);
//
//            // 记录日志
//            //LogCollectManager.cache("Consumer <> " + aqConsumer.getAction() + " - " + JsonHelper.toJson(aqRequest), "aq", aqConsumer.getAction());
//
//            // 统计当前处理的请求执行时间
//            long interval = System.currentTimeMillis() - beginMillis;
//            aqConsumer.getIntervals().offer(interval);
//            if (aqResponse == null) {
//                continue;
//            }
//
//            // 结果记录到应答缓存列表
//            QAQ.setResponse(aqConsumer.getAction(), aqResponse);
//        }
//    }
//
//    protected abstract AQResponse doRequest(AQMessage aqRequest);
//
//    public long getMillis() {
//        return millis;
//    }
//
//    public void setMillis(long millis) {
//        this.millis = millis;
//    }
//}
