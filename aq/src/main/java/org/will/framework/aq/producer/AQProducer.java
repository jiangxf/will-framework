package org.will.framework.aq.producer;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.will.framework.aq.AQMessage;
import org.will.framework.aq.queue.AQQueue;
import org.will.framework.util.ProtoStuffUtil;

import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 15:54
 */
public class AQProducer {

    public AQProducer() {
        loadConfig(new AQProducerConfig());
    }

    public AQProducer(AQProducerConfig config) {
        loadConfig(config);
    }

    public void send(AQMessage aqMessage) {
        rateLimiter.acquire();

        Throwable throwable = null;
        try {
            beforeSend(aqMessage);
            doSend(aqMessage);
        } catch (Exception ex) {
            throwable = ex;
            logger.error(ex.getMessage());
        }
        afterSend(aqMessage, throwable);
    }

    public void sendAsyn(AQMessage aqMessage) {

    }

    public String sendForResponse(AQMessage aqMessage) {
        String messageId = UUID.randomUUID().toString();
        aqMessage.setMessageId(messageId);
        send(aqMessage);
        return aqMessage.getMessageId();
    }

    public void clear(String topic) {
        aqQueue.clear(topic);
    }

    public long remainCapacity(String topic) {
        return capacity - aqQueue.size(topic);
    }

    public long usedCapacity(String topic) {
        return aqQueue.size(topic);
    }

    private void loadConfig(AQProducerConfig config) {
        rateLimiter = RateLimiter.create(config.qps);
        capacity = config.getCapacity();
        retries = config.getRetries();
        timeoutMS = config.getTimeoutMS();
    }

    private void beforeSend(AQMessage aqMessage) {
        if (aqMessage == null || StringUtils.isEmpty(aqMessage.getTopic()) || aqMessage.getData() == null) {
            throw new IllegalArgumentException("消息类型与消息体不能为空!");
        }

        int curTries = 1;
        int sleepMS = timeoutMS / retries;
        while (remainCapacity(aqMessage.getTopic()) <= 0 && curTries++ < retries) {
            try {
                Thread.sleep(sleepMS);
            } catch (InterruptedException e) {
            }

            if (curTries == retries) {
                throw new RuntimeException("消息发送失败，队列容量不够");
            }
        }

        if (producerListeners != null) {
            for (AQProducerListener listener : producerListeners) {
                listener.beforeSend(aqMessage);
            }
        }
    }

    private void doSend(AQMessage aqMessage) {
        byte[] bytes = ProtoStuffUtil.serializer(aqMessage);
        aqQueue.enqueue(aqMessage.getTopic(), bytes);
    }

    private void afterSend(AQMessage aqMessage, Throwable throwable) {
        if (producerListeners != null) {
            for (AQProducerListener listener : producerListeners) {
                listener.afterSend(aqMessage, throwable);
            }
        }
    }

    protected AQQueue aqQueue;

    protected RateLimiter rateLimiter;

    protected long capacity;

    protected int retries;

    protected int timeoutMS;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<AQProducerListener> producerListeners;

    public List<AQProducerListener> getProducerListeners() {
        return producerListeners;
    }

    public void setProducerListeners(List<AQProducerListener> producerListeners) {
        this.producerListeners = producerListeners;
    }

    public AQQueue getAqQueue() {
        return aqQueue;
    }

    public void setAqQueue(AQQueue aqQueue) {
        this.aqQueue = aqQueue;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public int getTimeoutMS() {
        return timeoutMS;
    }

    public void setTimeoutMS(int timeoutMS) {
        this.timeoutMS = timeoutMS;
    }
}
