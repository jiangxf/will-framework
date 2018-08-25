package org.will.framework.aq.queue;

import com.qunar.redis.storage.Sedis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.will.framework.aq.common.AQMessage;
import org.will.framework.util.Base64;
import org.will.framework.util.ProtoStuffUtil;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 19:27
 */
public class SedisAQQueue implements AQQueue {

    protected final static Logger logger = LoggerFactory.getLogger(SedisAQQueue.class);

    Sedis sedis;

    public SedisAQQueue(Sedis sedis) {
        this.sedis = sedis;
    }

    @Override
    public void enqueue(String topic, AQMessage message) {
        try {
            byte[] bytes = ProtoStuffUtil.serializer(message);
            String str = new String(Base64.encode(bytes));
            sedis.rpush(topic, str);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public AQMessage dequeue(String topic) {
        try {
            String str = sedis.lpop(topic);
            byte[] bytes = Base64.decode(str.toCharArray());
            return ProtoStuffUtil.deserializer(bytes, AQMessage.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public long size(String topic) {
        return sedis.llen(topic);
    }

    @Override
    public boolean clear(String topic) {
        return sedis.del(topic) > 0;
    }
}
