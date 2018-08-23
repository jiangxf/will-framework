package org.will.framework.aq.common;

import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-08-23
 * Time: 15:45
 */
public class AQContext {

    // 消息主键
    private String messageId;

    // 已经消费失败的次数
    private int curFailTime;

    // 附件，可以透传 traceId 等信息
    private Map<String, Object> attachments;

    // 用于消息场景的细分
    private String subType;

    // 消息分类
    private String topic;

    // 发送时间
    private long sendTimestamp;

    public AQContext(AQMessage aqMessage) {
        this.setMessageId(aqMessage.getMessageId());
        this.setTopic(aqMessage.getTopic());
        this.setSubType(aqMessage.getSubType());
        this.setAttachments(aqMessage.getAttachments());
        this.setCurFailTime(aqMessage.getCurFailTime());
        this.setSendTimestamp(aqMessage.getSendTimestamp());
    }

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

    @Override
    public String toString() {
        return "AQContext{" +
                "messageId='" + messageId + '\'' +
                ", curFailTime=" + curFailTime +
                ", attachments=" + attachments +
                ", subType='" + subType + '\'' +
                ", topic='" + topic + '\'' +
                ", sendTimestamp=" + sendTimestamp +
                '}';
    }
}
