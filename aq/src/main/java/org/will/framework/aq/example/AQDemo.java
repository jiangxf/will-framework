package org.will.framework.aq.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.will.framework.aq.common.AQMessage;
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

        final AQProducerConfig aqProducerConfig = new AQProducerConfig();
        aqProducerConfig.setCapacity(100);

        final AQProducer aqProducer = new AQProducer(aqProducerConfig, aqQueue);

        BookAQConsumer aqConsumer = new BookAQConsumer(topic, 20, 0, aqQueue);
        aqConsumer.start();

        final BookInfo bookInfo = new BookInfo();
        bookInfo.setAuthor("will");
        bookInfo.setPrice(69.5);
        bookInfo.setBookName("AQQueue从入门到放弃");

        new Thread(new Runnable() {
            @Override
            public void run() {
                int idx = 0;
                while (idx++ < 1000) {
                    bookInfo.setId("BID" + idx);
                    AQMessage aqMessage = new AQMessage(topic, bookInfo);
                    if(idx % 2 == 0) {
                        aqMessage.setSubType("add");
                    }else{
                        aqMessage.setSubType("update");
                    }
                    aqMessage.setAttachment("traceId", idx);
                    aqProducer.send(aqMessage);

                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

        while (true) {
            aqConsumer.start();

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            aqConsumer.stop();
        }
    }
}
