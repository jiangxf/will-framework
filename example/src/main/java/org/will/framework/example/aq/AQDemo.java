package org.will.framework.example.aq;

import com.qunar.redis.storage.Sedis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.will.framework.aq.common.AQMessage;
import org.will.framework.aq.producer.AQProducer;
import org.will.framework.aq.producer.AQProducerConfig;
import org.will.framework.aq.queue.AQQueue;
import org.will.framework.aq.queue.LocalAQQueue;
import org.will.framework.aq.queue.SedisAQQueue;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 19:51
 */
public class AQDemo {

    protected final static Logger logger = LoggerFactory.getLogger(AQDemo.class);

    private static String topic = "topic_demo";
    private static AQQueue aqQueue = new LocalAQQueue();

    public static void main(String[] args) {
        aqQueue = new SedisAQQueue(new Sedis("pay_ious_beta", "be1e8df1", "10.86.36.159:2181,10.86.36.176:2181,10.86.36.231:2181,10.86.37.227:2181,10.86.37.202:2181"));
        testProducer(aqQueue);
        testConsumer(aqQueue);
    }

    private static void testProducer(AQQueue aqQueue) {
        AQProducerConfig aqProducerConfig = new AQProducerConfig();
        final AQProducer aqProducer = new AQProducer(aqProducerConfig, aqQueue);

        aqProducer.clear(topic);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int idx = 0;
                while (idx++ < 100) {

                    BookInfo bookInfo = createBookInfo(idx);
                    AQMessage aqMessage = createAQMessage(topic, bookInfo, idx);
                    aqProducer.send(aqMessage);

                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private static void testConsumer(AQQueue aqQueue) {
        BookAQConsumer aqConsumer = new BookAQConsumer(topic, 20, 0, aqQueue);
        while (true) {
            aqConsumer.start();
            doSleep(10000);
            aqConsumer.stop();
        }
    }

    private static BookInfo createBookInfo(int idx) {
        BookInfo.AuthorInfo authorInfo = new BookInfo.AuthorInfo();
        authorInfo.setName("will");
        authorInfo.setAge(32);
        authorInfo.setSex("男");

        BookInfo bookInfo = new BookInfo();
        bookInfo.setId("BID" + idx);
        bookInfo.setAuthorInfo(authorInfo);
        bookInfo.setPrice(69.5);
        bookInfo.setBookName("AQQueue从入门到放弃");

        return bookInfo;
    }

    private static AQMessage createAQMessage(String topic, Object data, int idx) {
        AQMessage aqMessage = new AQMessage(topic, data);
        if (idx % 2 == 0) {
            aqMessage.setSubType("add");
        } else {
            aqMessage.setSubType("update");
        }
        aqMessage.setAttachment("traceId", "TID" + idx);
        return aqMessage;
    }

    private static void doSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
