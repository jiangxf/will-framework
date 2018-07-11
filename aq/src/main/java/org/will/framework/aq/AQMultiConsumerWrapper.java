package org.will.framework.aq;

import java.util.List;

/**
 * Created by will on 02/12/2016.
 */
//@Component
public abstract class AQMultiConsumerWrapper extends AQConsumer {

    protected int multiCount = 10;

    @Override
    protected AQProcessor createAQProcessor(String threadName) {
        return new SimpleMultiAQProcessor(threadName, this, multiCount);
    }

    protected abstract List<AQResponse> doRequest(List<AQMessage> aqRequestList);

    final class SimpleMultiAQProcessor extends AQMultiProcessor {
        public SimpleMultiAQProcessor(String threadName, AQConsumer aqConsumer, int multiCount) {
            super(threadName, aqConsumer, multiCount);
        }

        /**
         * 公开 doRequest 请求
         *
         * @param aqRequestList
         * @return
         */
        @Override
        protected final List<AQResponse> doRequest(List<AQMessage> aqRequestList) {
            return ((AQMultiConsumerWrapper) aqConsumer).doRequest(aqRequestList);
        }
    }
}
