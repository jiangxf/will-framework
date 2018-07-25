//package org.will.framework.aq.cache;
//
//import org.springframework.util.CollectionUtils;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//
///**
// * Created with IntelliJ IDEA
// * Description:
// * User: will
// * Date: 2018-07-14
// * Time: 14:27
// */
//public class LocalCache implements Cache {
//
//    private static final ConcurrentHashMap<String, LinkedBlockingQueue> QueueMap = new ConcurrentHashMap<>();
//    private static volatile int help = 1;
//    private int capacity = 2048;
//
//    public LocalCache(int capacity){
//        this.capacity = capacity;
//    }
//
//    private LinkedBlockingQueue getQueue(String key){
//        LinkedBlockingQueue queue = QueueMap.get(key);
//        if(queue == null){
//            synchronized (Cache.class.getName() + key){
//                queue = QueueMap.get(key);
//                if(queue == null){
//                    QueueMap.put(key, new LinkedBlockingQueue(capacity));
//                }
//            }
//        }
//        return queue;
//    }
//
//    @Override
//    public void rpush(String key, String value) {
//        getQueue(key).offer(value);
//    }
//
//    @Override
//    public String lpop(String key) {
//        return getQueue(key).poll();
//    }
//
//    @Override
//    public void del(String key) {
//
//    }
//
//    @Override
//    public long llen(String key) {
//        return 0;
//    }
//
//    @Override
//    public boolean exists(String key) {
//        return false;
//    }
//
//    @Override
//    public String get(String key) {
//        return null;
//    }
//
//    @Override
//    public void set(String key, String value, int expireMS) {
//
//    }
//}
