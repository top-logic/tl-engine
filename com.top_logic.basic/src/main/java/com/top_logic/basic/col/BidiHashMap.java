/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MapIterator;

/**
 * Optimized {@link BidiMap} implementation using single hash array and only as many
 * {@link java.util.Map.Entry} objects as the {@link #size()} of the map.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BidiHashMap<K, V> extends AbstractMap<K, V> implements BidiMap<K, V> {
	
	/**
	 * Produces inverse {@link BidiHashMap.BidiEntry}s.
	 */
	/* package protected */static final Mapping<Entry<Object, Object>, Entry<Object, Object>> REVERSE_ENTRIES =
		new ReverseMapping<>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <K, V> Mapping<Entry<K, V>, Entry<V, K>> reverseMapping() {
		return (Mapping) REVERSE_ENTRIES;
	}

	static final class ReverseMapping<K, V> implements Mapping<Entry<K, V>, Entry<V, K>> {
		@Override
		public Entry<V, K> map(Entry<K, V> input) {
			BidiEntry<K, V> e = (BidiEntry<K, V>) input;
			return newEntry(e.destination, e.destHash, e.source, e.sourceHash);
		}
	}
	
	/**
	 * Replacement for <code>null</code> values in source and destination
	 * entries of internal {@link BidiHashMap.BidiEntry}s.
	 */
	private static final Object NULL = new Object() {
		@Override
		public boolean equals(Object obj) {
			return obj == this;
		}
		
		@Override
		public int hashCode() {
			return 0;
		}
		
		@Override
		public String toString() {
			return "null";
		}
	};

	/**
	 * The initial capacity of maps constructed by the default constructor.
	 * 
	 * <p>
	 * The internal hash table always has room for at least this number of
	 * mappings.
	 * </p>
	 */
	private static final int INITIAL_CAPACITY = 16;

	/**
	 * The size of this map is guaranteed to be smaller than
	 * <code>{@link #hashModulus} * {@link #EXPAND_FILL_FACTOR}</code>.
	 */
	private static final float EXPAND_FILL_FACTOR = 1.2f;

	/**
	 * if {@link #hashModulus} is larger than {@link #INITIAL_CAPACITY}, then
	 * <code>{@link #hashModulus} * {@link #SHRINK_FILL_FACTOR}</code> is
	 * guaranteed to be smaller than the size of this map.
	 */
	private static final float SHRINK_FILL_FACTOR = 0.4f;
	
	/**
	 * The internal hash table is enlarged, if the {@link #size()} of this map
	 * becomes larger than {@link #maxSize} after an addition.
	 * 
	 * <p>
	 * Invariant: {@link #EXPAND_FILL_FACTOR} * {@link #hashModulus}
	 * </p>
	 */
	private int maxSize;

	/**
	 * The internal hash table is decreased, if the {@link #size()} of this map
	 * becomes smaller than {@link #minSize} after a removal.
	 * 
	 * <p>
	 * Invariant: <code>{@link #SHRINK_FILL_FACTOR} * {@link #hashModulus}</code>, if
	 * {@link #hashModulus} > {@link #INITIAL_CAPACITY}, <code>0</code>,
	 * otherwise.</p>
	 */
	private int minSize;

	/**
	 * The number of entries in this map.
	 */
	/*package protected*/ int size;
	
	private int hashModulus;
	
	/**
	 * The internal hash table.
	 * 
	 * <p>
	 * Its size is <code>2 * {@link #hashModulus}</code>. Even indices store
	 * source mappings, odd entries store destination mappings.
	 * </p>
	 */
	/* package protected */BidiEntry<K, V> table[];

	/**
	 * Modification counter to allow detecting concurrent modifications and
	 * throwing {@link ConcurrentModificationException}s.
	 */
	/*package protected*/ transient volatile int modCount;

	/**
	 * Cached lazy {@link #entrySet()} view of this map.
	 */
	private Set<Entry<K, V>> entrySet;

	/**
	 * Cached lazy {@link #keySet()} view of this map.
	 */
	private Set<K> keySet;

	/**
	 * Cached lazy {@link #values()} view of this map.
	 */
	private Set<V> valueSet;
	
	/**
	 * Cached lazy {@link #inverseBidiMap()} view of this map.
	 */
	private BidiMap<V, K> inverseMap;


	/**
	 * {@link Iterator} over the {@link BidiHashMap.BidiEntry entries} of this
	 * map.
	 * 
	 * <p>
	 * The result of the {@link #next()} method in concrete subclasses is
	 * defined by the
	 * {@link #toResult(com.top_logic.basic.col.BidiHashMap.BidiEntry)} hook.
	 * </p>
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	/* package protected */abstract class AbstractEntryIterator<X> implements Iterator<X> {
		int nextIndex = sourceIndex(0);

		boolean hasValue;

		BidiEntry<K, V> e;
		
		/**
		 * Copy of {@link BidiHashMap#modCount} at the time of iterator
		 * creation.
		 * 
		 * <p>
		 * In iterator access, equality of {@link #expectedModCount} and
		 * {@link BidiHashMap#modCount} ensures that during iteration no
		 * concurrent modification occurred in the owner map.
		 * </p>
		 */
		int expectedModCount = modCount;

		BidiEntry<K, V> current;
		{
			findNext();
		}

		private void findNext() {
            checkModCount();
            
			if (e != null) {
				e = e.nextSource;
				
				if (e != null) {
					hasValue = true;
					return;
				}
			}
			
			while (nextIndex < table.length) {
				e = table[nextIndex];
				nextIndex += 2;
				
				if (e != null) {
					hasValue = true;
					return;
				}
			}
		}

		/**
		 * Assert that no concurrent modification was performed.
		 * 
		 * @return The current value of {@link BidiHashMap#modCount}.
		 */
		private int checkModCount() {
			int currentModCount = modCount;
			if (currentModCount != expectedModCount) {
            	throw new ConcurrentModificationException();
            }
			return currentModCount;
		}
		
		private void incModCount() {
			int currentModCount = checkModCount();
			
			// Update counter and local copy.
			currentModCount++;
			modCount = currentModCount;
			expectedModCount = currentModCount;
		}

		@Override
		public boolean hasNext() {
			if (! hasValue) {
				findNext();
			}
			
			return hasValue;
		}

		@Override
		public X next() {
			if (! hasValue) {
				// hasNext() might have not been called.
				findNext();
				
				if (! hasValue) {
					throw new NoSuchElementException();
				}
			}
			
			current = e;
			hasValue = false;
			
			return toResult(current);
		}

		/**
		 * Defines the result of calls to {@link #next()}.
		 * 
		 * @param be
		 *        The current {@link BidiHashMap.BidiEntry}.
		 * @return The value that {@link #next()} should return for the given entry.
		 */
		protected abstract X toResult(BidiEntry<K, V> be);

		@Override
		public void remove() {
            incModCount();
            
			if (current == null) {
				throw new IllegalStateException();
			}
			
			BidiEntry<K, V> nextSource = current.nextSource;
			
			removeDestinationEntry(current.destination, current.destHash, destIndex(current.destHash));
			// Note: (nextIndex - 2) must not be used as source index, because
			// hasNext() might have been called before remove(). This could have
			// already moved nextIndex.
			removeSourceEntry(current, sourceIndex(current.sourceHash));
			
			size--;
			
			// Must not rehash during iteration. Otherwise, entries might be
			// skipped or duplicated in the iteration.
//			checkCapacityRemove();
			
			// There is no longer a current entry in this iterator, because it
			// is removed. Only the next call to next() will fetch the next
			// current value.
			current = null;
			
			// The next outstanding value moves to the next entry in the source
			// chain. The next call to next() must not move the pointer again, but
			// return this entry (if there is one). Therefore, hasValue is
			// swapped to true, if there is an entry in the source chain of the
			// current entry. This simulates an implicit call to hasNext() in
			// case there is another entry in the source chain.
			//
			// Note: This must only be done, if hasNext() has not been called
			// before remove(). Otherwise, the iteration pointer would
			// eventually moved.
			if (! hasValue) {
				e = nextSource;
				hasValue = nextSource != null;
			}
		}
		
		protected final K internalKey() {
			if (current == null) {
				throw new IllegalStateException();
			}

			return externalize(current.source);
		}
		
		protected final V internalValue() {
			if (current == null) {
				throw new IllegalStateException();
			}

			return externalize(current.destination);
		}
		
		protected final V internalSetValue(V value) {
            incModCount();
            
			if (current == null) {
				throw new IllegalStateException();
			}

			V newDestination = internalize(value);
			int newDestHash = hash(newDestination);
			int newDestIndex = destIndex(newDestHash);
			
			V oldValue = externalize(current.destination);
			
			if (BidiHashMap.equals(current.destination, newDestination)) {
				return oldValue;
			}
			
			BidiEntry<K, V> oldBackEntry = getDestinationEntry(newDestination, newDestHash, newDestIndex);
			if (oldBackEntry != null) {
                throw new IllegalArgumentException("Cannot use setValue() when the object being set is has already a mapping.");
			}
			
			int oldDestHash = current.destHash;
			int oldDestIndex = destIndex(oldDestHash);
			removeDestinationEntry(current, oldDestIndex);
			
			current.destination = newDestination;
			current.destHash = newDestHash;
			
			insertDestinationEntry(current, newDestIndex);
			
			return oldValue;
		}
	}

	class EntryIterator extends AbstractEntryIterator<K> implements MapIterator<K, V> {

		@Override
		protected K toResult(BidiEntry<K, V> be) {
			return externalize(be.source);
		}

		@Override
		public K getKey() {
			return internalKey();
		}

		@Override
		public V getValue() {
			return internalValue();
		}

		@Override
		public V setValue(V value) {
			return internalSetValue(value);
		}
	}

	class ReverseEntryIterator extends AbstractEntryIterator<V> implements MapIterator<V, K> {

		@Override
		protected V toResult(BidiEntry<K, V> be) {
			return externalize(be.destination);
		}

		@Override
		public V getKey() {
			return internalValue();
		}

		@Override
		public K getValue() {
			return internalKey();
		}

		@Override
		public K setValue(K value) {
			// Not supported, because this would require a link
			// from each hash entry back to the owning map. This is
			// waste of space.
			throw new UnsupportedOperationException("Use BidiHashMap.mapIterator().setValue().");
		}
	}

	/**
	 * Internal hash entry of {@link BidiHashMap}s. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static class BidiEntry<K, V> implements Entry<K, V> {
		/**
		 * The {@link BidiHashMap#internalize(Object) internalized} {@link #getKey()} value.
		 */
		/* package protected */K source;
		
		/**
		 * The {@link BidiHashMap#internalize(Object) internalized} {@link #getValue()} value.
		 */
		/* package protected */V destination;
		
		/**
		 * Internal hash value of the {@link #source}.
		 */
		/*package protected*/ int sourceHash;
		
		/**
		 * Chain of entries with hash clashes for the {@link #source}.
		 */
		/* package protected */BidiEntry<K, V> nextSource;
		
		/**
		 * Internal hash value of the {@link #destination}.
		 */
		/*package protected*/ int destHash;
	
		/**
		 * Chain of entries with hash clashes for the {@link #destination}.
		 */
		/* package protected */BidiEntry<K, V> nextDestination;

		
		public BidiEntry(K source, int sourceHash, V destination, int destHash) {
			this.source = source;
			this.sourceHash = sourceHash;
			this.destination = destination;
			this.destHash = destHash;
		}

		@Override
		public K getKey() {
			return externalize(source);
		}

		@Override
		public V getValue() {
			return externalize(destination);
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}
		
        @Override
		public boolean equals(Object other) {
        	if (other == this) {
        		return true;
        	}
            if (! (other instanceof Entry)) {
            	return false;
            }
 
			Entry<?, ?> e1 = this;
			Entry<?, ?> e2 = (Entry<?, ?>) other;
            
			Object k1 = e1.getKey();
			Object k2 = e2.getKey();
			Object v1 = e1.getValue();
			Object v2 = e2.getValue();
			
			return  (k1 == null ? k2 == null : k1.equals(k2)) &&
            	    (v1 == null ? v2 == null : v1.equals(v2));
        }
    
        @Override
		public int hashCode() {
            return source.hashCode() ^ destination.hashCode();
        }
        
        @Override
		public String toString() {
        	return source + "=" + destination;
        }
	}
	
	/**
	 * Constructs a new {@link BidiHashMap} with initial capacity.
	 */
	public BidiHashMap() {
		this(INITIAL_CAPACITY);
	}

	/**
	 * Creates a bidirectional mapping from the given source map.
	 * 
	 * <p>
	 * If the source map associates multiple keys to the same value, it is not
	 * defined, which of these mappings survive.
	 * </p>
	 * 
	 * @param source
	 *        The map to make bidirectional.
	 */
	public BidiHashMap(Map<? extends K, ? extends V> source) {
		this(source.size());
		
		internalPutAll(source);
		
		checkCapacity();
	}

	/**
	 * Creates a new {@link BidiHashMap} that has room for inserting the given
	 * number of mappings.
	 * 
	 * @param expectedSize
	 *        The expected number of mappings to insert.
	 */
	public BidiHashMap(int expectedSize) {
		this.hashModulus = increaseHashModulus(INITIAL_CAPACITY, expectedSize);
		this.table = allocate(2 * hashModulus);

		initRange(hashModulus);
	}
	
	private void initRange(int newHashModulus) {
		this.maxSize = (int) (EXPAND_FILL_FACTOR * newHashModulus);
		this.minSize = newHashModulus > INITIAL_CAPACITY ? (int) (SHRINK_FILL_FACTOR * newHashModulus) : 0;
	}

	
	@Override
	public K getKey(Object value) {
		Object internalValue = internalize(value);
		int destHash = hash(internalValue);
		
		int lookupIndex = destIndex(destHash);
		
		BidiEntry<K, V> e = table[lookupIndex];
		while (e != null) {
			if (e.destHash == destHash && equals(internalValue, e.destination)) {
				return externalize(e.source);
			}
			
			e = e.nextDestination;
		}
		
		return null;
	}

	/*package protected*/ static boolean equals(Object a, Object b) {
		return a == b || a.equals(b);
	}

	/*package protected*/ final int sourceIndex(int sourceHash) {
		return 2 * index(sourceHash);
	}

	/*package protected*/ final int destIndex(int destHash) {
		return 2 * index(destHash) + 1;
	}
	
	private int index(int destHash) {
		return destHash % hashModulus;
	}
	
	/*package protected*/ static int hash(Object value) {
		// Non-negative hash code.
		return 0x7FFFFFFF & value.hashCode();
	}

	@Override
	public BidiMap<V, K> inverseBidiMap() {
		if (inverseMap == null) {
			inverseMap = new BidiMap<>() {
				private Set<Entry<V, K>> inverseEntrySet;
				
				@Override
				public V getKey(Object value) {
					return BidiHashMap.this.get(value);
				}

				@Override
				public BidiMap<K, V> inverseBidiMap() {
					return BidiHashMap.this;
				}

				@Override
				public MapIterator<V, K> mapIterator() {
					return new ReverseEntryIterator();
				}

				@Override
				public K put(V value, K key) {
					K oldKey = BidiHashMap.this.getKey(value);
					// Note: Put delivers the old value, but here the old key is needed.
					BidiHashMap.this.put(key, value);
					return oldKey;
				}

				@Override
				public V removeValue(Object value) {
					return BidiHashMap.this.remove(value);
				}

				@Override
				public void clear() {
					BidiHashMap.this.clear();
				}

				@Override
				public boolean containsKey(Object key) {
					return BidiHashMap.this.containsValue(key);
				}

				@Override
				public boolean containsValue(Object value) {
					return BidiHashMap.this.containsKey(value);
				}

				@Override
				public Set<Entry<V, K>> entrySet() {
					if (inverseEntrySet == null) {
						inverseEntrySet = new AbstractSet<>() {
							@Override
							public Iterator<Entry<V, K>> iterator() {
								Iterator<Entry<K, V>> entryIt = BidiHashMap.this.entrySet().iterator();
								Mapping<Entry<K, V>, Entry<V, K>> reverseMapping = reverseMapping();
								return new MappingIterator<>(reverseMapping, entryIt);
							}
	
							@Override
							public int size() {
								return BidiHashMap.this.size();
							}

							@Override
							public boolean contains(Object o) {
								if (! (o instanceof Entry)) {
									return false;
								}
								
								Entry<?, ?> e = (Entry<?, ?>) o;
								
								Object searchSource;
								int searchSourceHash;
								Object searchDestination;
								if (e instanceof BidiEntry) {
									BidiEntry<?, ?> ownEntry = (BidiEntry<?, ?>) e;
									searchSource = ownEntry.destination;
									searchSourceHash = ownEntry.destHash;
									
									searchDestination = ownEntry.source;
								} else {
									searchSource = internalize(e.getValue());
									searchSourceHash = hash(searchSource);
									
									searchDestination = internalize(e.getKey());
								}
								
								return containsMapping(searchSource, searchSourceHash, searchDestination);
							}
							
							@Override
							public boolean remove(Object o) {
								if (! (o instanceof Entry)) {
									return false;
								}
								
								Entry<?, ?> e = (Entry<?, ?>) o;
								
								Object searchSource;
								int searchSourceHash;
								Object searchDestination;
								if (e instanceof BidiEntry) {
									BidiEntry<?, ?> ownEntry = (BidiEntry<?, ?>) e;
									searchSource = ownEntry.destination;
									searchSourceHash = ownEntry.destHash;
									
									searchDestination = ownEntry.source;
								} else {
									searchSource = internalize(e.getValue());
									searchSourceHash = hash(searchSource);
									
									searchDestination = internalize(e.getKey());
								}
								
								return removeMapping(searchSource, searchSourceHash, searchDestination);
							}
							
							@Override
							public void clear() {
								BidiHashMap.this.clear();
							}
						};
					}
					
					return inverseEntrySet;
				}

				@Override
				public K get(Object key) {
					return BidiHashMap.this.getKey(key);
				}

				@Override
				public boolean isEmpty() {
					return BidiHashMap.this.isEmpty();
				}

				@Override
				public Set<V> keySet() {
					return BidiHashMap.this.values();
				}

				@Override
				public void putAll(Map<? extends V, ? extends K> t) {
					for (Entry<? extends V, ? extends K> otherEntry : t.entrySet()) {
						BidiHashMap.this.put(otherEntry.getValue(), otherEntry.getKey());
					}
				}

				@Override
				public K remove(Object key) {
					return BidiHashMap.this.removeValue(key);
				}

				@Override
				public int size() {
					return BidiHashMap.this.size();
				}

				@Override
				public Set<K> values() {
					return BidiHashMap.this.keySet();
				}
				
			};
		}
		
		return inverseMap;
	}

	@Override
	public MapIterator<K, V> mapIterator() {
		return new EntryIterator();
	}

	@Override
	public V put(K key, V value) {
		// IGNORE FindBugs(VO_VOLATILE_INCREMENT): Only "best effort" finding concurrent
		// modifications.
		modCount++;
		K source = internalize(key);
		V newDestination = internalize(value);
        
        int sourceHash = hash(source);
        int sourceIndex = sourceIndex(sourceHash);
        
        int newDestHash = hash(newDestination);
        int newDestIndex = destIndex(newDestHash);

		V oldValue;
		BidiEntry<K, V> e = getSourceEntry(source, sourceHash, sourceIndex);
        if (e != null) {
        	if (equals(e.destination, newDestination)) {
        		// No change.
        		return value;
        	} else {
            	// Remove potential old backward mapping.
        		internalRemoveDestination(newDestination, newDestHash, newDestIndex);
        	}
        	
        	int oldDestIndex = destIndex(e.destHash);
        	oldValue = externalize(e.destination);
        	
        	e.destination = newDestination;
        	e.destHash = newDestHash;
        	
        	if (newDestIndex != oldDestIndex) {
        		// Entry is also linked from another slot and destination slot
				// changes, remove from original slot.
        		removeDestinationEntry(e, oldDestIndex);
        		insertDestinationEntry(e, newDestIndex);
        	}

			checkCapacityRemove();
        } else {
        	// Remove potential old backward mapping.
    		internalRemoveDestination(newDestination, newDestHash, newDestIndex);

            oldValue = null;

        	insertNew(source, sourceHash, sourceIndex, newDestination, newDestHash, newDestIndex);
        	
        	size++;
        	checkCapacityAdd();
        }

        return oldValue;
	}

	private void insertNew(K source, int sourceHash, int sourceIndex, V destination, int destHash, int destIndex) {
		BidiEntry<K, V> e = newEntry(source, sourceHash, destination, destHash);
		
		insertEntry(e, sourceIndex, destIndex);
	}

	private void insertEntry(BidiEntry<K, V> e, int sourceIndex, int destIndex) {
		insertSourceEntry(e, sourceIndex);
		insertDestinationEntry(e, destIndex);
	}

	/* package protected */static <K, V> BidiEntry<K, V> newEntry(K source, int sourceHash, V destination, int destHash) {
		return new BidiEntry<>(source, sourceHash, destination, destHash);
	}

	private BidiEntry<K, V> internalRemoveDestination(Object destination, int destHash, int destIndex) {
		BidiEntry<K, V> e = removeDestinationEntry(destination, destHash, destIndex);
		if (e != null) {
			removeSourceEntry(e, sourceIndex(e.sourceHash));
			size--;
		}
		return e;
	}

	private BidiEntry<K, V> internalRemoveSource(Object source, int sourceHash, int sourceIndex) {
		BidiEntry<K, V> e = removeSourceEntry(source, sourceHash, sourceIndex);
		if (e != null) {
			removeDestinationEntry(e, destIndex(e.destHash));
			size--;
		}
		return e;
	}
	
	/**
	 * <code>true</code> iff this map was re-arranged, i.e. all references to
	 *         {@link BidiEntry} are invalid now.
	 */
	private boolean checkCapacity() {
		if (checkCapacityAdd())
			return true;
		if (checkCapacityRemove())
			return true;
		return false;
	}

	/**
	 * <code>true</code> iff this map was re-arranged, i.e. all references to
	 *         {@link BidiEntry} are invalid now.
	 */
	private boolean checkCapacityRemove() {
		if (size < minSize) {
			int newHashModulus;
			newHashModulus = decreaseHashModulus(hashModulus, size);
			rehash(newHashModulus);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <code>true</code> iff this map was re-arranged, i.e. all references to
	 *         {@link BidiEntry} are invalid now.
	 */
	private boolean checkCapacityAdd() {
		if (size > maxSize) {
			int newHashModulus;
			newHashModulus = increaseHashModulus(hashModulus, size);
			rehash(newHashModulus);
			return true;
		} else {
			return false;
		}
	}

	private static int decreaseHashModulus(int initialHashModulus, int expectedSize) {
		int maxHashModulus = (int) (expectedSize / SHRINK_FILL_FACTOR) - 1;
		
		int newHashModulus = initialHashModulus;
		while (newHashModulus > INITIAL_CAPACITY && newHashModulus > maxHashModulus) {
			newHashModulus /= 2;
		}
		return newHashModulus;
	}

	private static int increaseHashModulus(int initialHashModulus, int expectedSize) {
		int minHashModulus = (int) (expectedSize / EXPAND_FILL_FACTOR) + 1;
		
		int newHashModulus = initialHashModulus;
		while (newHashModulus < minHashModulus) {
			newHashModulus *= 2;
		}
		return newHashModulus;
	}

	private void rehash(int newHashModulus) {
		BidiEntry<K, V>[] oldEntries = table;
		if (newHashModulus == oldEntries.length) {
			return;
		}
		
		hashModulus = newHashModulus;
		int newSize = 2 * hashModulus;
		table = allocate(newSize);
		
		initRange(hashModulus);
		
		for (int n = sourceIndex(0), cnt = oldEntries.length; n < cnt; n += 2) {
			BidiEntry<K, V> e = oldEntries[n];
			
			while (e != null) {
				// Fetch next source here, because inserting into new table override reference.
				BidiEntry<K, V> nextSource = e.nextSource;
				insertEntry(e, sourceIndex(e.sourceHash), destIndex(e.destHash));
				e = nextSource;
			}
		}
	}

	/* package protected */static <T> T externalize(T destination) {
		return destination == NULL ? null : destination;
	}

	private void insertSourceEntry(BidiEntry<K, V> e, int sourceIndex) {
		e.nextSource = table[sourceIndex];
		table[sourceIndex] = e;
	}
	
	private BidiEntry<K, V> getSourceEntry(Object source, int sourceHash, int sourceIndex) {
		BidiEntry<K, V> e = table[sourceIndex];
        while (e != null) {
            if (e.sourceHash == sourceHash && equals(source, e.source)) {
                return e;
            }
            
            e = e.nextSource;
        }
        return null;
	}
	
	/* package protected */final void insertDestinationEntry(BidiEntry<K, V> e, int destIndex) {
		e.nextDestination = table[destIndex];
		table[destIndex] = e;
	}

	/* package protected */final BidiEntry<K, V> getDestinationEntry(Object destination, int destHash, int destIndex) {
		BidiEntry<K, V> e = table[destIndex];
		while (e != null) {
			if (e.destHash == destHash && equals(destination, e.destination)) {
				return e;
			}
			
            e = e.nextDestination;
		}
		return null;
	}
	
	/* package protected */final BidiEntry<K, V> removeDestinationEntry(Object destination, int destHash, int destIndex) {
		BidiEntry<K, V> last = null;
		BidiEntry<K, V> e = table[destIndex];
		while (e != null) {
			if (e.destHash == destHash && equals(destination, e.destination)) {
				if (last != null) {
					last.nextDestination = e.nextDestination;
				} else {
					table[destIndex] = e.nextDestination;
				}
				e.nextDestination = null;
				return e;
			}
			
			last = e;
            e = e.nextDestination;
		}
		return null;
	}
	
	private BidiEntry<K, V> removeSourceEntry(Object source, int sourceHash, int sourceIndex) {
		BidiEntry<K, V> last = null;
		BidiEntry<K, V> e = table[sourceIndex];
		while (e != null) {
			if (e.sourceHash == sourceHash && equals(source, e.source)) {
				if (last != null) {
					last.nextSource = e.nextSource;
				} else {
					table[sourceIndex] = e.nextSource;
				}
				return e;
			}
			
			last = e;
            e = e.nextSource;
		}
		return null;
	}
	
	/* package protected */final boolean removeDestinationEntry(BidiEntry<K, V> entry, int destIndex) {
		BidiEntry<K, V> last = null;
		BidiEntry<K, V> e = table[destIndex];
		while (e != null) {
			if (e == entry) {
            	// Remove entry.
				if (last != null) {
					last.nextDestination = e.nextDestination;
				} else {
					table[destIndex] = e.nextDestination;
				}
				e.nextDestination = null;
				return true;
			}
            last = e;
            e = e.nextDestination;
		}
		return false;
	}
	
	/* package protected */final boolean removeSourceEntry(BidiEntry<K, V> entry, int sourceIndex) {
		BidiEntry<K, V> last = null;
		BidiEntry<K, V> e = table[sourceIndex];
		while (e != null) {
			if (e == entry) {
				// Remove entry.
				if (last != null) {
					last.nextSource = e.nextSource;
				} else {
					table[sourceIndex] = e.nextSource;
				}
				e.nextSource = null;
				return true;
			}
			last = e;
            e = e.nextSource;
		}
		return false;
	}

	@Override
	public K removeValue(Object value) {
		return externalize(internalRemoveValue(internalize(value)));
	}

	/* package protected */final K internalRemoveValue(Object value) {
		int destHash = hash(value);
		int destIndex = destIndex(destHash);
		
		BidiEntry<K, V> e = internalRemoveDestination(value, destHash, destIndex);
		if (e != null) {
			// IGNORE FindBugs(VO_VOLATILE_INCREMENT): Only "best effort" finding concurrent
			// modifications.
			modCount++;
			K key = e.source;
			checkCapacityRemove();
			return key;
		} else {
			return nullKey();
		}
	}

	@Override
	public void clear() {
		// IGNORE FindBugs(VO_VOLATILE_INCREMENT): Only "best effort" finding concurrent
		// modifications.
		modCount++;
		size = 0;
		Arrays.fill(table, null);
		
		checkCapacityRemove();
	}

	@Override
	public boolean containsKey(Object key) {
        Object source = internalize(key);
        int sourceHash = hash(source);
        int sourceIndex = sourceIndex(sourceHash);

		return getSourceEntry(source, sourceHash, sourceIndex) != null;
	}

	@Override
	public boolean containsValue(Object value) {
        Object destination = internalize(value);
        int destHash = hash(destination);
        int destIndex = destIndex(destHash);

		return getDestinationEntry(destination, destHash, destIndex) != null;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		if (entrySet == null) {
			entrySet = new AbstractSet<>() {
				@Override
				public Iterator<Entry<K, V>> iterator() {
					return new AbstractEntryIterator<>() {
						@Override
						protected Entry<K, V> toResult(BidiEntry<K, V> be) {
							return be;
						}
					};
				}

				@Override
				public int size() {
					return size;
				}
				
				@Override
				public boolean contains(Object o) {
					if (! (o instanceof Entry)) {
						return false;
					}
					Entry<?, ?> e = (Entry<?, ?>) o;
					
					return containsMapping(e);
				}

				@Override
				public boolean remove(Object o) {
					if (! (o instanceof Entry)) {
						return false;
					}
					Entry<?, ?> e = (Entry<?, ?>) o;
					
					return removeMapping(e);
				}
				
				@Override
				public void clear() {
					BidiHashMap.this.clear();
				}
			};
		}
		return entrySet;
	}

	@Override
	public V get(Object key) {
        Object source = internalize(key);
        int sourceHash = hash(source);
        int sourceIndex = sourceIndex(sourceHash);

		BidiEntry<K, V> e = getSourceEntry(source, sourceHash, sourceIndex);
		
		if (e != null) {
			return externalize(e.destination);
		} else {
			return null;
		}
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Set<K> keySet() {
		if (keySet == null) {
			keySet = new AbstractSet<>() {
				@Override
				public Iterator<K> iterator() {
					return new AbstractEntryIterator<>() {
						@Override
						protected K toResult(BidiEntry<K, V> be) {
							return externalize(be.source);
						}
					};
				}

				@Override
				public int size() {
					return size;
				}
				
				@Override
				public boolean contains(Object o) {
					return BidiHashMap.this.containsKey(o);
				}
				
				@Override
				public boolean remove(Object o) {
					return internalRemoveKey(internalize(o)) != null;
				}
				
				@Override
				public void clear() {
					BidiHashMap.this.clear();
				}
			};
		}
		return keySet;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> t) {
		// IGNORE FindBugs(VO_VOLATILE_INCREMENT): Only "best effort" finding concurrent
		// modifications.
		modCount++;
		// Adjust table size?
		internalPutAll(t);
	}

	private void internalPutAll(Map<? extends K, ? extends V> t) {
		for (Entry<? extends K, ? extends V> otherEntry : t.entrySet()) {
			this.put(otherEntry.getKey(), otherEntry.getValue());
		}
	}

	@Override
	public V remove(Object key) {
		return externalize(internalRemoveKey(internalize(key)));
	}

	/* package protected */final V internalRemoveKey(Object key) {
		int sourceHash = hash(key);
		int sourceIndex = sourceIndex(sourceHash);
		
		BidiEntry<K, V> e = internalRemoveSource(key, sourceHash, sourceIndex);
		if (e != null) {
			// IGNORE FindBugs(VO_VOLATILE_INCREMENT): Only "best effort" finding concurrent
			// modifications.
			modCount++;
			V value = e.destination;
			checkCapacityRemove();
			return value;
		} else {
			return nullValue();
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Set<V> values() {
		if (valueSet == null) {
			valueSet = new AbstractSet<>() {
				@Override
				public Iterator<V> iterator() {
					return new AbstractEntryIterator<>() {
						@Override
						protected V toResult(BidiEntry<K, V> be) {
							return externalize(be.destination);
						}
					};
				}

				@Override
				public int size() {
					return size;
				}
				
				@Override
				public boolean contains(Object o) {
					return BidiHashMap.this.containsValue(o);
				}

				@Override
				public boolean remove(Object o) {
					return internalRemoveValue(internalize(o)) != null;
				}
				
				@Override
				public void clear() {
					BidiHashMap.this.clear();
				}
			};
		}
		return valueSet;
	}

	/* package protected */final boolean containsMapping(Entry<?, ?> e) {
		Object searchSource;
		int searchSourceHash;
		Object searchDestination;
		if (e instanceof BidiEntry) {
			BidiEntry<?, ?> ownEntry = (BidiEntry<?, ?>) e;
			searchSource = ownEntry.source;
			searchSourceHash = ownEntry.sourceHash;
			
			searchDestination = ownEntry.destination;
		} else {
			searchSource = internalize(e.getKey());
			searchSourceHash = hash(searchSource);
			
			searchDestination = internalize(e.getValue());
		}
		
		return containsMapping(searchSource, searchSourceHash, searchDestination);
	}
	
	/*package protected*/ final boolean containsMapping(Object source, int sourceHash, Object destination) {
		int sourceIndex = sourceIndex(sourceHash);
		BidiEntry<K, V> e = getSourceEntry(source, sourceHash, sourceIndex);
		if (e == null) {
			return false;
		}
		
		return equals(destination, e.destination);
	}

	/* package protected */final boolean removeMapping(Entry<?, ?> e) {
		Object searchSource;
		int searchSourceHash;
		Object searchDestination;
		if (e instanceof BidiEntry) {
			BidiEntry<?, ?> ownEntry = (BidiEntry<?, ?>) e;
			searchSource = ownEntry.source;
			searchSourceHash = ownEntry.sourceHash;
			
			searchDestination = ownEntry.destination;
		} else {
			searchSource = internalize(e.getKey());
			searchSourceHash = hash(searchSource);
			
			searchDestination = internalize(e.getValue());
		}
		
		return removeMapping(searchSource, searchSourceHash, searchDestination);
	}
	
	/*package protected*/ final boolean removeMapping(Object source, int sourceHash, Object destination) {
		int sourceIndex = sourceIndex(sourceHash);
		
		BidiEntry<K, V> e = getSourceEntry(source, sourceHash, sourceIndex);
		if (e == null || (! BidiHashMap.equals(e.destination, destination))) {
			return false;
		}
		
		// Actually remove.
		removeSourceEntry(e, sourceIndex);

		int destHash = hash(destination);
		int destIndex = destIndex(destHash);
		removeDestinationEntry(e, destIndex);
		
		// IGNORE FindBugs(VO_VOLATILE_INCREMENT): Only "best effort" finding concurrent
		// modifications.
		modCount++;
		size--;
		checkCapacityRemove();
		return true;
	}

	/* package protected */@SuppressWarnings("unchecked")
	static <T> T internalize(T x) {
		return x == null ? (T) NULL : x;
	}

	@SuppressWarnings("unchecked")
	private K nullKey() {
		return (K) NULL;
	}

	@SuppressWarnings("unchecked")
	private V nullValue() {
		return (V) NULL;
	}

	@SuppressWarnings("unchecked")
	private BidiEntry<K, V>[] allocate(int newSize) {
		return new BidiEntry[newSize];
	}

}
