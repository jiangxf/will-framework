package org.will.framework.aq;

/**
 * Created by will on 02/12/2016.
 */
//@Component
public abstract class AQConsumerWrapper<PARAMTYPE> extends AQConsumer {

    @Override
    protected AQProcessor createAQProcessor(String threadName) {
        return new SimpleAQProcessor(threadName, this);
    }

    protected abstract AQResponse doRequest(AQMessage<PARAMTYPE> aqRequest);

    final class SimpleAQProcessor extends AQSingleProcessor {
        public SimpleAQProcessor(String threadName, AQConsumer aqConsumer) {
            super(threadName, aqConsumer);
        }

        /**
         * 公开 doRequest 请求
         *
         * @param aqRequest
         * @return
         */
        @Override
        protected final AQResponse doRequest(AQMessage aqRequest) {
            return ((AQConsumerWrapper) aqConsumer).doRequest(aqRequest);
        }
    }
}
