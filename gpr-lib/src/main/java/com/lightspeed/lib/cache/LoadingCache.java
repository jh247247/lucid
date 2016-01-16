package com.lightspeed.gpr.lib.cache;

import java.util.concurrent.ConcurrentHashMap;

public class LoadingCache<K,V> extends ConcurrentHashMap<K,V> {
    private CacheLoader m_cacheLoader;

    /**
     * These constructors are essentially the same as the inherited ConcurrentHashMap, except with the forced addition
     * of a CacheLoader to compute unknown elements.
     */

    LoadingCache(CacheLoader cl) {
        super();
        m_cacheLoader = cl;
    }

    LoadingCache(int initialCapacity, CacheLoader cl) {
        super(initialCapacity);
        m_cacheLoader = cl;
    }

    LoadingCache(int initialCapacity, float loadFactor, CacheLoader cl) {
        super(initialCapacity, loadFactor);
        m_cacheLoader = cl;
    }

    LoadingCache(int initialCapacity, float loadFactor, int concurrencyLevel, CacheLoader cl) {
        super(initialCapacity, loadFactor, concurrencyLevel);
        m_cacheLoader = cl;
    }

    /**
     * This is the interface that the loader must fulfill.
     */
    public static interface CacheLoader<K,V> {
        public V load(K key);
    }

    @Override
    public V get(Object key) {
        // have to downcast, unsafe!
        cache((K)key);
        return super.get((K)key);
    }

    public void cache(K key) {
        // check if map contains key
        if(!containsKey(key) && m_cacheLoader != null) {
            put(key, (V)m_cacheLoader.load((K)key));
        }
    }
}
