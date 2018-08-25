package org.will.framework.aq.queue;

import org.will.framework.aq.common.AQMessage;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 16:30
 */
public interface AQQueue {

    void enqueue(final String topic, AQMessage message);

    AQMessage dequeue(final String topic);

    long size(final String topic);

    boolean clear(final String topic);
}
