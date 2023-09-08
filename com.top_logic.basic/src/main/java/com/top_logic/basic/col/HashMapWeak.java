/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.top_logic.basic.NamedConstant;

/**
 * Implements a HasMap with Weak values in contrast to
 * {@link java.util.WeakHashMap}.
 *
 * To Do so you must implement a method to retrieve the key for a given
 * value since the Object cannot be removed otherwise. As fo the WeakHashmap
 * this class has some unxpected features. You should not use Iterators and
 * such since they may lead to ConcurrentModificationExceptions.
 *
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class HashMapWeak<K, V> extends AbstractMap<K, V> implements Cloneable {

	/**
	 * Storage map for wrapped data.
	 */
	/*package protected*/ final HashMap<K, WeakValue<K, V>> data;
	
    /** This Queue is used internally to remove the object as soon any call
        is made to this class */
	/*package protected*/ ReferenceQueue<V> refQ = new ReferenceQueue<>();

	/** Non-weak view of the entries of this map */
	private Set<Entry<K, V>> entrySetView;


    /** Constructs a new, empty map with a default capacity and load factor, which is 0.75.  */
    public HashMapWeak () {
        this.data = new HashMap<>();
    }
          
    /**  Constructs a new, empty map with the specified initial capacity and default load factor, which is 0.75.   */
    public HashMapWeak (int initialCapacity) { 
    	this.data = new HashMap<>(initialCapacity);
    }

    /** Constructs a new, empty map with the specified initial capacity and the specified load factor. */
    public HashMapWeak (int initialCapacity, float loadFactor) { 
    	this.data = new HashMap<>(initialCapacity, loadFactor);
    }
           
    // Internal Management of the References
    
    /** Cleanup all WeakValues that where removed by last sweep of the GC. */
    protected void cleanup() {
    	WeakValue<?, ?> wv = (WeakValue<?, ?>) refQ.poll();
        while (wv != null) {
            WeakValue<K, V> removed = data.remove(wv.key);
            
            // Make sure that collected references only drop themselves from the underlying map.
            assert removed == null || removed == wv;
            
            wv = (WeakValue<?, ?>) refQ.poll();
        }
    }

    /** Returns true if this map maps one or more keys to the specified value.
     * 
     * Be Aware, that values may "vanish" so the reuslt of this
     * function is not stable. It has linear complexity, too.
     * 
     */
    @Override
	public boolean containsValue(Object value)  {
        cleanup();  // remove old Values first.
        Iterator<WeakValue<K, V>> it = data.values().iterator();
        while (it.hasNext()) {
            WeakValue<K, V> wv = it.next();
            V val = wv.get();
            if (val == value || (val != null && val.equals(value))) {
                return true;
            }
        }
        return false;
    }
           
    @Override
	public Set<Entry<K, V>> entrySet()  {
		if (entrySetView == null) {
			this.entrySetView = new StableEntrySet();
		}
		return entrySetView;
    }
    
    /** Returns the value to which this map maps the specified key. */
    @Override
	public V get(Object key)  {
        cleanup();  // remove old Values first.
        WeakValue<K, V> wv = data.get(key);
        
		if (wv == null) {
		    return null;
		}
        
		return wv.get();
    }

    /** Overriden to use WeakValue as wrapper around the actual value. */
    @Override
	public V put(K key, V value)  {
        cleanup();  // remove old Values first.
        WeakValue<K, V> wv = data.put(key, new WeakValue(key, value, refQ));
        
		if (wv == null) {
		    return null;
		}
		
		wv.destroy();
		return wv.get();
    }

     /** Removes the mapping for this key from this map if present. */
    @Override
	public V remove(Object key)  {
        cleanup();  // remove old Values first.
        WeakValue<K, V> wv = data.remove(key);
        
		if (wv == null) {
		    return null;
		}
        
		wv.destroy();
		return wv.get();
    }

     /** May cleanup some Object when called. */ 
     @Override
	public int size()  {
         cleanup();             // remove old Values first.
         return data.size();
     }

	@Override
	public boolean isEmpty() {
        cleanup();
		return data.isEmpty();
	}
	
	@Override
	public Set<K> keySet() {
        cleanup();
		return data.keySet();
	}
	
    @Override
	public boolean containsKey(Object key) {
        cleanup();
    	return data.containsKey(key);
    }

    @Override
	public void clear() {
    	data.clear();
    }
    
	@Override
	public void putAll(Map<? extends K, ? extends V> t) {
		for (Entry<? extends K, ? extends V> entry : t.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
     * Non-weak view of the entry set of a {@link HashMapWeak}.
     * 
     * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
     */
    private final class StableEntrySet extends AbstractSet<Entry<K, V>> {
    	
		public StableEntrySet() {
			super();
		}

		@Override
		public Iterator<java.util.Map.Entry<K, V>> iterator() {
			return new EntryIterator(data.entrySet().iterator());
		}

		@Override
		public int size() {
			return data.size();
		}

		/**
		 * {@link Iterator} of an {@link StableEntrySet} that ensures
		 * that the current value is not garbage collected.
		 * 
		 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
		 */
		private final class EntryIterator implements Iterator<Entry<K, V>> {
			
			private final Iterator<Entry<K, WeakValue<K, V>>> innerIterator;

			private Entry<K, V> stableEntry;

			public EntryIterator(Iterator<Entry<K, WeakValue<K, V>>> innerIterator) {
				this.innerIterator = innerIterator;
			}

			@Override
			public boolean hasNext() {
				if (stableEntry == null) {
					findNext();
				}
				return stableEntry != null;
			}

			@Override
			public java.util.Map.Entry<K, V> next() {
				if (! hasNext()) {
					throw new NoSuchElementException();
				}
				
				Entry<K, V> result = stableEntry;
				stableEntry = null;
				
				return result;
			}

			@Override
			public void remove() {
				innerIterator.remove();
			}

			private void findNext() {
				while (innerIterator.hasNext()) {
					final java.util.Map.Entry<K, WeakValue<K, V>> innerEntry = innerIterator.next();
					
					// Make hard reference.
					final V innerValue = innerEntry.getValue().get();
					
					if (innerValue != null) {
						stableEntry = new StableEntry(innerValue, innerEntry);
						break;
					}
				}
			}

			/**
			 * {@link java.util.Map.Entry} that ensures that the value is not garbage collected.
			 * 
			 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
			 */
			private final class StableEntry implements Entry<K, V> {
				private final Entry<K, WeakValue<K, V>> innerEntry;

				private V stableValue;

				public StableEntry(V innerValue, Entry<K, WeakValue<K, V>> innerEntry) {
					this.innerEntry = innerEntry;
					this.stableValue = innerValue;
				}

				@Override
				public K getKey() {
					return innerEntry.getKey();
				}

				@Override
				public V getValue() {
					return stableValue;
				}

				@Override
				public V setValue(V newValue) {
					V oldValue = this.stableValue;
					
					stableValue = newValue;
					WeakValue<K, V> disposed = innerEntry.setValue(new WeakValue<>(getKey(), newValue, refQ));
					if (disposed != null) {
						disposed.clear();
					}
					
					return oldValue;
				}
				
				@Override
				public int hashCode() {
					return hashCodeEntry(this);
				}
				
				@Override
				public boolean equals(Object obj) {
			    	if (obj == this) {
			    		return true;
			    	}
					if (! (obj instanceof Entry<?, ?>)) {
						return false;
					}
					
					return equalsEntry(this, (Entry<?, ?>) obj);
				}

			}
		}
	}

    /*package protected*/ static int hashCodeEntry(Entry<?, ?> e1) {
		Object key = e1.getKey();
		Object value = e1.getValue();
		
		return
			(key == null ? 0 : key.hashCode()) ^
			(value == null ? 0 : value.hashCode());
    }

    /*package protected*/ static boolean equalsEntry(Entry<?, ?> e1, Entry<?, ?> e2) {
    	Object key1 = e1.getKey();
    	Object key2 = e2.getKey();
    	Object value1 = e1.getValue();
    	Object value2 = e2.getValue();
    	return 
    	(key1 == null ? key2 == null : key1.equals(e2.getKey()))  &&
    	(value1 == null ? value2 == null : value1.equals(e2.getValue()));
    }
    

    /** The Values actually stored in this HashTable are of this Type.
     *
     * This class extracts the key to be able to remove the mapping later.
     */
    private static class WeakValue<K, V> extends WeakReference<V>
    {
        private static final Object DESTROYED = new NamedConstant("DESTROYED");
        
        /** Key extracted from the original value on creation */
        Object key;

        /** CTor extracts key from Value to be able to remove Mapping later */        
        public WeakValue(K aKey, V value, ReferenceQueue<? super V> aRefQ) {
            super(value, aRefQ);
            
            if (value == null) {
            	throw new IllegalArgumentException("Must not add null values to a '" + HashMapWeak.class + "'.");
            }
            
            this.key = aKey;
        }

        /**
		 * Mark this reference as disposed.
		 * 
		 * <p>
		 * A disposed reference must not drop current entries with the same key
		 * from the map.
		 * </p>
		 */
		void destroy() {
			key = DESTROYED;
		}
    
    }
}
