package org.will.framework.aq;


import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by will on 03/04/2017.
 */
public class AQMessage implements Serializable, Cloneable {

    private String messageId;
    private Object data;

    private int curFailTime;
    private long sendTimestamp;
    private long offTimestamp;
    private Map<String, Object> attachments;
    private String subType;
    private String topic;

    public AQMessage(String topic, Object data) {
        this.topic = topic;
        this.data = data;
    }

    public void setAttachment(String key, Object value) {
        if (attachments == null) {
            attachments = Maps.newHashMap();
        }

        this.attachments.put(key, value);
    }

    public Object setAttachmentIfNotExist(String key, Object value) {
        if (attachments == null) {
            attachments = Maps.newHashMap();
        }

        if (this.attachments.containsKey(key)) {
            return this.attachments.get(key);
        }

        this.attachments.put(key, value);
        return value;
    }

    public Object removeAttachment(String key, Object value) {
        if (attachments == null) {
            return null;
        }

        return this.attachments.remove(key);
    }

    public boolean containAttachment(String key) {
        if (attachments == null) {
            return false;
        }

        return this.attachments.containsKey(key);
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

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getCurFailTime() {
        return curFailTime;
    }

    public void setCurFailTime(int curFailTime) {
        this.curFailTime = curFailTime;
    }

    public long getSendTimestamp() {
        return sendTimestamp;
    }

    public void setSendTimestamp(long sendTimestamp) {
        this.sendTimestamp = sendTimestamp;
    }

    public long getOffTimestamp() {
        return offTimestamp;
    }

    public void setOffTimestamp(long offTimestamp) {
        this.offTimestamp = offTimestamp;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return "AQMessage{" +
                "messageId='" + messageId + '\'' +
                ", data=" + data +
                ", curFailTime=" + curFailTime +
                ", sendTimestamp=" + sendTimestamp +
                ", offTimestamp=" + offTimestamp +
                ", attachments=" + attachments +
                ", subType='" + subType + '\'' +
                ", topic='" + topic + '\'' +
                '}';
    }
}
