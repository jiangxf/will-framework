package org.will.framework.aq;

import org.will.framework.aq.consumer.AQConsumer;

import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-08-23
 * Time: 15:45
 */
public class AQContext {

    private String messageId;
    private int curFailTime;
    private Map<String, Object> attachments;
    private String subType;
    private String topic;
    private long sendTimestamp;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getCurFailTime() {
        return curFailTime;
    }

    public void setCurFailTime(int curFailTime) {
        this.curFailTime = curFailTime;
    }

    public Map<String, Object> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, Object> attachments) {
        this.attachments = attachments;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getSendTimestamp() {
        return sendTimestamp;
    }

    public void setSendTimestamp(long sendTimestamp) {
        this.sendTimestamp = sendTimestamp;
    }

    public AQContext(AQMessage aqMessage){
        this.setMessageId(aqMessage.getMessageId());
        this.setTopic(aqMessage.getTopic());
        this.setSubType(aqMessage.getSubType());
        this.setAttachments(aqMessage.getAttachments());
        this.setCurFailTime(aqMessage.getCurFailTime());
        this.setSendTimestamp(aqMessage.getSendTimestamp());
    }
}
