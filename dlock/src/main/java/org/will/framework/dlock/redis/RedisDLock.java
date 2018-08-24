package org.will.framework.dlock.redis;

import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;
import org.will.framework.dlock.DLock;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 16:30
 */
public class RedisDLock extends DLock {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DLock.class);

    private final static String LUA_SETNX_EXPIRE_SCRIPT = ""
            + "\nlocal r = tonumber(redis.call('SETNX', KEYS[1],ARGV[1]));"
            + "\nredis.call('PEXPIRE',KEYS[1],ARGV[2]);"
            + "\nreturn r";

    private final static String LUA_GET_DEL_SCRIPT = ""
            + "\nlocal v = redis.call('GET', KEYS[1]);"
            + "\nlocal r = 0;"
            + "\nif v == ARGV[1] then"
            + "\nr = redis.call('DEL',KEYS[1]);"
            + "\nend"
            + "\nreturn r";

    private Jedis jedis;

    public RedisDLock(Jedis jedis, String lockKey) {
        super(lockKey);
        this.jedis = jedis;
    }

    /**
     * 创建锁，保证原子操作
     * @return
     */
    @Override
    protected boolean createLock(long expireMS) {
        List<String> keys = Lists.newArrayList();
        keys.add(lockKey);
        List<String> args = Lists.newArrayList();
        args.add(lockKey);
        args.add(expireMS + "");

        Long ret = (Long) jedis.eval(LUA_SETNX_EXPIRE_SCRIPT, keys, args);
        if (new Long(1).equals(ret)) {
            return true;
        }
        return false;
    }

    /**
     * 释放锁，保证原子操作
     * @return
     */
    @Override
    protected void doUnlock(String key, String value) {
        List<String> keys = new ArrayList<String>();
        keys.add(key);
        List<String> args = new ArrayList<String>();
        args.add(value);
        Object obj = jedis.eval(LUA_GET_DEL_SCRIPT, keys, args);
    }
}
