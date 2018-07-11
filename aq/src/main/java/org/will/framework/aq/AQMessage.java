package org.will.framework.aq;


import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by will on 03/04/2017.
 */
public class AQMessage<PARAMTYPE> implements Serializable, Cloneable {

    private String requestId;
    private PARAMTYPE params;
    private int maxTry;
    private int curTry;
    private long time;
    private Map<String, Object> attachments;
    private String subType;

    public AQMessage(PARAMTYPE params) {
        this.params = params;
    }

    public AQMessage(PARAMTYPE params, Map<String, Object> attachments) {
        this.params = params;
        this.attachments = attachments;
    }

    public AQMessage(PARAMTYPE params, Map<String, Object> attachments, int maxTry) {
        this.params = params;
        this.attachments = attachments;
        this.maxTry = maxTry;
    }

    public AQMessage(String requestId, PARAMTYPE params) {
        this.requestId = requestId;
        this.params = params;
    }

    public AQMessage(String requestId, PARAMTYPE params, Map<String, Object> attachments) {
        this.requestId = requestId;
        this.params = params;
        this.attachments = attachments;
    }

    public AQMessage(String requestId, PARAMTYPE params, int maxTry) {
        this.requestId = requestId;
        this.params = params;
        this.maxTry = maxTry;
    }

    public AQMessage(String requestId, PARAMTYPE params, Map<String, Object> attachments, int maxTry) {
        this.requestId = requestId;
        this.params = params;
        this.attachments = attachments;
        this.maxTry = maxTry;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public PARAMTYPE getParams() {
        return params;
    }

    public void setParams(PARAMTYPE params) {
        this.params = params;
    }

    public int getMaxTry() {
        return maxTry;
    }

    public void setMaxTry(int maxTry) {
        this.maxTry = maxTry;
    }

    public int getCurTry() {
        return curTry;
    }

    public void setCurTry(int curTry) {
        this.curTry = curTry;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Map<String, Object> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, Object> attachments) {
        this.attachments = attachments;
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

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }
}
