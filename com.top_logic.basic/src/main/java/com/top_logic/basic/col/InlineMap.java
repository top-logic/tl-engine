/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.apache.commons.collections4.map.LinkedMap;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.util.Utils;

/**
 * Memory-optimized map implementation for very small maps.
 * 
 * <p>
 * {@link InlineMap} is used as follows:
 * 
 * <ul>
 * <li>Starting with an empty map that is a constant occupying only the storage used for the
 * reference itself:
 * <p>
 * <ul>
 * <li><code>InlineMap&lt;K,V> myMap = InlineMap.empty();</code></li>
 * </ul>
 * </p>
 * </li>
 * 
 * <li>Read access to the inline map is similar to conventional map objects:
 * <ul>
 * <li><code>size = myMap.size();</code></li>
 * <li><code>value = myMap.getValue(key);</code></li>
 * </ul>
 * </li>
 * 
 * <li>Modifying access to the inline map requires updating the reference to the map with the result
 * of the modifying method:
 * <ul>
 * <li><code>myMap = myMap.putValue(newKey, newValue);</code></li>
 * <li><code>myMap = myMap.removeValue(oldKey);</code></li>
 * </ul>
 * The map must be treated as if it were unmodifiable, creating a new version of the map for each
 * modifying access. Internally, this is not the case, the old version of the map must no longer be
 * used.</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface InlineMap<K, V> extends Iterable<Entry<K, V>> {

	/**
	 * The empty map singleton.
	 */
	public static <K, V> InlineMap<K, V> empty() {
		return Linked.emptyLink();
	}

	/**
	 * Whether this map has no entries.
	 * 
	 * @see Map#isEmpty()
	 */
	public abstract boolean isEmpty();

	/**
	 * The number of entries in this map.
	 * 
	 * @see Map#size()
	 */
	public abstract int size();

	/**
	 * Looks up the value for the given key.
	 * 
	 * @see Map#get(Object)
	 */
	public abstract V getValue(Object key);

	/**
	 * Checks whether there is an entry for the given key in this map.
	 * 
	 * @see Map#containsKey(Object)
	 */
	public abstract boolean hasValue(Object key);

	/**
	 * Adds a new entry to this map.
	 *
	 * @param key
	 *        The new key.
	 * @param value
	 *        The new value.
	 * @return The new version of this map.
	 * 
	 * @see Map#put(Object, Object)
	 */
	public abstract InlineMap<K, V> putValue(K key, V value);

	/**
	 * Removes an entry from this map.
	 *
	 * @param key
	 *        The key to remove.
	 * @return The new version of this map.
	 * 
	 * @see Map#remove(Object)
	 */
	public abstract InlineMap<K, V> removeValue(Object key);

	/**
	 * Utility to put all value from the given conventional map to this map.
	 * 
	 * @param map
	 *        The source of new entries.
	 * @return The new version of this map.
	 */
	public abstract InlineMap<K, V> putAllValues(Map<? extends K, ? extends V> map);

	/**
	 * Utility to put all value from the given inline map to this map.
	 * 
	 * @param map
	 *        The source of new entries.
	 * @return The new version of this map.
	 */
	public abstract InlineMap<K, V> putAllValues(InlineMap<? extends K, ? extends V> map);

	/**
	 * Converts this inline map to a conventional map.
	 */
	public abstract Map<K, V> toMap();

	/**
	 * A linked variant of {@link InlineMap}.
	 * 
	 * <p>
	 * Must no used directly, see {@link InlineMap#empty()}.
	 * </p>
	 */
	@FrameworkInternal
	static abstract class Linked<K, V> implements InlineMap<K, V>, Map.Entry<K, V> {

		/**
		 * Threshold of entries kept as linked list.
		 * 
		 * <p>
		 * Only if a {@link LinkedMap} contains more entries, a hash-table is allocated.
		 * </p>
		 */
		static final int LINK_LIMIT = 3;

		@Override
		public abstract Linked<K, V> removeValue(Object key);

		protected abstract Linked<K, V> doPutValue(K key, V value);

		protected abstract boolean hasValue();

		protected abstract Linked<K, V> next();

		@SuppressWarnings("unchecked")
		protected static <K, V> Linked<K, V> emptyLink() {
			return (Linked<K, V>) Empty.INSTANCE;
		}

		@Override
		public InlineMap<K, V> putAllValues(Map<? extends K, ? extends V> map) {
			if (map.size() > LINK_LIMIT) {
				return new Hashed<>(this).putAllValues(map);
			}

			InlineMap<K, V> result = this;
			for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
				result = result.putValue(entry.getKey(), entry.getValue());
			}
			return result;
		}

		@Override
		public InlineMap<K, V> putAllValues(InlineMap<? extends K, ? extends V> map) {
			if (map.size() > LINK_LIMIT) {
				return new Hashed<>(this).putAllValues(map);
			}

			InlineMap<K, V> result = this;
			for (Entry<? extends K, ? extends V> entry : map) {
				result = result.putValue(entry.getKey(), entry.getValue());
			}
			return result;
		}

		private static final class Empty<K, V> extends Linked<K, V> {

			static final Empty<Object, Object> INSTANCE = new Empty<>();

			@Override
			public boolean isEmpty() {
				return true;
			}

			@Override
			public int size() {
				return 0;
			}

			@Override
			public V getValue(Object key) {
				return null;
			}

			@Override
			public boolean hasValue(Object key) {
				return false;
			}

			@Override
			public InlineMap<K, V> putValue(K key, V value) {
				return doPutValue(key, value);
			}

			@Override
			public Linked<K, V> doPutValue(K key, V value) {
				return new Link<>(key, value);
			}

			@Override
			public Linked<K, V> removeValue(Object key) {
				return this;
			}

			@Override
			protected boolean hasValue() {
				return false;
			}

			@Override
			public K getKey() {
				throw new UnsupportedOperationException();
			}

			@Override
			public V getValue() {
				throw new UnsupportedOperationException();
			}

			@Override
			protected Linked<K, V> next() {
				throw new UnsupportedOperationException();
			}

			@Override
			public V setValue(Object value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Iterator<Entry<K, V>> iterator() {
				return Collections.<Entry<K, V>> emptyList().iterator();
			}

			@Override
			public Map<K, V> toMap() {
				return Collections.emptyMap();
			}
		}
	}

	/**
	 * A linked variant of {@link InlineMap}.
	 * 
	 * <p>
	 * Must no used directly, see {@link InlineMap#empty()}.
	 * </p>
	 */
	@FrameworkInternal
	static class Link<K, V> extends Linked<K, V> {

		private K _key;

		private V _value;

		private int _size;

		private Linked<K, V> _next;

		/**
		 * Creates a {@link InlineMap.Link}.
		 */
		Link(K key, V value) {
			this(key, value, emptyLink());
		}

		private Link(K key, V value, Linked<K, V> next) {
			_key = key;
			_value = value;
			_next = next;
			updateSize();
		}

		@Override
		public V getValue(Object key) {
			return Utils.equals(key, _key) ? _value : _next.getValue(key);
		}

		@Override
		public boolean hasValue(Object key) {
			return Utils.equals(key, _key) ? true : _next.hasValue(key);
		}

		@Override
		public Linked<K, V> removeValue(Object key) {
			if (Utils.equals(key, _key)) {
				return _next;
			} else {
				_next = _next.removeValue(key);
				updateSize();
				return this;
			}
		}

		@Override
		public InlineMap<K, V> putValue(K key, V value) {
			Linked<K, V> result = doPutValue(key, value);
			if (result.size() > LINK_LIMIT) {
				return new Hashed<>(result);
			}
			return result;
		}

		@Override
		public Linked<K, V> doPutValue(K key, V value) {
			if (Utils.equals(key, _key)) {
				_value = value;
			} else {
				_next = _next.doPutValue(key, value);
				updateSize();
			}
			return this;
		}

		private void updateSize() {
			_size = 1 + _next.size();
		}

		@Override
		public int size() {
			return _size;
		}

		@Override
		public K getKey() {
			return _key;
		}

		@Override
		public V getValue() {
			return _value;
		}

		@Override
		protected boolean hasValue() {
			return true;
		}

		@Override
		protected Linked<K, V> next() {
			return _next;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public V setValue(V value) {
			V old = _value;
			_value = value;
			return old;
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new Iterator<>() {

				Linked<K, V> _current = Link.this;

				@Override
				public boolean hasNext() {
					return _current.hasValue();
				}

				@Override
				public Entry<K, V> next() {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}
					Linked<K, V> result = _current;
					_current = _current.next();
					return result;
				}
			};
		}

		@Override
		public Map<K, V> toMap() {
			HashMap<K, V> result = MapUtil.newMap(size());
			for (Entry<K, V> entry : this) {
				result.put(entry.getKey(), entry.getValue());
			}
			return result;
		}

	}

	/**
	 * A hashed variant of {@link InlineMap}.
	 * 
	 * <p>
	 * Must no used directly, see {@link InlineMap#empty()}.
	 * </p>
	 */
	@FrameworkInternal
	static class Hashed<K, V> extends HashMap<K, V> implements InlineMap<K, V> {

		Hashed(Linked<K, V> link) {
			while (link.hasValue()) {
				put(link.getKey(), link.getValue());
				link = link.next();
			}
		}

		@Override
		public V getValue(Object key) {
			return get(key);
		}

		@Override
		public boolean hasValue(Object key) {
			return containsKey(key);
		}

		@Override
		public InlineMap<K, V> putValue(K key, V value) {
			put(key, value);
			return this;
		}

		@Override
		public InlineMap<K, V> removeValue(Object key) {
			remove(key);
			return this;
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return entrySet().iterator();
		}

		@Override
		public Map<K, V> toMap() {
			return this;
		}

		@Override
		public InlineMap<K, V> putAllValues(Map<? extends K, ? extends V> map) {
			putAll(map);
			return this;
		}

		@Override
		public InlineMap<K, V> putAllValues(InlineMap<? extends K, ? extends V> map) {
			for (Entry<? extends K, ? extends V> entry : map) {
				put(entry.getKey(), entry.getValue());
			}
			return this;
		}

	}

}
