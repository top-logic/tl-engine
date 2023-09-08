/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MapIterator;

/**
 * Inverse view of a {@link BidiMap} for imperfect but easy implementation of
 * {@link BidiMap#inverseBidiMap()}.
 * 
 * <p>
 * This view does not support setting entry values, neither while iterating a {@link #mapIterator()}
 * , nor when accessing the {@link #entrySet()}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InverseBidiMap<K, V> implements BidiMap<K, V> {

	private final BidiMap<V, K> _impl;

	/**
	 * Creates a {@link InverseBidiMap}.
	 * 
	 * @param impl
	 *        The underlying implementation.
	 */
	public InverseBidiMap(BidiMap<V, K> impl) {
		_impl = impl;
	}

	@Override
	public int size() {
		return _impl.size();
	}

	@Override
	public boolean isEmpty() {
		return _impl.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return _impl.containsValue(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return _impl.containsKey(value);
	}

	@Override
	public V get(Object key) {
		return _impl.getKey(key);
	}

	@Override
	public V remove(Object key) {
		return _impl.removeValue(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
			_impl.put(entry.getValue(), entry.getKey());
		}
	}

	@Override
	public void clear() {
		_impl.clear();
	}

	@Override
	public Set<K> keySet() {
		return _impl.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		final Set<Entry<V, K>> set = _impl.entrySet();
		return new AbstractSet<>() {

			@Override
			public boolean add(Entry<K, V> e) {
				return set.add(swap(e));
			}

			@Override
			public boolean contains(Object o) {
				if (!(o instanceof Entry<?, ?>)) {
					return false;
				}
				return set.contains(swap((Entry<?, ?>) o));
			}

			@Override
			public Iterator<Entry<K, V>> iterator() {
				final Iterator<Entry<V, K>> it = set.iterator();
				return new Iterator<>() {

					@Override
					public boolean hasNext() {
						return it.hasNext();
					}

					@Override
					public Entry<K, V> next() {
						return swap(it.next());
					}

					@Override
					public void remove() {
						it.remove();
					}
				};
			}

			@Override
			public int size() {
				return set.size();
			}

			private <KK, VV> Entry<KK, VV> swap(final Entry<VV, KK> entry) {
				return new Entry<>() {

					@Override
					public KK getKey() {
						return entry.getValue();
					}

					@Override
					public VV getValue() {
						return entry.getKey();
					}

					@Override
					public VV setValue(VV value) {
						throw new UnsupportedOperationException(
							"Cannot update entry values of inverse BidiMap views.");
					}

					@Override
					public int hashCode() {
						KK key = getKey();
						VV value = getValue();
						return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
					}

					@Override
					public boolean equals(Object obj) {
						if (obj == this) {
							return true;
						}
						if (!(obj instanceof Entry<?, ?>)) {
							return false;
						}
						Entry<?, ?> e1 = this;
						Entry<?, ?> e2 = (Entry<?, ?>) obj;

						Object key1 = e1.getKey();
						Object value1 = e1.getValue();
						Object key2 = e2.getKey();
						Object value2 = e2.getValue();
						return (key1 == null ? key2 == null : key1.equals(key2)) &&
							(value1 == null ? value2 == null : value1.equals(value2));
					}

				};
			}

		};
	}

	@Override
	public MapIterator<K, V> mapIterator() {
		final MapIterator<V, K> it = _impl.mapIterator();
		return new MapIterator<>() {

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public K next() {
				it.next();
				return it.getValue();
			}

			@Override
			public K getKey() {
				return it.getValue();
			}

			@Override
			public V getValue() {
				return it.getKey();
			}

			@Override
			public void remove() {
				it.remove();
			}

			@Override
			public V setValue(V value) {
				throw new UnsupportedOperationException("Cannot update values when iterating inverse BidiMaps.");
			}
		};
	}

	@Override
	public V put(K key, V value) {
		V before = _impl.getKey(key);
		_impl.put(value, key);
		return before;
	}

	@Override
	public K getKey(Object value) {
		return _impl.get(value);
	}

	@Override
	public K removeValue(Object value) {
		return _impl.remove(value);
	}

	@Override
	public BidiMap<V, K> inverseBidiMap() {
		return _impl;
	}

	@Override
	public Set<V> values() {
		return _impl.keySet();
	}

	@Override
	public int hashCode() {
		// Map hash code is defined to be symmetric.
		return _impl.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Map<?, ?>)) {
			return false;
		}
		return entrySet().equals(((Map<?, ?>) obj).entrySet());
	}

}
