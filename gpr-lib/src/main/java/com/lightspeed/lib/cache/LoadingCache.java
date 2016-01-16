package com.lightspeed.gpr.lib.cache;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.SortedSet;
import java.util.Map;
import java.util.Comparator;
import java.util.TreeSet;

public class LoadingCache<K,V> extends ConcurrentHashMap<K,V> {
    private CacheLoader m_cacheLoader;
    private ConcurrentHashMap<K,Long> m_keyLastAccess = new ConcurrentHashMap<K,Long>();

    private static final int DEFAULT_MAX_SIZE = 16;
    private static final int SIZE_HYSTERISIS = 4;
    private int m_maxSize = DEFAULT_MAX_SIZE;


    /**
     * These constructors are essentially the same as the inherited ConcurrentHashMap, except with the forced addition
     * of a CacheLoader to compute unknown elements.
     */

    public LoadingCache(CacheLoader cl) {
        super();
        m_cacheLoader = cl;
    }

    public LoadingCache(int initialCapacity, CacheLoader<K,V> cl) {
        super(initialCapacity);
        m_maxSize = initialCapacity;
        m_cacheLoader = cl;
    }

    public LoadingCache(int initialCapacity, float loadFactor, CacheLoader<K,V> cl) {
        super(initialCapacity, loadFactor);
        m_maxSize = initialCapacity;
        m_cacheLoader = cl;
    }

    public LoadingCache(int initialCapacity, float loadFactor, int concurrencyLevel, CacheLoader<K,V> cl) {
        super(initialCapacity, loadFactor, concurrencyLevel);
        m_maxSize = initialCapacity;
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
        m_keyLastAccess.put((K)key,new Long(System.nanoTime()));
        return super.get((K)key);
    }

    @Override
    public V put(K key, V val) {
        V v = super.put(key,val);
        m_keyLastAccess.put(key, new Long(System.nanoTime()));
        trim();
        return v;
    }

    /**
     * Attempt to cache an object based on a given key.
     */
    public void cache(K key) {
        // check if map contains key
        if(super.get(key) == null && m_cacheLoader != null) {
            put(key, (V)m_cacheLoader.load((K)key));
        }
    }


    /**
     * Trim the cache, removes the oldest SIZE_HYSTERISIS elements, or the m_maxSize/10, whichever is larger
     */
    synchronized public void trim() {
        int trimSize = Math.max(m_maxSize/10, SIZE_HYSTERISIS);
        if(size() > m_maxSize) {
            // remove the oldest SIZE_HYSTERISIS elements
            SortedSet<Map.Entry<K,Long>> entries = entriesSortedByValues(m_keyLastAccess);

            int idx = 0;
	    Iterator<Map.Entry<K,Long>> it = entries.iterator();

	    while(idx++ < trimSize && it.hasNext()) {
		Map.Entry<K,Long> e = it.next();
		remove(e.getKey());
		m_keyLastAccess.remove(e.getKey());
	    }
        }
    }

    // https://stackoverflow.com/questions/2864840/treemap-sort-by-value
    private static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
	SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(new Comparator<Map.Entry<K,V>>() {
		@Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
		    int res = e1.getValue().compareTo(e2.getValue());
		    return res != 0 ? res : 1; // Special fix to preserve items with equal values
		}
	    }
	    );
	sortedEntries.addAll(map.entrySet());
	return sortedEntries;
    }
}
