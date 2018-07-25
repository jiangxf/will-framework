package org.will.framework.aq.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static org.will.framework.aq.AQConstants.DEFAULT_CAPACITY;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 16:35
 */
public class LocalAQQueue implements AQQueue {

    @Override
    public void enqueue(String topic, byte[] message) {
        getQueue(topic).offer(message);
    }

    @Override
    public byte[] dequeue(String topic) {
        return getQueue(topic).poll();
    }

    @Override
    public long size(String topic) {
        return DEFAULT_CAPACITY - getQueue(topic).remainingCapacity();
    }

    @Override
    public boolean clear(String topic) {
        getQueue(topic).clear();
        return true;
    }

    private LinkedBlockingQueue<byte[]> getQueue(String topic) {
        LinkedBlockingQueue queue = LOCAL_QUEUE_MAP.get(topic);
        if (queue == null) {
            synchronized (LocalAQQueue.class.getName() + topic) {
                queue = LOCAL_QUEUE_MAP.get(topic);
                if (queue == null) {
                    queue = new LinkedBlockingQueue(DEFAULT_CAPACITY);
                    LOCAL_QUEUE_MAP.put(topic, queue);
                }
            }
        }
        return queue;
    }

    ArrayBlockingQueue queue;

    private static final ConcurrentHashMap<String, LinkedBlockingQueue<byte[]>> LOCAL_QUEUE_MAP = new ConcurrentHashMap<>();
}
