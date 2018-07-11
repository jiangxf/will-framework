//package com.intime.soa.framework.distributed.aq;
//
//import com.intime.soa.framework.distributed.lock.redis.RedisReentrantLock;
//import com.intime.soa.framework.util.ValueUtil;
//import com.intime.soa.framework.util.json.JsonHelper;
//import com.intime.soa.framework.validation.ValidateManager;
//import org.apache.commons.collections.map.HashedMap;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
///**
// * Created by will on 02/12/2016.
// */
////@Component
//public class DemoAQConsumer extends AQConsumer {
//
////    @Autowired
////    private DataLinkService dataLinkService;
//
//    @Override
//    protected void initProperies() {
//        this.action = "Demo";
//        this.maxThread.set(1);
//        this.minThread.set(0);
//    }
//
//    @Override
//    protected AQProcessor createAQProcessor(String threadName) {
//        return new DemoAQProcessor(threadName, this);
//    }
//
//    class DemoAQProcessor extends AQSingleProcessor {
//        public DemoAQProcessor(String threadName, AQConsumer threadLoader) {
//            super(threadName, threadLoader);
//        }
//
//        @Override
//        protected AQResponse doRequest(AQMessage aqRequest) {
//            String requestId = aqRequest.getRequestId();
//            String params = aqRequest.getParams();
//            if (!JsonHelper.isGoodJson(params)) {
//                logger.error("请求参数错误：" + params);
//                return null;
//            }
//
//            Map<String, Object> paramMap = JsonHelper.convertJsonToMap(params);
//            List<String> validates = ValidateManager.checkNotEmpty("name, idcard", "参数错误").checkAndReturn(paramMap);
//            if (validates != null && validates.size() > 0) {
//                logger.error("请求参数错误：" + params);
//                return null;
//            }
//
//            boolean result = false;
//            String name = ValueUtil.toString(paramMap.get("name"));
//            String idcard = ValueUtil.toString(paramMap.get("idcard"));
//            RedisReentrantLock lock = new RedisReentrantLock(name + "_" + idcard);
//            try {
//                if (lock.tryLock(5000L, TimeUnit.MILLISECONDS)) {
//                    long curMillis = System.currentTimeMillis();
//                    logger.info("   begin millis:" + curMillis);
////                    result = dataLinkService.validateIdcard(name, idcard);
//                    long millisTemp = System.currentTimeMillis() - curMillis;
//                    logger.info("interval millis:" + millisTemp);
//                } else {
//                    //获取锁失败
//                }
//            } catch (InterruptedException e) {
//                logger.error(e.getMessage());
//            } finally {
//                lock.unlock();
//            }
//
//            Map<String, Object> resultMap = new HashedMap();
//            resultMap.put("result", result);
//            AQResponse aqResponse = new AQResponse(requestId, JsonHelper.toJson(resultMap));
//            return aqResponse;
//        }
//    }
//}
