package org.will.framework.aq.queue;

import com.qunar.redis.storage.Sedis;

import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 19:27
 */
public class SedisAQQueue implements AQQueue {

    Sedis sedis;

    public SedisAQQueue(Sedis sedis) {
        this.sedis = sedis;
    }

    @Override
    public void enqueue(String topic, byte[] message) {
        sedis.rpush(topic, new String(message, Charset.forName("ISO-8859-1")));
    }

    @Override
    public byte[] dequeue(String topic) {
        return sedis.lpop(topic).getBytes(Charset.forName("ISO-8859-1"));
    }

    @Override
    public long size(String topic) {
        return sedis.llen(topic);
    }

    @Override
    public boolean clear(String topic) {
        return sedis.del(topic) > 0;
    }
}
