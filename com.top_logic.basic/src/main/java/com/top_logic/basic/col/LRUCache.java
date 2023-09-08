/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import com.top_logic.basic.Logger;

/**
 * A Map with LRU behavior.
 * 
 * This is a replacement for {@link LRUMap} that does not allow removal
 * along time thus avoiding asynchronous problems of all kinds. In contracts
 * to the LRUMap this class is <em>not synchronized</em>. In case you need 
 * this you may use {@link Collections#synchronizedMap(Map)}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class LRUCache<K,V> extends LinkedHashMap<K,V> {

   /**
     * The load factor used when none specified in constructor.
     **/
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /** maximim size to remove a entry */
    private int maxCut;

    /** 
     * Create a new LRUCache with given maximum size.
     */
    public LRUCache(int aMaxCut) {
        super(aMaxCut, DEFAULT_LOAD_FACTOR, /* acessorder */ true);
        maxCut = aMaxCut;
    }

    /** 
     * Create a new LRUCache. from a given Map.
     * 
     * @param aMaxCut Old Elements will be {@link #handleUpcomingRemove(Object, Object)} when this size is reached.
     */
    public LRUCache(int aMaxCut, Map<K,V> aMap) {
        this(aMaxCut);
        if (aMap.size() > aMaxCut) {
            Logger.warn("Size of Map(" + aMap.size() + ") is bigger then specified LRU-size(" + aMaxCut + ")", this);
        }
        this.putAll(aMap);
    }

    /** 
     * Create a new LRUCache and allow specification of loadFactor.
     */
    public LRUCache(int aMaxCut, float aLoadFactor) {
        super(aMaxCut, aLoadFactor, /* acessorder */ true);
        maxCut = aMaxCut;
    }
    
    /**
     * Constructor using Propertis to Configure the intial values.
     * 
     * will use <code>&lt;prefix&gt;LRUSeconds</code> and
     * <code>&lt;prefix&gt;LRUCount</code> with same semantics a defaut Ctor.
     *
     * @param aProp     Properties used to configure the LRUMap.
     *                  0, if Entries don't expire
     * @param aPrefix   Optional Prefix to prepend before using Proerty values.
     * 
     * @param aDefCount  default minimal size of the map after a clean up due to oversize.
     */
    public LRUCache (Properties aProp, String aPrefix, int aDefCount) {
        this(extractMaxCut(aProp, aPrefix, aDefCount));
    }

    /** 
     * Static helper for CTor to extract the value of "LRUCount"
     */
    static int extractMaxCut(Properties aProp, String aPrefix, int aDefCount) {
        if (aPrefix == null)
            aPrefix = "";
        String val = aProp.getProperty(aPrefix + "LRUCount");
        if (val != null)
            return Integer.parseInt(val);
        else
            return aDefCount;
    } 


    @Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
    {
        boolean remove = size() > maxCut;
        if (remove) {
			handleUpcomingRemove(eldest);
        }
        return remove;
    }
    
	/**
	 * Overridden to call {@link #handleUpcomingRemove(Object, Object)} as needed.
	 */
    @Override
	public V remove(Object aKey) {
		V v = get(aKey);
		if (v != null || containsKey(aKey)) {
			handleUpcomingRemove(aKey, v);
		}
		return super.remove(aKey);
    }

	/**
	 * Overridden to call {@link #handleUpcomingRemove(Object, Object)} as needed.
	 */
	@Override
	public V put(K key, V value) {
		V v = get(key);
		if (v != null || containsKey(key)) {
			handleUpcomingRemove(key, v);
		}
		return super.put(key, value);
	}

	/**
	 * Overridden to call {@link #handleUpcomingRemove(Object, Object)} as needed.
	 */
	@Override
	public void clear() {
		for (Map.Entry<K, V> entry : entrySet()) {
			handleUpcomingRemove(entry);
		}
		super.clear();
	}
        
	private void handleUpcomingRemove(Map.Entry<K, V> entry) {
		handleUpcomingRemove(entry.getKey(), entry.getValue());
	}

    /**
	 * Hook for subclasses (e.g. to close() cached objects).
	 * 
	 * This function should <em>not</em> be called from a (known) synchronized block so subclasses
	 * are free to do their own locking. It will be called for each key / value pair that will be
	 * removed.
	 *
	 * @param key
	 *        The key that object was bound to.
	 * @param value
	 *        The object, which is to removed.
	 */
	protected void handleUpcomingRemove(Object key, V value) {
        // override as you see fit
    }

    /**
	 * The maximum number of concurrent objects in the cache.
	 */
    public int getMaxCut() {
        return maxCut;
    }

}

