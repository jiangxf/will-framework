package org.will.framework.aq;


import java.util.HashMap;

/**
 * Created by will on 03/04/2017.
 */
public class AQResponse extends HashMap {

    public AQResponse(String requestId, String result) {
        this.put("requestId", requestId);
        this.put("result", result);
    }

    public String getRequestId() {
        return String.valueOf(this.get("requestId"));
    }

    public void setRequestId(String requestId) {
        this.put("requestId", requestId);
    }

    public String getResult() {
        return String.valueOf(this.get("result"));
    }

    public void setResult(String result) {
        this.put("result", result);
    }
}
