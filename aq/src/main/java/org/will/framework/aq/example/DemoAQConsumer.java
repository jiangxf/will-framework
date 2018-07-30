package org.will.framework.aq.example;

import org.will.framework.aq.AQMessage;
import org.will.framework.aq.consumer.AQConsumer;
import org.will.framework.aq.queue.AQQueue;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-24
 * Time: 21:23
 */
public class DemoAQConsumer extends AQConsumer {

    public DemoAQConsumer(String topic, AQQueue aqQueue) {
        super(topic, aqQueue);
    }

    public DemoAQConsumer(String topic, int maxThread, int minThread, AQQueue aqQueue) {
        super(topic, maxThread, minThread, aqQueue);
    }

    @Override
    protected void doProcessMessage(AQMessage aqRequest) {
        doSleep(ThreadLocalRandom.current().nextLong(100));
    }
}
