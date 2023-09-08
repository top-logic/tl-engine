/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * {@link Map} proxy that allows observing changes to a map implementation.
 * 
 * <p>
 * This solution is independent of the underlying {@link Map} implementation and transforms calls to
 * bulk APIs like {@link #putAll(Map)} to the corresponding atomic API {@link #put(Object, Object)}.
 * </p>
 * 
 * @see #afterPut(Object, Object)
 * @see #afterRemove(Object, Object)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ObservableMapProxy<K, V> extends MapProxy<K, V> {

	private Set<java.util.Map.Entry<K, V>> entrySet;
	
	private Set<K> keySet;

	private Collection<V> values;

	@Override
	public V put(K key, V value) {
		beforePut(key, value);
		V oldValue = impl().put(key, value);

		if (oldValue != null) {
			afterRemove(key, oldValue);
		}
		afterPut(key, value);

		return oldValue;
	}

	/**
	 * Hook that is called before each single put operation.
	 * 
	 * @param key
	 *        The key that being added.
	 * @param value
	 *        The new value being added.
	 */
	protected void beforePut(K key, V value) {
		// Hook for subclasses.
	}

	/**
	 * Hook that is called after each single put operation.
	 * 
	 * @param key
	 *        The key that was removed.
	 * @param value
	 *        The new value that was associated with the given key in the last put operation.
	 */
	protected void afterPut(K key, V value) {
		// Hook for subclasses.
	}

	@Override
	public V remove(Object key) {
		boolean containedBefore = containsKey(key);
		V valueBefore = impl().remove(key);
		if (containedBefore) {
			afterRemove(key, valueBefore);
		}
		return valueBefore;
	}

	/**
	 * Hook that is called after each single removal.
	 * 
	 * @param key
	 *        The key that was removed.
	 * @param oldValue
	 *        The old value that was associated with the given key before the removal.
	 */
	protected void afterRemove(Object key, V oldValue) {
		// Hook for subclasses.
	}

	@Override
	public void clear() {
		for (Iterator<Entry<K, V>> it = entrySet().iterator(); it.hasNext();) {
			it.next();
			it.remove();
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> other) {
		Set<? extends Map.Entry<? extends K, ? extends V>> entrySet2 = other.entrySet();
		for (Iterator<? extends Entry<? extends K, ? extends V>> it = entrySet2.iterator(); it.hasNext();) {
			Map.Entry<? extends K, ? extends V> otherEntry = it.next();
			put(otherEntry.getKey(), otherEntry.getValue());
		}
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> es;
		return (es = entrySet) == null ? (entrySet = new ObservableEntrySet(impl().entrySet())) : es;
	}

	@Override
	public Set<K> keySet() {
		Set<K> ks;
		return (ks = keySet) == null ? (keySet = new ObservableKeySet(impl().keySet())) : ks;
	}

	@Override
	public Collection<V> values() {
		Collection<V> vals;
		return (vals = values) == null ? (values = new ObservableValuesCollection(impl().values())) : vals;
	}
	
	private final class ObservableEntrySet extends SetProxy<Entry<K, V>> {

		private Set<java.util.Map.Entry<K, V>> _impl;

		public ObservableEntrySet(Set<Entry<K, V>> impl) {
			_impl = impl;
		}

		@Override
		protected Set<Entry<K, V>> impl() {
			return _impl;
		}

		@Override
		public boolean add(Entry<K, V> entry) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends Entry<K, V>> a1) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object obj) {
			if (obj instanceof Entry<?, ?>) {
				// Note: It is important to retrieve the key and value before actually removing the
				// entry, since the underlying implementation may destroy the entry on removal.
				@SuppressWarnings("unchecked")
				Entry<K, V> entry = (Entry<K, V>) obj;
				K key = entry.getKey();
				V value = entry.getValue();

				boolean changed = impl().remove(obj);
				if (changed) {
					afterRemove(key, value);
				}
				return changed;
			} else {
				return false;
			}
		}

		@Override
		public void clear() {
			ObservableMapProxy.this.clear();
		}

		@Override
		public boolean removeAll(Collection<?> entries) {
			boolean modified = false;
			for (Object entry : entries) {
				modified |= remove(entry);
			}
			return modified;
		}

		@Override
		public boolean retainAll(Collection<?> entries) {
			Objects.requireNonNull(entries);
			boolean modified = false;
			for (Iterator<Entry<K, V>> it = iterator(); it.hasNext();) {
				if (!entries.contains(it.next())) {
					it.remove();
					modified = true;
				}
			}
			return modified;
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			final Iterator<Map.Entry<K, V>> inner = impl().iterator();
			return new Iterator<>() {
				private Entry<K, V> _last;

				@Override
				public boolean hasNext() {
					return inner.hasNext();
				}

				@Override
				public Map.Entry<K, V> next() {
					java.util.Map.Entry<K, V> result = inner.next();
					_last = result;
					return result;
				}

				@Override
				public void remove() {
					K key = _last.getKey();
					V value = _last.getValue();

					inner.remove();

					afterRemove(key, value);
				}
			};
		}

	}

	private final class ObservableKeySet extends SetProxy<K> {

		private Set<K> _impl;

		public ObservableKeySet(Set<K> impl) {
			_impl = impl;
		}

		@Override
		protected Set<K> impl() {
			return _impl;
		}

		@Override
		public boolean add(K a1) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends K> a1) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object key) {
			boolean containedBefore = containsKey(key);
			ObservableMapProxy.this.remove(key);
			return containedBefore;
		}

		@Override
		public void clear() {
			ObservableMapProxy.this.clear();
		}

		@Override
		public boolean removeAll(Collection<?> keys) {
			boolean modified = false;
			for (Object key : keys) {
				modified |= remove(key);
			}
			return modified;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			Objects.requireNonNull(c);
			boolean modified = false;
			for (Iterator<K> it = iterator(); it.hasNext();) {
				if (!c.contains(it.next())) {
					it.remove();
					modified = true;
				}
			}
			return modified;
		}

		@Override
		public Iterator<K> iterator() {
			final Iterator<K> inner = impl().iterator();
			return new Iterator<>() {

				private K _last;

				@Override
				public boolean hasNext() {
					return inner.hasNext();
				}

				@Override
				public K next() {
					K result = inner.next();
					_last = result;
					return result;
				}

				@Override
				public void remove() {
					K keyBefore = _last;
					V valueBefore = get(keyBefore);

					inner.remove();

					afterRemove(keyBefore, valueBefore);
				}

			};
		}
	}

	private class ObservableValuesCollection extends CollectionProxy<V> {

		private Collection<V> _impl;

		public ObservableValuesCollection(Collection<V> impl) {
			_impl = impl;
		}

		@Override
		protected Collection<V> impl() {
			return _impl;
		}

		@Override
		public boolean add(V a1) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends V> a1) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object value) {
			Iterator<Entry<K, V>> it = entrySet().iterator();
			if (value == null) {
				while (it.hasNext()) {
					Entry<K, V> entry = it.next();
					if (entry.getValue() == null) {
						it.remove();
						return true;
					}
				}
			} else {
				while (it.hasNext()) {
					Map.Entry<K, V> entry = it.next();
					if (value.equals(entry.getValue())) {
						it.remove();
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public void clear() {
			ObservableMapProxy.this.clear();
		}

		@Override
		public boolean removeAll(Collection<?> other) {
			Objects.requireNonNull(other);
			boolean modified = false;
			Iterator<Entry<K, V>> it = entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<K, V> entry = it.next();
				if (other.contains(entry.getValue())) {
					it.remove();
					modified = true;
				}
			}
			return modified;
		}

		@Override
		public boolean retainAll(Collection<?> other) {
			Objects.requireNonNull(other);
			boolean modified = false;
			Iterator<Entry<K, V>> it = entrySet().iterator();
			while (it.hasNext()) {
				java.util.Map.Entry<K, V> entry = it.next();
				if (!other.contains(entry.getValue())) {
					it.remove();
					modified = true;
				}
			}
			return modified;
		}

		@Override
		public Iterator<V> iterator() {
			final Iterator<Entry<K, V>> inner = entrySet().iterator();
			return new Iterator<>() {
				@Override
				public boolean hasNext() {
					return inner.hasNext();
				}

				@Override
				public V next() {
					return inner.next().getValue();
				}

				@Override
				public void remove() {
					inner.remove();
				}
			};
		}

	}

}
