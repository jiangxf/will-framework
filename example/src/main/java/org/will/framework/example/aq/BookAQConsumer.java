package org.will.framework.example.aq;

import org.will.framework.aq.common.AQContext;
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
public class BookAQConsumer extends AQConsumer<BookInfo> {

    public BookAQConsumer(String topic, int maxThread, int minThread, AQQueue aqQueue) {
        super(topic, maxThread, minThread, aqQueue);
    }

    @Override
    protected void doProcess(AQContext aqContext, BookInfo data) {
        if ("add".equals(aqContext.getSubType())) {
            // 保存书籍
            saveBookInfo(data);
        } else {
            // 修改书籍
            updateBookInfo(data);
        }
    }

    private void saveBookInfo(BookInfo data) {
        logger.info("保存书籍");
        doSleep(ThreadLocalRandom.current().nextLong(100));
    }

    private void updateBookInfo(BookInfo data) {
        logger.info("修改书籍");
        doSleep(ThreadLocalRandom.current().nextLong(200));
    }
}
