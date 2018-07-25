//package org.will.framework.aq;
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.will.framework.common.spring.AbstractContextLoadListener;
//
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * Created by will on 02/12/2016.
// */
//public abstract class AQConsumer extends AbstractContextLoadListener {
//    protected final Logger logger = LoggerFactory.getLogger(AQConsumer.this.getClass());
//    protected final ConcurrentMap<String, AQThread> aqProcessorMap = new ConcurrentHashMap<>();
//    protected AQScheduler aqScheduler = null;
//    protected ConcurrentLinkedQueue<Long> intervals = new ConcurrentLinkedQueue<>();
//    protected String action = null;
//    protected AtomicInteger maxThread = new AtomicInteger(1);
//    protected AtomicInteger minThread = new AtomicInteger(0);
//    protected AtomicInteger needThread = new AtomicInteger(1);
//    protected AtomicInteger parallelCount = new AtomicInteger(30);
//    protected AtomicInteger intervalAVG = new AtomicInteger(10);
//    protected AtomicInteger qpsAvg = new AtomicInteger(0);
//    protected AtomicInteger shedulerWaitS = new AtomicInteger(3);
//
//    @Override
//    protected void afterAppContextLoad() {
//        init();
//    }
//
//    @Override
//    protected void afterWebContextLoad() {
//    }
//
//    private final void init() {
//        initProperies();
//
//        loadaqScheduler();
//        AQMonitor.addAQConsumer(this);
//    }
//
//    public final void loadAQProcessThreads() {
//        if (StringUtils.isEmpty(action)) {
//            throw new IllegalArgumentException("type 不能为空");
//        }
//
//        Set<String> keySet = aqProcessorMap.keySet();
//        for (String name : keySet) {
//            if (!aqProcessorMap.get(name).isAlive()) {
//                aqProcessorMap.remove(name);
//            }
//        }
//
//        int size = aqProcessorMap.size();
//        if (size >= needThread.get()) {
//            return;
//        }
//
//        int newNeed = needThread.get() - size;
//        for (int i = 0; i < maxThread.get(); i++) {
//            String threadName = action.trim() + "ProcessThread-" + i;
//            if (aqProcessorMap.containsKey(threadName)) {
//                continue;
//            }
//
//            initAQProcessThread(threadName);
//            if (--newNeed <= 0) {
//                break;
//            }
//        }
//    }
//
//    private final void initAQProcessThread(String threadName) {
//        if (StringUtils.isEmpty(action)) {
//            throw new IllegalArgumentException("type 不能为空");
//        }
//
//        AQProcessor aqProcessor = createAQProcessor(threadName);
//        aqProcessor.start();
//        logger.info("创建线程：" + threadName);
//        aqProcessorMap.put(threadName, aqProcessor);
//    }
//
//    public final void loadaqScheduler() {
//        if (StringUtils.isEmpty(action)) {
//            throw new IllegalArgumentException("type 不能为空");
//        }
//
//        if (aqScheduler == null || !aqScheduler.isAlive()) {
//            String schedulerName = action.trim() + "SchedulerThread";
//            aqScheduler = new AQScheduler(schedulerName, this);
//            aqScheduler.start();
//            logger.info("创建线程：" + schedulerName);
//        }
//    }
//
//    public final int computeParallelCount() {
//        if (intervals.size() <= 0) {
//            qpsAvg.set(0);
//            return parallelCount.get();
//        }
//
//        int size = intervals.size();
//        qpsAvg.set(size / shedulerWaitS.get());
//        if (size > 20) {
//            size = 20;
//        }
//
//        int result = 0;
//        for (int i = 0; i < size; i++) {
//            result += intervals.poll();
//        }
//        intervals.clear();
//
//        int intervalTemp = result / size;
//        intervalAVG.set(intervalTemp);
//
//        result = 1000 / intervalTemp;
//        if (result < 1) {
//            result = 1;
//        }
//        return result;
//    }
//
//    protected abstract void initProperies();
//
//    protected abstract AQProcessor createAQProcessor(String threadName);
//
//    public final void processorsMonitor() {
//        loadAQProcessThreads();
//    }
//
//    public final void schedulerMonitor() {
//        loadaqScheduler();
//    }
//
//    public ConcurrentMap<String, AQThread> getAQProcessorMap() {
//        return aqProcessorMap;
//    }
//
//    public String getAction() {
//        return action;
//    }
//
//    public AtomicInteger getMaxThread() {
//        return maxThread;
//    }
//
//    public AtomicInteger getMinThread() {
//        return minThread;
//    }
//
//    public AtomicInteger getNeedThread() {
//        return needThread;
//    }
//
//    public void setNeedThread(int needThread) {
//        this.needThread.set(needThread);
//    }
//
//    public AtomicInteger getParallelCount() {
//        return parallelCount;
//    }
//
//    public void setParallelCount(AtomicInteger parallelCount) {
//        this.parallelCount = parallelCount;
//    }
//
//    public ConcurrentLinkedQueue<Long> getIntervals() {
//        return intervals;
//    }
//
//    public void setIntervals(ConcurrentLinkedQueue<Long> intervals) {
//        this.intervals = intervals;
//    }
//
//    public AtomicInteger getIntervalAVG() {
//        return intervalAVG;
//    }
//
//    public AtomicInteger getShedulerWaitS() {
//        return shedulerWaitS;
//    }
//
//    public void setShedulerWaitS(AtomicInteger shedulerWaitS) {
//        this.shedulerWaitS = shedulerWaitS;
//    }
//
//    public AtomicInteger getQpsAvg() {
//        return qpsAvg;
//    }
//
//    public void setCountAvg(AtomicInteger qpsAvg) {
//        this.qpsAvg = qpsAvg;
//    }
//
//    class AQScheduler extends AQThread {
//        public AQScheduler(String schedulerName, AQConsumer aqConsumer) {
//            super(schedulerName, aqConsumer);
//        }
//
//        @Override
//        protected void doRun() {
//            while (true) {
//                int real = 0;
//                int parallelCount = aqConsumer.computeParallelCount();
//                long queueLength = QAQ.queueLength(aqConsumer.getAction());
//                if (queueLength <= 0) {
//                    aqConsumer.setNeedThread(aqConsumer.getMinThread().get());
//                } else {
//                    int need = (int) ((queueLength + parallelCount + aqConsumer.getQpsAvg().get()) / parallelCount);
//                    real = need;
//                    if (need >= aqConsumer.getMaxThread().get()) {
//                        need = aqConsumer.getMaxThread().get();
//                    } else if (need <= aqConsumer.getMinThread().get()) {
//                        need = aqConsumer.getMinThread().get();
//                    }
//                    aqConsumer.setNeedThread(need);
//                }
//                aqConsumer.loadAQProcessThreads();
//
//                if (queueLength > 0) {
//                    String logInfo = "AQScheduler <> "
//                            + String.format("action:%s, remain:%s, countAVG:%s, intervalAVG:%s, parallel:%s, running:%s, used:%s, need:%s",
//                            aqConsumer.getAction(), queueLength, aqConsumer.getQpsAvg().get(), aqConsumer.getIntervalAVG().get(),
//                            parallelCount, aqConsumer.getAQProcessorMap().size(), aqConsumer.getNeedThread().get(), real
//                    );
//                    logger.info(logInfo);
//                    //LogCollectManager.cache(logInfo, "aq", aqConsumer.getAction());
//                }
//
//                doSleep(aqConsumer.getShedulerWaitS().get() * 1000);
//            }
//        }
//    }
//}
