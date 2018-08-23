package org.will.framework.aq.producer;

import org.will.framework.aq.common.AQMessage;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-17
 * Time: 14:04
 */
public interface AQProducerListener {

    void beforeSend(AQMessage aqMessage);

    void afterSend(AQMessage aqMessage, Throwable throwable);
}
