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
            aqConsumer.schedule();
            doSleep(aqConsumer.getWatcherCycleSec() * 1000);
        }
    }

    protected final AQConsumer aqConsumer;
}
