package org.will.framework.aq.queue;

import redis.clients.jedis.ShardedJedis;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 19:27
 */
public class ShardedJedisAQQueue implements AQQueue {

    public ShardedJedisAQQueue(ShardedJedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public void enqueue(String topic, byte[] message) {
        jedis.rpush(topic.getBytes(), message);
    }

    @Override
    public byte[] dequeue(String topic) {
        return jedis.lpop(topic.getBytes());
    }

    @Override
    public long size(String topic) {
        return jedis.llen(topic);
    }

    @Override
    public boolean clear(String topic) {
        return jedis.del(topic) > 0;
    }

    ShardedJedis jedis;
}
