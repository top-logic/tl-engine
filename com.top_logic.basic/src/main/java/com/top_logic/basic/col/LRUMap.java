/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.top_logic.basic.Logger;

/**
 * LRUMap represents a Map with a preset maximum size and/or maximum time.
 * <p>
 * Each Entry is associated with a time stamp representing the last time of use
 * (get, put). If the Map reaches the maximum size the least resently used Entry
 * is removed.
 * </p>
 * <p>
 * Expect ConcurrentModificationExceptions when using Iterators on keys, values
 * and entries due to the regular removal of lru entries.
 * </p>
 * 
 * TODO Better use {@link LRUCache} with does not have problems with asynchronous removal.
 * 
 * @author <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class LRUMap implements Map, LRU {

	/** minimum time to remove a entry */
	private final long minTime;
    
	/** maximim size to remove a entry */
	private int maxCut;
    
	/** least accessed Entry */
	private LinkedLRUMapValue first;
    
	/** last accessed Entry */
	private LinkedLRUMapValue last;
    
	/** HashMap to keep access dates */
	private Map access;
    
	/** the LRUMapWatcher watching this map */
	private final LRUWatcher watcher;

	/**
	 * Constructor.
	 * 
	 * @param aMinTime
	 *        the interval after which an Entry can be removed 0, if Entries don't expire
	 * @param aMaxCut
	 *        the maximum size of the map 0, if size is not constrained
	 * @param anInitMap
	 *        an initial Map to fill the LRUMap
	 * @param aWatcher
	 *        a LRUMapWatcher in case the global watcher is not to be used
	 */
	public LRUMap(long aMinTime,
		          int  aMaxCut,
		          Map  anInitMap,
		          LRUWatcher aWatcher)  {
            
		minTime = aMinTime;
		maxCut  = aMaxCut;
		first   = null;
		last    = null;
		// set up watcher
		if (minTime > 0) {
			if (aWatcher != null)
				watcher = aWatcher;
			else {
				watcher = LRUWatcher.getLRUWatcher();
			}
		} else {
			watcher = null;
		}
		// initialize maps
		if (anInitMap != null) {
			// generate access Map as copy of initial Map
			LinkedLRUMapValue prev        = null;
			long              currentTime = System.currentTimeMillis();
			Iterator          it          = anInitMap.entrySet().iterator();
                              access      = new HashMap(anInitMap.size());
            LinkedLRUMapValue current;
            Object            key;
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
                key     = entry.getKey();
				current = new LinkedLRUMapValue(key, entry.getValue(),
                                          currentTime, prev,  null);
                if (prev != null)
                    prev.next = current;
                else if (first == null) 
                    first = current;
                access.put(key, current);
                prev = last = current;
                assert checkLists() : "<init> with Map";
            }
        } else {
            // generate empty map and access map
            access = new HashMap(aMaxCut);
        }
    }

    /**
	 * Simple Consturctor
	 *
	 * @param aMinTime
	 *        the interval after which an Entry can be removed 0, if Entries don't expire
	 * @param aMaxCut
	 *        the minimal size of the map after a clean up due to oversize
	 */
	public LRUMap (long aMinTime, int aMaxCut) {
         
		minTime = aMinTime;
		maxCut  = aMaxCut;
		first   = null;
		last    = null;
		// set up watcher
		if (minTime > 0) {
			watcher = LRUWatcher.getLRUWatcher();
		} else {
			watcher = null;
		}
		// generate empty map and access map
		access = new HashMap(aMaxCut);
	} 

    /**
	 * Constructor using Propertis to Configure the intial values.
	 * 
	 * will use <code>&lt;prefix&gt;LRUSeconds</code> and <code>&lt;prefix&gt;LRUCount</code> with
	 * same semantics a defaut Ctor.
	 *
	 * @param aProp
	 *        Properties used to configure the LRUMap. 0, if Entries don't expire
	 * @param aPrefix
	 *        Optional Prefix to prepend before using Proerty values.
	 * 
	 * @param aDefTime
	 *        default interval after which an Entry can be removed 0, if Entries don't expire.
	 * @param aDefCount
	 *        default minimal size of the map after a clean up due to oversize.
	 */
    public LRUMap (Properties aProp, String aPrefix,
                   long aDefTime, int aDefCount) {
        
        if (aPrefix == null)
            aPrefix = "";
        String val = aProp.getProperty(aPrefix + "LRUSeconds");
        if (val != null)
            minTime = 1000 * Long.parseLong(val);
        else
            minTime = aDefTime;
        val = aProp.getProperty(aPrefix + "LRUCount");
        if (val != null)
            maxCut = Integer.parseInt(val);
        else
            maxCut = aDefCount;

        first   = null;
        last    = null;
        // set up watcher
        if (minTime > 0) {
            watcher = LRUWatcher.getLRUWatcher();
        } else {
        	watcher = null;
        }
        // generate empty map and access map
        access = new HashMap(maxCut);
    } 

    /** 
     * Return nanme of class and some usefull values for debuggign.
     */
    @Override
	public String toString() {
        return this.getClass().getName() + " [" + this.toStringValues() + ']';
    }

    /** 
     * Return some usefull values for debuggign.
     */
    private String toStringValues() {
        return "maxCut: " + this.maxCut  + ", minTime: " + this.minTime;
    }

    /** 
     * AbstractMap uses (unssupported) entrySet() for equals.
     * 
     * @return this == other, ynthing else would be suicide
     */
	@Override
	public boolean equals(Object other) {
        return this == other; 
	}

	/** 
	 * Returns true if this map contains a mapping
	 * for the specified key.
	 */
	@Override
	public boolean containsKey(Object aKey) {
		// just for forward to actual map
		return access.containsKey(aKey);
	}

	/** 
	 * Returns true if this map maps one or more 
	 * keys to the specified value.
	 */
	@Override
	public boolean containsValue(Object aVal) {
		// just for forward to actual map
		return access.containsValue(new LinkedLRUMapValue(null, aVal, 0, null, null));
	}
	
	/**
	 * Unsupported since the internal mechnism dont allow a clean implementation.
	 * 
	 * You would expect ConcurrentModificationExceptions when using Iterators on keys, values and
	 * entries due to the regular removal of LRU Entries.
	 */
	@Override
	public Set entrySet() {
		// just for forward to actual map
        throw new UnsupportedOperationException();
	}

	/** 
	 * Returns the value to which this map 
	 * maps the specified key. 
	 */
	@Override
	public synchronized Object get(Object aKey) {
		LinkedLRUMapValue accessObject = (LinkedLRUMapValue) access.get(aKey);
		if (accessObject != null) {
            assert checkLists() : "List out of order (pre)";
			// update access dates
			accessObject.time = System.currentTimeMillis();
			if (accessObject.prev != null) {
            
				// actual Entry is not least accessed Entry
                // remove it from list 
				accessObject.prev.next = accessObject.next;
				if (accessObject.next != null) {
					accessObject.next.prev = accessObject.prev;
				} else {
					last = accessObject.prev;
				}

                assert checkLists() : "List out of order (middle)";

                // put it at front position
                accessObject.next = first;
				first.prev = accessObject;
				first = accessObject;
                accessObject.prev = null;
			}
            assert checkLists() : "List out of order (post)";
			// return actual entry
			return accessObject.value;
		}
		return null;
	}

	/** 
	 * Returns true if this map 
	 * contains no key-value mappings.
	 */
	@Override
	public boolean isEmpty() {
		// just for forward to actual map
		return access.isEmpty();
	}

    /** 
     * Returns a set view of the keys contained 
     * in this map.
     * 
     * Expect ConcurrentModificationExceptions when using Iterators on 
     * keys, values and entries due to the regular removal of lru entries.
     */
    @Override
	public Set keySet() {
        // just for forward to actual map
        return new LRUKeySet(access.keySet());
    }

    /** 
     * Associates the specified value with the specified key in this map. 
     * If the map previously contained a mapping for this key, the old 
     * value is replaced.
     */
    @Override
	public synchronized Object put(Object aKey, Object aValue) {
        Object oldValue;  // save old value for return  

        boolean register;
        synchronized (this) {
            // if map is empty, it must be register to the watcher after put
			register = size() == 0;
     
            assert checkLists() : "List out of order (pre)";
    
    		// generate an accessEntry for the new mapping
    		LinkedLRUMapValue accessEntry =
    			new LinkedLRUMapValue(aKey, aValue, System.currentTimeMillis(), null, null);
    		// put entry in access map 
    		LinkedLRUMapValue oldEntry = (LinkedLRUMapValue) access.put(aKey, accessEntry);
    		if (oldEntry != null) {
    			oldValue = oldEntry.value;
    		} else {
    			oldValue = null;
    		}
    		// update LRU list
    		// remove oldEntry from list
    		if (oldEntry != null) {
    			if (first != oldEntry) {
    				if (last != oldEntry) {     // first -> x - (key) - x <- last 
    					oldEntry.next.prev = oldEntry.prev;
    					oldEntry.prev.next = oldEntry.next;
    				} else {                       // first -> x - (key) <- last
    					last = oldEntry.prev;
    					oldEntry.prev.next = null;
    				}
    			} else if (last != oldEntry) {  // first -> (key) - x <- last
    				first = oldEntry.next;
    				oldEntry.next.prev = null;
    			} else {                           // first -> (key) <- last
    				first = last = null;
    			}
    		}
    
            assert checkLists() : "List out of order (middle)";
    
    		// now enter acessEntry as first in LRU list
            if (first != null) {
                // map is not empty, otherwise first would be null
                accessEntry.next = first;
                first.prev = accessEntry;
                first = accessEntry;
            } else {
                // if map is empty Entry is first and last
                first = accessEntry;
                last = accessEntry;
            }
    
            assert checkLists() : "List out of order (post)";
    
            // start LRU removal if map to large
            if (maxCut != 0 && size() > maxCut) {
                removeLRU();
            }
        }
        
        // Note: Must not happen while being synchronized on this.
        if (register && minTime != 0) {
        	watcher.register(this);
        }
        
        if (oldValue != null && oldValue != aValue)
            removed(aKey, oldValue);
        // return result of map.put() map.put
        return oldValue;
    }

    /** 
     * Copies all of the mappings from the specified 
	 * map to this map (optional operation).
	 */
	@Override
	public void putAll(Map t) {
		// Just use put for each entry in map t
		Iterator i = t.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			put(e.getKey(), e.getValue());
		}
	}

	/** 
	 * Removes the mapping for this key from this map if present.
	 */
	@Override
	public synchronized Object remove(Object key) {

		// get the access entry for the Object to be removed
		LinkedLRUMapValue accessEntry = (LinkedLRUMapValue) access.remove(key);

		// update LRU list
        if (accessEntry != null) {

            assert checkLists() : "List out of order (pre)";

			if (first != accessEntry) {
				if (last != accessEntry) {     // first -> x - (key) - x <- last 
					accessEntry.next.prev = accessEntry.prev;
					accessEntry.prev.next = accessEntry.next;
				} else {                       // first -> x - (key) <- last
					last = accessEntry.prev;
					accessEntry.prev.next = null;
				}
			} 
            //  accessEntry == first
            else if (last != accessEntry) {  // first -> (key) - x <- last
                if (accessEntry.next == null)
                    Logger.fatal("Linked list out of order (middle)", this);
                else {
                    first = accessEntry.next;
                    first.prev = null;
                }
			} else {                           // first -> (key) <- last
				first = last = null;
			}

            assert checkLists() : "List out of order (pre)";

            removed(accessEntry.key, accessEntry.value);
            return accessEntry.value;
		}
		return null;
	}

   /** Hook for subclasses (e.g. to close() Cached objects).
     * 
     * This fucntion should <em>not</em> be called from a (known) synchronized
     * block so subclasses are free to do theire own locking. It will only
     * called on explicit removal not on calls to {@link #clear()} or such.
     *
     * @param key the key that object was bound to.  
     * @param o   the removed Object, not part of the Map any more.  
     */
    protected void removed(Object key, Object o) {
        // override as you see fit
    }

    /** 
     * Removes all mappings from this map (optional operation).
     * 
     * AbstractMap uses (unssupported) entrySet() for clear().
     */
    @Override
	public void clear() {
        access.clear();
        first = null;
        last  = null;
    }

    /** 
	 * Returns the number of key-value mappings in this map. 
	 */
	@Override
	public int size() {
		// just for forward to actual map
		return access.size();
	}

	/** 
	 * Returns a collection view of 
	 * the values contained in this map. 
	 * 
	 * Expect ConcurrentModificationExceptions when using Iterators on 
	 * keys, values and entries due to the regular removal of lru entries.
	 */
	@Override
	public Collection values() {
		// just for forward to actual map
		return new LRUValuesCollection(access.values());
	}

	/**
	 * Returns the next Time an Entry expires.
	 * 
	 * @return the next time an Entry expires,
	 * 			0 if the map is empty.
	 */
	@Override
	public synchronized long nextExpiration () {
        LinkedLRUMapValue val = last;
		if (val != null) 
            return val.time + minTime;
		// else
		return 0;
	}

    /**
     * @see com.top_logic.basic.col.LRU#removeExpired()
     */
    @Override
	public synchronized long removeExpired() {
        // if minTime is 0 entries don't expire
        if (minTime <= 0) {
            throw new IllegalStateException("Entries are not removed from an LRU map with out expiration.");
        }
        // else
        // set the actual expiration time
        long expTime = System.currentTimeMillis() - minTime;
        // remove the lru entry until it is not expired
        while ((last != null) && (last.time <= expTime)) {
            removeLRU();
        }
        
        return nextExpiration();
    }

	/**
	 * Removes the least recently used Entry.
     * 
     * (Call for synchronized functions only !)
     * 
     * @return the Object actually removed
	 */
	private Object removeLRU() {
        Object            result = null;
        LinkedLRUMapValue rem    = null;
        rem = last;
		if (size() > 1) {
			// typical case (not all entries are expired)
			last      = rem.prev;
			last.next = null;
            rem       = (LinkedLRUMapValue) access.remove(rem.key);
            result    = rem.value;
		} 
        else if (size() == 1) {
			// in case all entries are expired
			access.remove(rem.key);
            result = rem.value;
			first = null;
			last = null;
		}
        assert checkLists() : "List out of order";
        removed(rem.key, result);
        return result;
		// otherwise the map is empty (nothing to do
	}

    /** Accessor to maxCut: maximim size to remove a entry */
    public int getMaxCut() {
        return maxCut;
    }
    
    /** Accessor to maxCut: minimum time to remove a entry*/
    public long getMinTime() {
        return minTime;
    }    

    /** 
     * Class to hold access time information on Map entries. 
     */
    static class LinkedLRUMapValue {
    	
        /** The key of the mapping */
        Object key;
        
        /** The value assigned with the key */
        Object value;
        
        /** Recencently used (RU-) time. */
        long time;
        
        /** next older Entry */
        LinkedLRUMapValue next;
        
        /** next younger Entry */
        LinkedLRUMapValue prev;
        
        /**
         * Constructor for LinkedLRUMapValue
         * 
         * @param aKey  the Key of the map Entry
         * @param aTime the access time
         * @param aPrev the prev Entry (larger time)
         * @param aNext the next Entry (smaller time)
         */
		LinkedLRUMapValue(
			Object aKey, Object aValue,
			long aTime, LinkedLRUMapValue aPrev,
			            LinkedLRUMapValue aNext) {

            key = aKey;
            value = aValue;
            time = aTime;
            prev = aPrev;
            next = aNext;
        }

        /**
         * equals() method based on the value attribute.
         */
        @Override
		public boolean equals(Object anObject) {
	    	if (anObject == this) {
	    		return true;
	    	}
	    	if (anObject instanceof LinkedLRUMapValue) {
	    		return value.equals(((LinkedLRUMapValue)anObject).value);
	    	}
	    	return false;
        }
    }
	/**
	 * Set representation of the keys
	 */
	class LRUKeySet implements Set {
        
		/**
		 * The KeySet returned from access map.
		 */
		Set baseSet;
        
		/**
		 * CTor forthe LRUKeySet
		 */
		LRUKeySet (Set aBaseSet) {
			baseSet = aBaseSet;
		}
		/**
		 * @see java.util.Collection#add(Object)
		 */
		@Override
		public boolean add(Object arg0) {
			throw new UnsupportedOperationException();
		}
		/**
		* @see java.util.Collection#addAll(Collection)
		*/
		@Override
		public boolean addAll(Collection arg0) {
			throw new UnsupportedOperationException();
		}
		/**
		* @see java.util.Collection#clear()
		*/
		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}
		/**
		* @see java.util.Collection#contains(Object)
		*/
		@Override
		public boolean contains(Object arg0) {
			return baseSet.contains(arg0);
		}
		/**
		* @see java.util.Collection#containsAll(Collection)
		*/
		@Override
		public boolean containsAll(Collection arg0) {
			return baseSet.containsAll(arg0);
		}
		/**
		* @see java.util.Collection#isEmpty()
		*/
		@Override
		public boolean isEmpty() {
			return baseSet.isEmpty();
		}
		/**
		* @see java.util.Collection#iterator()
		*/
		@Override
		public Iterator iterator() {
			return new LRUKeySetIterator(baseSet.iterator());
		}
		/**
		* @see java.util.Collection#remove(Object)
		*/
		@Override
		public boolean remove(Object arg0) {
			throw new UnsupportedOperationException();
		}
		/**
		* @see java.util.Collection#removeAll(Collection)
		*/
		@Override
		public boolean removeAll(Collection arg0) {
			throw new UnsupportedOperationException();
		}
		/**
		* @see java.util.Collection#retainAll(Collection)
		*/
		@Override
		public boolean retainAll(Collection arg0) {
			throw new UnsupportedOperationException();
		}
		/**
		* @see java.util.Collection#size()
		*/
		@Override
		public int size() {
			return baseSet.size();
		}
		/**
		* @see java.util.Collection#toArray()
		*/
		@Override
		public Object[] toArray() {
			return baseSet.toArray();
		}
		/**
		* @see java.util.Collection#toArray(Object[])
		*/
		@Override
		public Object[] toArray(Object[] arg0) {
			return baseSet.toArray(arg0);
		}
	}
    
	/**
	 * LRUKeySetIterator takes care of correct removal of elements.
     * 
     * The remove function does not work as found
     * in other Iterators. 
	 */
	class LRUKeySetIterator implements Iterator {
		/** the Itreator returned from LRUEntrySet */
		Iterator it;
		/** the last returned object. */
		Object  lastObj;
		/** remove() legal */
		boolean legal;
		
		public LRUKeySetIterator (Iterator anIt) {
			it = anIt;
		}
		@Override
		public boolean hasNext() {
			return it.hasNext();
		}
		@Override
		public Object next() {
			lastObj = it.next();
			legal = true;
			return lastObj;
		}
		@Override
		public void remove() {
			if (legal) {
			    LRUMap.this.remove(lastObj);
			    legal = false;
			} else throw new IllegalStateException();
		}
	}
	
	/**
	 * Collection representation of the values
	 */
	class LRUValuesCollection implements Collection {
		/**
		 * The values Collection returned from access map.
		 */
		Collection baseCol;
		/**
		 * CTor forthe LRUEntrySet
		 */
		LRUValuesCollection (Collection aBaseCol) {
			baseCol = aBaseCol;
		}
		/**
		 * @see java.util.Collection#add(Object)
		 */
		@Override
		public boolean add(Object arg0) {
			throw new UnsupportedOperationException();
		}
		/**
		* @see java.util.Collection#addAll(Collection)
		*/
		@Override
		public boolean addAll(Collection arg0) {
			throw new UnsupportedOperationException();
		}
		/**
		* @see java.util.Collection#clear()
		*/
		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}
		/**
		* @see java.util.Collection#contains(Object)
		*/
		@Override
		public boolean contains(Object arg0) {
			return baseCol.contains(new LinkedLRUMapValue(null, arg0, 0, null, null));
		}
        /**
         * @see java.util.Collection#containsAll(Collection)
         */
        @Override
		public boolean containsAll(Collection arg0) {
            Iterator e = arg0.iterator();
            while (e.hasNext()) {
                if(!contains(e.next())) {
                    return false;
                }
            }
            return true;
        }
		/**
		* @see java.util.Collection#isEmpty()
		*/
		@Override
		public boolean isEmpty() {
			return baseCol.isEmpty();
		}
		/**
		* @see java.util.Collection#iterator()
		*/
		@Override
		public Iterator iterator() {
			return new LRUValuesCollectionIterator(baseCol.iterator());
		}
		/**
		* @see java.util.Collection#remove(Object)
		*/
		@Override
		public boolean remove(Object arg0) {
			throw new UnsupportedOperationException();
		}
		/**
		* @see java.util.Collection#removeAll(Collection)
		*/
		@Override
		public boolean removeAll(Collection arg0) {
			throw new UnsupportedOperationException();
		}
		/**
		* @see java.util.Collection#retainAll(Collection)
		*/
		@Override
		public boolean retainAll(Collection arg0) {
			throw new UnsupportedOperationException();
		}
		/**
		* @see java.util.Collection#size()
		*/
		@Override
		public int size() {
			return baseCol.size();
		}
		/**
		* @see java.util.Collection#toArray()
		*/
		@Override
		public Object[] toArray() {
			return baseCol.toArray();
		}
		/**
		* @see java.util.Collection#toArray(Object[])
		*/
		@Override
		public Object[] toArray(Object[] arg0) {
			return baseCol.toArray(arg0);
		}
	}
	/**
	 * LRUBaseSetIterator takes care of correct removal of elements.
     * 
     * The remove function does not work as found in other Iterators. 
	 */
	class LRUValuesCollectionIterator implements Iterator {
		/** the Itreator returned from LRUEntrySet */
		Iterator it;
		/** the last returned object. */
		LinkedLRUMapValue lastValue;
		
		public LRUValuesCollectionIterator (Iterator anIt) {
			it = anIt;
		}
		@Override
		public boolean hasNext() {
			return it.hasNext();
		}
		@Override
		public Object next() {
			lastValue = (LinkedLRUMapValue)it.next();
			return lastValue.value;
		}
		@Override
		public void remove() throws IllegalStateException {
			if (null != lastValue) {
			    LRUMap.this.remove(lastValue.key);
			    lastValue = null;
			} else throw new IllegalStateException();
		}
	}
    
    /** This method is used in asserts and eventually testing only 
     *
     * @return true when List is OK. false indicates an inconsistency 
     */
    public synchronized boolean checkLists() {
        
        if (first == null) {
            return last == null;
        }
        
        int                 count = 0;
        int                 size  = access.size();
        LinkedLRUMapValue   curr  = first;
        LinkedLRUMapValue   prev  = curr.prev;
        LinkedLRUMapValue   next  = curr.next;
        while (curr != null && count <= size) {
            if (prev != null && prev.next != curr)
                return false; // Must never happen, cannot legally be covered
            if (next != null && next.prev != curr)
                return false; // Must never happen, cannot legally be covered
            if (next == null)
                break;
            
            curr = next;
            prev = curr.prev;
            next = curr.next;
            count ++;
        }
        return count <= size     // more in list than im Map ?
            && curr  == last;    // Last in list not known last ?

    }
}
