package org.will.framework.aq.producer;

import static org.will.framework.aq.AQConstants.DEFAULT_CAPACITY;
import static org.will.framework.aq.AQConstants.DEFAULT_QPS;
import static org.will.framework.aq.AQConstants.DEFAULT_TIMEOUT_MS;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-15
 * Time: 21:09
 */
public class AQProducerConfig {

//    /**
//     * 该值控制生产者批量发送消息的大小，批量发送可以减少生产者到服务端的请求数，有助于提高客户端和服务端的性能。
//     */
//    protected int batchSize = 5;
//
//    /**
//     * 默认情况下缓冲区的消息会被立即发送到服务端，即使缓冲区的空间并没有被用完。
//     * 可以将该值设置为大于0的值，这样发送者将等待一段时间后，再向服务端发送请求，以实现每次请求可以尽可能多的发送批量消息。
//     * batch.size和linger.ms是两种实现让客户端每次请求尽可能多的发送消息的机制，它们可以并存使用，并不冲突。
//     */
//    protected int lingerMS = 2000;

    /**
     * 队列总长度
     */
    protected int capacity = DEFAULT_CAPACITY;

    /**
     * 发送超时时间
     */
    protected int sendTimeoutMS = DEFAULT_TIMEOUT_MS;

    /**
     * 每秒发送消息数量
     */
    protected double qps = DEFAULT_QPS;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getSendTimeoutMS() {
        return sendTimeoutMS;
    }

    public void setSendTimeoutMS(int sendTimeoutMS) {
        this.sendTimeoutMS = sendTimeoutMS;
    }

    public double getQps() {
        return qps;
    }

    public void setQps(double qps) {
        this.qps = qps;
    }
}
