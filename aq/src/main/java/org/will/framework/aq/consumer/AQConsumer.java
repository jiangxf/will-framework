package org.will.framework.aq.consumer;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.will.framework.aq.common.AQContext;
import org.will.framework.aq.common.AQMessage;
import org.will.framework.aq.queue.AQQueue;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.will.framework.aq.common.AQConstants.*;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-17
 * Time: 21:01
 */
public abstract class AQConsumer<DT> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 记录每个消息的处理执行时间 ms
    protected final ConcurrentLinkedQueue<Long> elapseQueue = new ConcurrentLinkedQueue<>();

    // 消息类型
    protected final String topic;
    // worker 线程最大数
    protected final int maxWorkerThread;
    // worker 线程最小数
    protected final int minWorkerThread;
    // 当前需要的线程数
    protected final AtomicInteger needWorkThreadAtoc = new AtomicInteger(0);
    // 限速
    protected final RateLimiter rateLimiter = RateLimiter.create(DEFAULT_QPS);
    // 保存 worker 线程的池子
    protected final ConcurrentMap<String, AQWorker> workerThreadMap = Maps.newConcurrentMap();
    // watcher 的循环时间
    protected final int watcherCycleSec = 3;
    // 运行状态
    protected final AtomicInteger runStatus = new AtomicInteger(0);
    // 监视器
    protected AQWatcher aqWatcher = null;
    // 每个消息处理的平均时间 ms
    protected AtomicDouble elapseAvg = new AtomicDouble(0);
    // qps
    protected AtomicInteger qpsAvg = new AtomicInteger(0);
    // 队列
    protected AQQueue aqQueue;

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

    /**
     * 开始消费
     */
    public void start() {
        runStatus.set(1);

        init();
    }

    /**
     * 停止消费
     */
    public void stop() {
        runStatus.set(0);

        if (aqWatcher != null && aqWatcher.isInterrupted()) {
            aqWatcher.interrupt();
        }

        for (AQWorker aqWorker : workerThreadMap.values()) {
            if (aqWorker.isInterrupted()) {
                aqWorker.interrupt();
            }
        }

        while (aqWatcher.isAlive()) {
            doSleep(200);
        }

        while (workerThreadMap.size() > 0) {
            doSleep(200);
        }

        aqWatcher = null;
        workerThreadMap.clear();
    }

    /**
     * 接收消息
     *
     * @return
     */
    public AQMessage recv() {
        if (aqQueue.size(topic) > 0) {
            return aqQueue.dequeue(topic);
        }
        return null;
    }

    /**
     * wather 的实现
     */
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

    /**
     * 队列已使用长度
     *
     * @return
     */
    public synchronized long size() {
        return aqQueue.size(topic);
    }

    /**
     * 获取限速令牌
     *
     * @return
     */
    public synchronized void acquire() {
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

    protected AQWorker createAQWorker(String threadName) {
        return new AQWorker(threadName, this) {
            @Override
            protected void processMessage(AQContext aqContext, Object data) {
                logger.info("AQConsumer context:{}, data:{}", aqContext.toString(), data.toString());

                doProcess(aqContext, (DT) data);
            }
        };
    }

    protected abstract void doProcess(AQContext aqContext, DT data);

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
