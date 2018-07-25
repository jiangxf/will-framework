package org.will.framework.aq.queue;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 16:30
 */
public interface AQQueue {

    void enqueue(final String topic, byte[] message);

    byte[] dequeue(final String topic);

    long size(final String topic);

    boolean clear(final String topic);
}
