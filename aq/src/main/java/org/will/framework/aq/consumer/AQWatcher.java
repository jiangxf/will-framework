package org.will.framework.aq.consumer;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-25
 * Time: 14:24
 */
public class AQWatcher extends AQThread {

    protected final AQConsumer aqConsumer;

    public AQWatcher(String threadName, AQConsumer aqConsumer) {
        super(threadName);
        this.aqConsumer = aqConsumer;
    }

    @Override
    protected void doRun() throws InterruptedException {
        while (aqConsumer.isRunning()) {
            aqConsumer.schedule();
            sleep(aqConsumer.getWatcherCycleSec() * 1000);
        }
    }
}
