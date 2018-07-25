package org.will.framework.aq.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.will.framework.aq.AQMessage;
import org.will.framework.aq.consumer.AQConsumer;
import org.will.framework.aq.producer.AQProducer;
import org.will.framework.aq.producer.AQProducerConfig;
import org.will.framework.aq.queue.AQQueue;
import org.will.framework.aq.queue.LocalAQQueue;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 19:51
 */
public class AQDemo {

    protected final static Logger logger = LoggerFactory.getLogger(AQDemo.class);

    public static void main(String[] args) {

        final String topic = "TEST";

        AQQueue aqQueue = new LocalAQQueue();

        AQProducerConfig aqProducerConfig = new AQProducerConfig();
        aqProducerConfig.setCapacity(1000);

        final AQProducer aqProducer = new AQProducer(aqProducerConfig);
        aqProducer.setAqQueue(aqQueue);

        AQConsumer aqConsumer = new DemoAQConsumer(topic, 20, 0, aqQueue);
        aqConsumer.init();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int idx = 0;
                while (idx++ < 100000) {
                    AQMessage aqMessage = new AQMessage(topic, logger);
                    aqMessage.setMessageId("msgId_" + idx);

                    aqProducer.send(aqMessage);

                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int idx = 0;
                while (idx++ < 100000) {
                    AQMessage aqMessage = new AQMessage(topic, logger);
                    aqMessage.setMessageId("msgId_" + idx);

                    aqProducer.send(aqMessage);

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int idx = 0;
                while (idx++ < 100000) {
                    AQMessage aqMessage = new AQMessage(topic, logger);
                    aqMessage.setMessageId("msgId_" + idx);

                    aqProducer.send(aqMessage);

                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
