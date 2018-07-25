package org.will.framework.aq.consumer;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.will.framework.aq.AQMessage;
import org.will.framework.aq.AQResponse;
import org.will.framework.aq.queue.AQQueue;
import org.will.framework.util.ProtoStuffUtil;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.will.framework.aq.AQConstants.*;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-17
 * Time: 21:01
 */
public abstract class AQConsumer {

    public AQConsumer(String topic, AQQueue aqQueue) {
        this.topic = topic;
        this.maxWorkerThread = MAX_WORKER_THREAD;
        this.minWorkerThread = MIN_WORKER_THREAD;
        this.aqQueue = aqQueue;
        this.rateLimiter = RateLimiter.create(DEFAULT_QPS);
        this.workerThreadMap = Maps.newConcurrentMap();
        elapseQueue = new ConcurrentLinkedQueue<>();
    }

    public AQConsumer(String topic, int maxThread, int minThread, AQQueue aqQueue) {
        this.topic = topic;
        this.maxWorkerThread = maxThread;
        this.minWorkerThread = minThread;
        this.aqQueue = aqQueue;
        this.rateLimiter = RateLimiter.create(DEFAULT_QPS);
        this.workerThreadMap = Maps.newConcurrentMap();
        elapseQueue = new ConcurrentLinkedQueue<>();
    }

    public void init() {
        loadAQWatcher();
        loadAQWorkers();
    }

    public final synchronized void loadAQWatcher() {
        if (StringUtils.isEmpty(topic)) {
            throw new IllegalArgumentException("type 不能为空");
        }

        if (aqWatcher == null || !aqWatcher.isAlive()) {
            String watcherName = "AQWatcher-Thread-" + topic.trim();
            aqWatcher = new AQWatcher(watcherName, this);
            aqWatcher.start();
        }
    }

    public final synchronized void loadAQWorkers() {
        // todo: 参数校验考虑放在合适的位置
        if (StringUtils.isEmpty(topic)) {
            throw new IllegalArgumentException("topic 不能为空");
        }

        // 移除已消亡的工作线程
        Set<String> keySet = workerThreadMap.keySet();
        for (String name : keySet) {
            if (!workerThreadMap.get(name).isAlive()) {
                workerThreadMap.remove(name);
            }
        }

        // 检查存活线程数是否满足当前消息的消费能力
        int size = workerThreadMap.size();
        if (size >= needWorkThreadAtoc.get()) {
            return;
        }

        // 按照顺序创建线程
        int newNeed = needWorkThreadAtoc.get() - size;
        for (int i = 0; i < maxWorkerThread; i++) {
            String threadName = "AQWorker-Thread-" + topic.trim() + "-" + i;
            if (workerThreadMap.containsKey(threadName)) {
                continue;
            }

            startAQWorker(threadName);
            if (--newNeed <= 0) {
                break;
            }
        }
    }

    public final int computeParallelCount() {
        if (elapseQueue.size() <= 0) {
            qpsAvg.set(0);
            return 30;
        }

        int size = elapseQueue.size();
        qpsAvg.set(size / watcherCycleSec);
        if (size > 30) {
            size = 30;
        }

        double result = 0;
        for (int i = 0; i < size; i++) {
            result += elapseQueue.poll();
        }
        elapseQueue.clear();

        double elapseAvgTemp = result / size;
        if(elapseAvgTemp < 0.01){
            elapseAvgTemp = 0.01;
        }
        elapseAvg.set(elapseAvgTemp);

        double parallel = 1000 / elapseAvgTemp;
        if (parallel < 1) {
            parallel = 1;
        }
        return new BigDecimal(parallel).intValue();
    }

    public AQMessage recv() {
        if (aqQueue.size(topic) > 0) {
            byte[] bytes = aqQueue.dequeue(topic);
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            return ProtoStuffUtil.deserializer(bytes, AQMessage.class);
        }
        return null;
    }

    public long size() {
        return aqQueue.size(topic);
    }

    public void acquire() {
        rateLimiter.acquire();
    }

    protected AQWorker createAQWorker(String threadName) {
        return new AQWorker(threadName, this) {
            @Override
            protected AQResponse processMessage(AQMessage aqMessage) {
//                logger.info("消费消息：" + aqMessage.getMessageId());
                doProcessMessage(aqMessage);
                return null;
            }
        };
    }

    protected abstract AQResponse doProcessMessage(AQMessage aqRequest);

    private final void startAQWorker(String threadName) {
        AQWorker aqWorker = createAQWorker(threadName);
        workerThreadMap.put(threadName, aqWorker);

        aqWorker.start();
    }

    protected ConcurrentLinkedQueue<Long> elapseQueue;

    protected AQWatcher aqWatcher = null;

    protected AtomicInteger parallelCount = new AtomicInteger(0);

    protected AtomicDouble elapseAvg = new AtomicDouble(0);

    protected AtomicInteger qpsAvg = new AtomicInteger(0);

    protected AQQueue aqQueue;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final String topic;

    protected final int maxWorkerThread;

    protected final int minWorkerThread;

    protected final RateLimiter rateLimiter;

    protected final ConcurrentMap<String, AQWorker> workerThreadMap;

    protected final AtomicInteger needWorkThreadAtoc = new AtomicInteger(0);

    protected final int watcherCycleSec = 3;

    public AQQueue getAqQueue() {
        return aqQueue;
    }

    public void setAqQueue(AQQueue aqQueue) {
        this.aqQueue = aqQueue;
    }

    public String getTopic() {
        return topic;
    }

    public int getNeedThread() {
        return needWorkThreadAtoc.get();
    }

    public void setNeedThread(int needThread) {
        this.needWorkThreadAtoc.set(needThread);
    }

    public ConcurrentMap<String, AQWorker> getWorkerThreadMap() {
        return workerThreadMap;
    }

    public int getMaxWorkerThread() {
        return maxWorkerThread;
    }

    public int getMinWorkerThread() {
        return minWorkerThread;
    }

    public ConcurrentLinkedQueue<Long> getElapseQueue() {
        return elapseQueue;
    }

    public int getParallelCount() {
        return parallelCount.get();
    }

    public int getQpsAvg() {
        return qpsAvg.get();
    }

    public int getWatcherCycleSec() {
        return watcherCycleSec;
    }

    public AQWatcher getAqWatcher() {
        return aqWatcher;
    }

    public long getElapseAvg() {
        return Math.round((double)elapseAvg.get());
    }

    protected final void doSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }
}