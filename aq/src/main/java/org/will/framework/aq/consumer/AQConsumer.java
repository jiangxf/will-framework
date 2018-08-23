package org.will.framework.aq.consumer;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.will.framework.aq.AQContext;
import org.will.framework.aq.AQMessage;
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
public abstract class AQConsumer<DT> {

    protected final ConcurrentLinkedQueue<Long> elapseQueue = new ConcurrentLinkedQueue<>();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final String topic;
    protected final int maxWorkerThread;
    protected final int minWorkerThread;
    protected final RateLimiter rateLimiter = RateLimiter.create(DEFAULT_QPS);
    protected final ConcurrentMap<String, AQWorker> workerThreadMap = Maps.newConcurrentMap();
    protected final AtomicInteger needWorkThreadAtoc = new AtomicInteger(0);
    protected final int watcherCycleSec = 3;
    protected AQWatcher aqWatcher = null;
    protected AtomicInteger parallelCount = new AtomicInteger(0);
    protected AtomicDouble elapseAvg = new AtomicDouble(0);
    protected AtomicInteger qpsAvg = new AtomicInteger(0);
    protected AQQueue aqQueue;
    protected final AtomicInteger runStatus = new AtomicInteger(0);

    public AQConsumer(String topic, AQQueue aqQueue) {
        this.topic = topic;
        this.maxWorkerThread = MAX_WORKER_THREAD;
        this.minWorkerThread = MIN_WORKER_THREAD;
        this.aqQueue = aqQueue;
    }

    public AQConsumer(String topic, int maxThread, int minThread, AQQueue aqQueue) {
        this.topic = topic;
        this.maxWorkerThread = maxThread;
        this.minWorkerThread = minThread;
        this.aqQueue = aqQueue;
    }

    /**
     * 初始化参数
     */
    protected void init() {
        loadAQWatcher();
        loadAQWorkers();
    }

    public void start(){
        runStatus.set(1);

        init();
    }

    public void stop() {
        runStatus.set(0);

        while (aqWatcher != null && !aqWatcher.isInterrupted()) {
            aqWatcher.interrupt();
        }
        aqWatcher = null;

        for (AQWorker aqWorker : workerThreadMap.values()) {
            while (!aqWorker.isInterrupted()) {
                aqWorker.interrupt();
            }
        }
        workerThreadMap.clear();
    }

    /**
     * 接收消息
     *
     * @return
     */
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

    public void schedule() {
        int real = 0;
        int parallel = computeParallelCount();
        long queueLength = size();
        if (queueLength <= 0) {
            setNeedThread(getMinWorkerThread());
        } else {
            int need = (int) ((queueLength + parallel + getQpsAvg()) / parallel);
            real = need;
            if (need >= getMaxWorkerThread()) {
                need = getMaxWorkerThread();
            } else if (need <= getMinWorkerThread()) {
                need = getMinWorkerThread();
            }
            setNeedThread(need);
        }

        loadAQWorkers();
        if (queueLength > 0) {
            logger.info("AQWatcher <> topic:{}, remain:{}, qpsAvg:{}, elapseAvg:{}, parallel:{}, running:{}, need:{}, real:{}",
                    topic, queueLength, getQpsAvg(), getElapseAvg(), parallel, workerThreadMap.size(), getNeedThread(), real);
        }
    }

    public long size() {
        return aqQueue.size(topic);
    }

    public void acquire() {
        rateLimiter.acquire();
    }

    /**
     * 创建守护线程
     */
    protected final synchronized void loadAQWatcher() {

        if (runStatus.get() == 0) {
            return;
        }

        if (StringUtils.isEmpty(topic)) {
            throw new IllegalArgumentException("type 不能为空");
        }

        if (aqWatcher == null || !aqWatcher.isAlive()) {
            String watcherName = "AQWatcher-Thread-" + topic.trim();
            aqWatcher = new AQWatcher(watcherName, this);
            aqWatcher.start();
        }
    }

    /**
     * 创建消费处理线程组
     */
    protected final synchronized void loadAQWorkers() {

        if (runStatus.get() == 0) {
            return;
        }

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

    /**
     * 计算并行处理数
     *
     * @return
     */
    protected final int computeParallelCount() {
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
        if (elapseAvgTemp < 0.01) {
            elapseAvgTemp = 0.01;
        }
        elapseAvg.set(elapseAvgTemp);

        double parallel = 1000 / elapseAvgTemp;
        if (parallel < 1) {
            parallel = 1;
        }
        return new BigDecimal(parallel).intValue();
    }

    protected AQWorker createAQWorker(String threadName) {
        return new AQWorker(threadName, this) {
            @Override
            protected void processMessage(AQContext aqContext, Object data) {
                doProcess(aqContext, (DT)data);
            }
        };
    }

    protected abstract void doProcess(AQContext aqContext, DT data);

    protected final void doSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private final void startAQWorker(String threadName) {
        AQWorker aqWorker = createAQWorker(threadName);
        workerThreadMap.put(threadName, aqWorker);

        aqWorker.start();
    }

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
        return Math.round((double) elapseAvg.get());
    }

    public boolean isRunning() {
        return runStatus.get() > 0;
    }
}
