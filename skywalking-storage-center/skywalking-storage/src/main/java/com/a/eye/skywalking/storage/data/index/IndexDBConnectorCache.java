package com.a.eye.skywalking.storage.data.index;

import java.util.LinkedHashMap;
import java.util.Map;

public class IndexDBConnectorCache {

    private static final int MAX_CACHE_SIZE = 5;

    private LRUCache cachedOperators;

    public IndexDBConnectorCache() {
        cachedOperators = new LRUCache(MAX_CACHE_SIZE);
    }

    public IndexDBConnector get(long timestamp) {
        IndexDBConnector connector = (IndexDBConnector) cachedOperators.get(timestamp);
        if (connector == null) {
            connector = new IndexDBConnector(timestamp);
            updateCache(timestamp, connector);
        }
        return connector;
    }

    private void updateCache(long timestamp, IndexDBConnector operator) {
        cachedOperators.put(timestamp, operator);
    }

    private void removedCache(IndexDBConnector connector) {
        connector.close();
    }

    private class LRUCache extends LinkedHashMap<Long, IndexDBConnector> {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, IndexDBConnector> eldest) {
            boolean isNeedRemove = size() > MAX_CACHE_SIZE;
            if (isNeedRemove) {
                removedCache(eldest.getValue());
            }
            return isNeedRemove;
        }

        public LRUCache(int cacheSize) {
            super((int) Math.ceil(MAX_CACHE_SIZE / 0.75) + 1, 0.75f, true);
        }
    }
}
