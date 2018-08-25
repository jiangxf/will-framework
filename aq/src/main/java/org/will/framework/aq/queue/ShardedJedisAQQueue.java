package org.will.framework.aq.queue;

import org.will.framework.aq.common.AQMessage;
import org.will.framework.util.ProtoStuffUtil;
import redis.clients.jedis.ShardedJedis;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 19:27
 */
public class ShardedJedisAQQueue implements AQQueue {

    ShardedJedis jedis;

    public ShardedJedisAQQueue(ShardedJedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public void enqueue(String topic, AQMessage message) {
        byte[] bytes = ProtoStuffUtil.serializer(message);
        jedis.rpush(topic.getBytes(), bytes);
    }

    @Override
    public AQMessage dequeue(String topic) {
        byte[] bytes = jedis.lpop(topic.getBytes());
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return ProtoStuffUtil.deserializer(bytes, AQMessage.class);
    }

    @Override
    public long size(String topic) {
        return jedis.llen(topic);
    }

    @Override
    public boolean clear(String topic) {
        return jedis.del(topic) > 0;
    }
}
