package org.will.framework.aq.producer;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.will.framework.aq.common.AQMessage;
import org.will.framework.aq.queue.AQQueue;
import org.will.framework.util.IdWorker;

import java.util.List;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 15:54
 */
public class AQProducer {

    protected final AQQueue aqQueue;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected RateLimiter rateLimiter;
    protected long capacity;
    protected int sendTimeoutMS;
    private List<AQProducerListener> producerListeners;

    public AQProducer(AQQueue aqQueue) {
        this(new AQProducerConfig(), aqQueue);
    }

    public AQProducer(AQProducerConfig config, AQQueue aqQueue) {
        loadConfig(config);
        this.aqQueue = aqQueue;
    }

    /**
     * 发送消息主方法
     *
     * @param aqMessage
     * @return
     */
    public String send(AQMessage aqMessage) {

        // 限流
        rateLimiter.acquire();

        // 初始化消息
        String messageId = initAndCheckMessage(aqMessage);

        // 消息发送前处理
        beforeSend(aqMessage);

        // 发送消息
        Throwable throwable = null;
        try {
            doSend(aqMessage);
        } catch (Exception ex) {
            throwable = ex;
            logger.error(ex.getMessage());
        }

        // 消息发送后处理
        afterSend(aqMessage, throwable);

        return messageId;
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

    protected final void doSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 检查并初始化消息
     *
     * @param aqMessage
     * @return
     */
    private String initAndCheckMessage(AQMessage aqMessage) {
        if (aqMessage == null || StringUtils.isEmpty(aqMessage.getTopic()) || aqMessage.getData() == null) {
            throw new IllegalArgumentException("消息类型与消息体不能为空!");
        }

        if (aqMessage.getSendTimestamp() <= 0) {
            aqMessage.setSendTimestamp(System.currentTimeMillis());
        }

        if (StringUtils.isEmpty(aqMessage.getMessageId())) {
            String messageId = IdWorker.getUUID();
            aqMessage.setMessageId(messageId);
        }
        return aqMessage.getMessageId();
    }

    private void beforeSend(AQMessage aqMessage) {
        if (producerListeners != null) {
            for (AQProducerListener listener : producerListeners) {
                listener.beforeSend(aqMessage);
            }
        }
    }

    private synchronized void doSend(AQMessage aqMessage) {
        // 检查队列是否已满
        int eachWaitMS = 200;
        int totalWaitMS = sendTimeoutMS;
        while (remainCapacity(aqMessage.getTopic()) <= 0) {
            if (totalWaitMS <= 0) {
                logger.warn("队列已满，丢弃 {}", aqMessage.getMessageId());
                return;
            }

            doSleep(eachWaitMS);
            totalWaitMS -= eachWaitMS;
        }
        aqQueue.enqueue(aqMessage.getTopic(), aqMessage);
    }

    private void afterSend(AQMessage aqMessage, Throwable throwable) {
        if (producerListeners != null) {
            for (AQProducerListener listener : producerListeners) {
                listener.afterSend(aqMessage, throwable);
            }
        }
    }

    /**
     * 加载配置文件
     *
     * @param config
     */
    private void loadConfig(AQProducerConfig config) {
        rateLimiter = RateLimiter.create(config.qps);
        capacity = config.getCapacity();
        sendTimeoutMS = config.getSendTimeoutMS();
    }

    public List<AQProducerListener> getProducerListeners() {
        return producerListeners;
    }

    public void setProducerListeners(List<AQProducerListener> producerListeners) {
        this.producerListeners = producerListeners;
    }

    public AQQueue getAqQueue() {
        return aqQueue;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }
}
