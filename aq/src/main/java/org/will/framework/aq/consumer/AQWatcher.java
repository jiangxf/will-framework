package org.will.framework.aq.consumer;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-25
 * Time: 14:24
 */
public class AQWatcher extends AQThread {

    public AQWatcher(String threadName, AQConsumer aqConsumer) {
        super(threadName);
        this.aqConsumer = aqConsumer;
    }

    @Override
    protected void doRun() {
        while (true) {
            int real = 0;
            int parallel = aqConsumer.computeParallelCount();
            long queueLength = aqConsumer.size();
            if (queueLength <= 0) {
                aqConsumer.setNeedThread(aqConsumer.getMinWorkerThread());
            } else {
                int need = (int) ((queueLength + parallel + aqConsumer.getQpsAvg()) / parallel);
                real = need;
                if (need >= aqConsumer.getMaxWorkerThread()) {
                    need = aqConsumer.getMaxWorkerThread();
                } else if (need <= aqConsumer.getMinWorkerThread()) {
                    need = aqConsumer.getMinWorkerThread();
                }
                aqConsumer.setNeedThread(need);
            }

            aqConsumer.loadAQWorkers();
            if (queueLength > 0) {
                logger.info("AQWatcher <> topic:{}, remain:{}, qpsAvg:{}, elapseAvg:{}, parallel:{}, running:{}, need:{}, real:{}",
                        aqConsumer.getTopic(), queueLength, aqConsumer.getQpsAvg(), aqConsumer.getElapseAvg(), parallel,
                        aqConsumer.getWorkerThreadMap().size(), aqConsumer.getNeedThread(), real);
            }

            doSleep(aqConsumer.getWatcherCycleSec() * 1000);
        }
    }

    protected final AQConsumer aqConsumer;
}
