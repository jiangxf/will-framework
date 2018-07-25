//package org.will.framework.aq;
//
//
//import org.will.framework.util.FastJsonUtil;
//
//import java.util.Map;
//
///**
// * Created by will on 02/12/2016.
// */
////@Component
//public class DemoAQConsumerWrapper extends AQConsumerWrapper<String> {
//
//    @Override
//    protected void initProperies() {
//        this.action = "DemoWrapper";
//        this.maxThread.set(1);
//        this.minThread.set(0);
//    }
//
//    @Override
//    protected AQResponse doRequest(AQMessage<String> aqRequest) {
//        String requestId = aqRequest.getRequestId();
//        String params = aqRequest.getParams();
//        if (!FastJsonUtil.isGoodJson(params)) {
//            logger.error("请求参数错误：" + params);
//            return null;
//        }
//
//        //do something here
//        Map<String, Object> paramMap = FastJsonUtil.toMap(params);
//        //xxxService.doSomething(paramMap);
//        return null;
//
//        //return aqResponse here
//        //Map<String, Object> resultMap = new HashedMap();
//        //resultMap.put("result", null);
//        //AQResponse aqResponse = new AQResponse(requestId, JsonHelper.toJson(resultMap));
//        //return aqResponse;
//    }
//}
