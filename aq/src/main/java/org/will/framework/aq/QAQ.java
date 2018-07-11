package org.will.framework.aq;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.will.framework.aq.common.CacheManager;
import org.will.framework.util.FastJsonUtil;

import java.util.Map;

/**
 * Created by will on 03/04/2017.
 */
public class QAQ {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(QAQ.class);
    private static final String RESPONSE_NOT_EXIST = "Response Not Exist";
    private static final int RSEPONSE_INTERVL = 200;
    private static final String AQ_REQUEST_PROFIX = "AQ:REQ:";
    private static final String AQ_RESPONSE_PROFIX = "AQ:RESP:";

    /**
     * 入队
     *
     * @param action 事件类型
     * @param params 参数
     */
    public static void enqueue(final String action, final String params) {
        AQMessage mqRequest = new AQMessage(params);
        enqueue(action, mqRequest);
    }

    /**
     * 入队
     *
     * @param action    事件类型
     * @param requestId 该请求标识
     * @param params    参数
     */
    public static void enqueue(final String action, final String requestId, final String params) {
        AQMessage mqRequest = new AQMessage(requestId, params);
        enqueue(action, mqRequest);
    }

    /**
     * 入队
     *
     * @param action 事件类型
     * @param params 参数
     */
    public static <T> void enqueue(final String action, final T params, final Map<String, Object> attachments) {
        AQMessage mqRequest = new AQMessage(params, attachments);
        enqueue(action, mqRequest);
    }

    /**
     * 入队
     *
     * @param action    事件类型
     * @param requestId 该请求标识
     * @param params    参数
     */
    public static void enqueue(final String action, final String requestId, final String params, final Map<String, Object> attachments) {
        AQMessage mqRequest = new AQMessage(requestId, params, attachments);
        enqueue(action, mqRequest);
    }

    /**
     * 入队
     *
     * @param action 事件类型
     * @param params 参数
     */
    public static void enqueue(final String action, final String params, final int maxTry) {
        AQMessage mqRequest = new AQMessage("", params, maxTry);
        enqueue(action, mqRequest);
    }

    /**
     * 入队
     *
     * @param action 事件类型
     * @param params 参数
     */
    public static <T> void enqueue(final String action, final T params, final Map<String, Object> attachments, final int maxTry) {
        AQMessage mqRequest = new AQMessage(params, attachments, maxTry);
        enqueue(action, mqRequest);
    }

    /**
     * 入队
     *
     * @param action    事件类型
     * @param requestId 该请求标识
     * @param params    参数
     */
    public static void enqueue(final String action, final String requestId, final String params, final int maxTry) {
        AQMessage mqRequest = new AQMessage(requestId, params, maxTry);
        enqueue(action, mqRequest);
    }

    /**
     * 入队
     *
     * @param action 事件类型
     * @param params 参数
     */
    public static void enqueueWithSubType(final String action, final String subType, final String params) {
        AQMessage mqRequest = new AQMessage(params);
        mqRequest.setSubType(subType);
        enqueue(action, mqRequest);
    }

    /**
     * 入队
     *
     * @param action 事件类型
     * @param params 参数
     */
    public static void enqueueWithSubType(final String action, final String subType, final String requestId, final String params) {
        AQMessage mqRequest = new AQMessage(requestId, params);
        mqRequest.setSubType(subType);
        enqueue(action, mqRequest);
    }

    /**
     * 入队
     *
     * @param action    事件类型
     * @param mqRequest 请求体（参数 + 请求标识）
     */
    public static void enqueue(final String action, AQMessage mqRequest) {
        int curTry = mqRequest.getCurTry();
        int maxTry = mqRequest.getMaxTry();
        if (curTry == 0) {
            curTry = 1;

        } else {
            curTry++;
            if (curTry > maxTry) {
                return;
            }
        }
        mqRequest.setCurTry(curTry);
        CacheManager.getCache().rpush(getRequestKey(action), FastJsonUtil.toJson(mqRequest));

        // 记录日志
        //LogCollectManager.common("Consumer <> " + action + " - " + mqRequest.getParams(), "aq", action);

    }

    /**
     * 出队
     *
     * @param action 事件类型
     * @return 参数
     */
    public static AQMessage dequeue(final String action) {
        String paramJson = CacheManager.getCache().lpop(getRequestKey(action));
        if (StringUtils.isEmpty(paramJson)) {
            return null;
        }

//        if (!JsonHelper.isGoodJson(paramJson)) {
//            logger.error("不能正确解析：" + paramJson);
//            return null;
//        }

        try {
            return FastJsonUtil.toObject(paramJson, AQMessage.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;

//        Map<String, Object> paramMap = JsonHelper.convertJsonToMap(paramJson);
//        String requestId = ValueUtil.toString(paramMap.get("requestId"));
//        String params = ValueUtil.toString(paramMap.get("params"));
//        if (StringUtils.isEmpty(requestId) || StringUtils.isEmpty(params)) {
//            logger.error("requestId 或 params 不能为空，paramJson:" + paramJson);
//            return null;
//        }
//
//        AQMessage mqRequest = new AQMessage(requestId, params);
//        mqRequest.setCurTry(ValueUtil.toInt(paramMap.get("curTry")));
//        mqRequest.setMaxTry(ValueUtil.toInt(paramMap.get("maxTry")));
//        mqRequest.setTime(ValueUtil.toLong(paramMap.get("time")));

    }

    public static void clearRequestQueue(final String action) {
        CacheManager.getCache().del(getRequestKey(action));
    }

//    public static void clearResponses(final String action) {
//        Set<String> keys = CacheManager.getCache().keys(FRAMEWORK_MQ_RESPONSE_PROFIX + action + ":*");
//    }

    /**
     * 队列长度
     *
     * @param action 事件类型
     * @return 参数
     */
    public static long queueLength(final String action) {
        return CacheManager.getCache().llen(getRequestKey(action));
    }

    /**
     * 设置应答
     *
     * @param action     事件类型
     * @param mqResponse 应答体
     */
    public static void setResponse(final String action, AQResponse mqResponse) {
        setResponse(action, mqResponse.getRequestId(), mqResponse.getResult(), 30 * 60 * 1000);
    }

    /**
     * 设置应答
     *
     * @param action     事件类型
     * @param mqResponse 应答体
     * @param timeout    超时，单位秒
     */
    public static void setResponse(final String action, AQResponse mqResponse, final int timeout) {
        setResponse(action, mqResponse.getRequestId(), mqResponse.getResult(), timeout);
    }

    /**
     * 设置应答
     *
     * @param action    事件类型
     * @param requestId 该请求标识
     * @param result    应答
     */
    public static void setResponse(final String action, final String requestId, final String result) {
        setResponse(action, requestId, result, 30 * 60 * 1000);
    }

    /**
     * 设置应答
     *
     * @param action    事件类型
     * @param requestId 该请求标识
     * @param result    应答
     * @param timeout   超时，单位秒
     */
    public static void setResponse(final String action, final String requestId, final String result, final int timeout) {
        CacheManager.getCache().set(getResponseKey(action, requestId), result, "NX", "EX", timeout);
    }

    /**
     * 获取应答
     *
     * @param action    事件类型
     * @param requestId 该请求标识
     * @return 应答
     */
    private static String getResponse(final String action, final String requestId) {
        String responseKey = getResponseKey(action, requestId);
        return getResponse(responseKey);
    }

    /**
     * 获取应答
     *
     * @param responseKey 应答标识
     * @return 应答
     */
    private static String getResponse(final String responseKey) {
        if (CacheManager.getCache().exists(responseKey)) {
            return CacheManager.getCache().get(responseKey);
        }
        return RESPONSE_NOT_EXIST;
    }

    /**
     * 获取结果
     *
     * @param action
     * @param requestId
     * @return
     */
    public static String getResult(final String action, final String requestId) {
        return getResultWithCount(action, requestId, 1);
    }

    /**
     * 获取结果
     *
     * @param responseKey
     * @return
     */
    private static String getResult(final String responseKey) {
        return getResultWithCount(responseKey, 1);
    }

    /**
     * 获取结果
     *
     * @param action
     * @param requestId
     * @param tryCount  重试次数
     * @return
     */
    public static String getResultWithCount(final String action, final String requestId, final int tryCount) {
        String responseKey = getResponseKey(action, requestId);
        return getResultWithCount(responseKey, tryCount);
    }

    /**
     * 获取结果
     *
     * @param responseKey
     * @param tryCount    重试次数
     * @return
     */
    private static String getResultWithCount(final String responseKey, final int tryCount) {
        if (tryCount <= 0) {
            logger.error("QAQ <> Error responseKey:" + responseKey + ", count:" + tryCount);
            return null;
        }

        String response = getResponse(responseKey);
        if (RESPONSE_NOT_EXIST.equals(response)) {
            int countTemp = tryCount - 1;
            if (countTemp <= 0) {
                return null;
            }

            doSleep(RSEPONSE_INTERVL);

            return getResultWithCount(responseKey, countTemp);
        }
        return response;
    }

    /**
     * 获取结果
     *
     * @param action
     * @param requestId
     * @param timeoutMS 有效时间
     * @return
     */
    public static String getResultWithTimeout(final String action, final String requestId, final long timeoutMS) {
        String responseKey = getResponseKey(action, requestId);
        return getResultWithTimeout(responseKey, timeoutMS);
    }

    /**
     * 获取结果
     *
     * @param responseKey
     * @param timeoutMS   有效时间
     * @return
     */
    private static String getResultWithTimeout(final String responseKey, final long timeoutMS) {
        if (timeoutMS <= 0) {
            logger.error("QAQ <> Error responseKey:" + responseKey + ", timeoutMS:" + timeoutMS);
            return null;
        }

        long curMillis = System.currentTimeMillis();
        String response = getResponse(responseKey);
        if (RESPONSE_NOT_EXIST.equals(response)) {
            long timeoutMSTemp = timeoutMS - (System.currentTimeMillis() - curMillis);
            if (timeoutMSTemp <= 0) {
                return null;
            }

            doSleep(RSEPONSE_INTERVL);

            timeoutMSTemp = timeoutMS - (System.currentTimeMillis() - curMillis);
            return getResultWithTimeout(responseKey, timeoutMSTemp);
        }
        return response;
    }

    private static String getRequestKey(final String action) {
        return AQ_REQUEST_PROFIX + action;
    }

    private static String getResponseKey(final String action, final String requestId) {
        return AQ_RESPONSE_PROFIX + action + ":" + requestId;
    }

    private static String getRequestTryCount(final String action, final String requestId, final String param) {
        return AQ_REQUEST_PROFIX + action + ":" + requestId + ":" + param;
    }


    /**
     * Thread.sleep 的封装
     *
     * @param millis
     */
    protected static void doSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    public static void main(String[] args) {
        String result = "";

        result = getResult("123", "456");
        logger.info("result:" + result);

        result = getResultWithTimeout("123", "456", 200);
        logger.info("result:" + result);

        result = getResultWithCount("123", "456", 5);
        logger.info("result:" + result);
    }
}
