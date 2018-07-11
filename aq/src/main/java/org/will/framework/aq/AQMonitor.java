package org.will.framework.aq;


import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by will on 15/04/2017.
 */
public class AQMonitor {
    private static ConcurrentSkipListSet<AQConsumer> aqConsumerList = new ConcurrentSkipListSet<AQConsumer>();

    public static void addAQConsumer(AQConsumer aqConsumer) {
        aqConsumerList.add(aqConsumer);
    }

    /**
     * 监控所有的消息消费线程
     */
    public static void monitor() {
        for (AQConsumer aqConsumer : aqConsumerList) {
            aqConsumer.schedulerMonitor();
        }
    }
}
