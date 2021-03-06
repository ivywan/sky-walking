package com.a.eye.skywalking.storage.block.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 块索引的一级缓存
 *
 * Created by xin on 2016/11/2.
 */
public class L1Cache {

    private static final int           MAX_DATA_KEEP_SIZE = 30;
    private static       Logger        logger             = LogManager.getLogger(L1Cache.class);
    private              TreeSet<Long> cacheData          = new TreeSet<Long>();
    private final        ReadWriteLock updateLock         = new ReentrantReadWriteLock();

    void init(List<Long> data) {
        for (int i = 0; i < MAX_DATA_KEEP_SIZE; i++) {
            this.cacheData.add(data.get(i));
        }
    }

    public Long find(long timestamp) {
        Lock lock = updateLock.readLock();
        try {
            lock.lock();
            return cacheData.higher(timestamp);
        } finally {
            lock.unlock();
        }
    }

    public void add2Rebuild(long timestamp) {
        TreeSet<Long> newCacheData = new TreeSet<>(cacheData);
        newCacheData.add(timestamp);

        if (newCacheData.size() >= MAX_DATA_KEEP_SIZE + 1) {
            long removedData = newCacheData.pollFirst();
            logger.info("Add cache data : {}, removed cache Data:{}", timestamp, removedData);
        }

        Lock lock = updateLock.writeLock();
        try {
            lock.lock();
            cacheData = newCacheData;
        } finally {
            lock.unlock();
        }
    }

}
